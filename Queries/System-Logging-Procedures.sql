CREATE OR REPLACE PROCEDURE log_dml_stage (
  p_operation_id   NUMBER,
  p_stage          NUMBER,
  p_table_name     VARCHAR2,
  p_operation      VARCHAR2,
  p_status         VARCHAR2,
  p_error_message  VARCHAR2 DEFAULT NULL
) AS
BEGIN
  INSERT INTO dml_multi_stage_log (
    operation_id, stage, table_name, operation, user_name, status, error_message
  )
  VALUES (
    p_operation_id,
    p_stage,
    p_table_name,
    p_operation,
    USER,
    p_status,
    p_error_message
  );
EXCEPTION
  WHEN OTHERS THEN
    NULL; 
END;
/


CREATE OR REPLACE PROCEDURE log_notification (
    p_email             IN VARCHAR2,
    p_notification_type IN VARCHAR2,
    o_status_code       OUT VARCHAR 
)
AS
    v_customer_id CUSTOMER.CUSTOMER_ID%TYPE;
    v_op_id NUMBER;
BEGIN
    SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

    log_dml_stage(v_op_id, 1, 'CUSTOMER', 'SELECT', 'STARTED');

    log_dml_stage(v_op_id, 2, 'CUSTOMER', 'SELECT', 'PROCESSING');

    BEGIN
        SELECT CUSTOMER_ID
        INTO v_customer_id
        FROM CUSTOMER
        WHERE EMAIL = p_email;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', 'User not found');
            o_status_code := '404';
            RETURN;
    END;

    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'SUCCESS');

    insert_notification_log(
        p_notification_type => p_notification_type,
        p_notification_time => CURRENT_TIMESTAMP,
        p_customer_id       => v_customer_id
    );

    COMMIT;
    o_status_code := '200';

EXCEPTION
    WHEN OTHERS THEN
        log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', SQLERRM);
        o_status_code := '500';
END;
/





CREATE OR REPLACE PROCEDURE get_last_30_logs_by_user (
  o_logs         OUT SYS_REFCURSOR,
  o_status_code  OUT NUMBER
)
AS
  v_username VARCHAR2(30);
  v_op_id NUMBER;
BEGIN
  v_username := USER;
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'DML_MULTI_STAGE_LOG', 'SELECT', 'STARTED');

  OPEN o_logs FOR
    SELECT *
    FROM dml_multi_stage_log
    WHERE user_name = v_username
    ORDER BY operation_time DESC
    FETCH FIRST 30 ROWS ONLY;

  o_status_code := 200;
  log_dml_stage(v_op_id, 2, 'DML_MULTI_STAGE_LOG', 'SELECT', 'SUCCESS');

EXCEPTION
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_logs FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 2, 'DML_MULTI_STAGE_LOG', 'SELECT', 'ERROR', SQLERRM);
END;
/



CREATE OR REPLACE PROCEDURE get_error_logs_by_user (
  o_logs         OUT SYS_REFCURSOR,
  o_status_code  OUT NUMBER
)
AS
  v_username VARCHAR2(30);
  v_op_id NUMBER;
BEGIN
  v_username := USER;
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'DML_MULTI_STAGE_LOG', 'SELECT', 'STARTED');

  OPEN o_logs FOR
    SELECT *
    FROM dml_multi_stage_log
    WHERE user_name = v_username
      AND (status = 'ERROR' OR status = 'FAILED')
    ORDER BY operation_time DESC;

  o_status_code := 200;
  log_dml_stage(v_op_id, 2, 'DML_MULTI_STAGE_LOG', 'SELECT', 'SUCCESS');

EXCEPTION
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_logs FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 2, 'DML_MULTI_STAGE_LOG', 'SELECT', 'ERROR', SQLERRM);
END;
/

