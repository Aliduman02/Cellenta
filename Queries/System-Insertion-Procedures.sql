
CREATE OR REPLACE PROCEDURE insert_package (
  p_name           VARCHAR2,
  p_price          NUMBER,
  p_amount_minute  NUMBER,
  p_amount_data    NUMBER,
  p_amount_sms     NUMBER,
  p_period         NUMBER,
  o_result     OUT VARCHAR2,
  o_package_id OUT NUMBER
) AS
  v_op_id NUMBER;
  v_id    NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;
  EXECUTE IMMEDIATE 'SELECT package_seq.NEXTVAL FROM dual' INTO v_id;

  log_dml_stage(v_op_id, 1, 'SUBSCRIPTION_PACKAGES', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'SUBSCRIPTION_PACKAGES', 'INSERT', 'PROCESSING');

  INSERT INTO SUBSCRIPTION_PACKAGES (
    PACKAGE_ID, PACKAGE_NAME, PRICE, AMOUNT_MINUTES, AMOUNT_DATA, AMOUNT_SMS, PERIOD
  ) VALUES (
    v_id, p_name, p_price, p_amount_minute, p_amount_data, p_amount_sms, p_period
  );

  o_result := 'OK';
  o_package_id := v_id;

  log_dml_stage(v_op_id, 3, 'SUBSCRIPTION_PACKAGES', 'INSERT', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'SUBSCRIPTION_PACKAGES', 'INSERT', 'FAILED', v_err);
      o_result := 'ERROR';
      o_package_id := NULL;
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    RAISE;
END;
/


CREATE OR REPLACE PROCEDURE insert_balance (
  p_customer_id      NUMBER,
  p_package_id       NUMBER,
  p_remaining_minutes NUMBER,
  p_remaining_sms     NUMBER,
  p_remaining_data    NUMBER,
  p_start_date        TIMESTAMP,
  p_end_date          TIMESTAMP
) AS
  v_op_id NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'BALANCE', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'BALANCE', 'INSERT', 'PROCESSING');

  INSERT INTO BALANCE (
    CUSTOMER_ID, PACKAGE_ID, REMAINING_MINUTES, REMAINING_SMS,
    REMAINING_DATA, START_DATE, END_DATE
  ) VALUES (
    p_customer_id, p_package_id, p_remaining_minutes, p_remaining_sms,
    p_remaining_data, p_start_date, p_end_date
  );


  log_dml_stage(v_op_id, 3, 'BALANCE', 'INSERT', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'BALANCE', 'INSERT', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL; 
    END;
    ROLLBACK;
    RAISE;
END;
/

CREATE OR REPLACE PROCEDURE insert_customer (
  p_msisdn       VARCHAR2,
  p_name         VARCHAR2,
  p_surname      VARCHAR2,
  p_email        VARCHAR2,
  p_password     VARCHAR2,
  o_customer_id OUT NUMBER
) AS
  v_op_id NUMBER;
  v_id    NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  SELECT customer_seq.NEXTVAL INTO v_id FROM dual;

  log_dml_stage(v_op_id, 1, 'CUSTOMER', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'CUSTOMER', 'INSERT', 'PROCESSING');

  INSERT INTO CUSTOMER (
    CUSTOMER_ID, MSISDN, NAME, SURNAME, EMAIL, PASSWORD, CREATED_AT
  ) VALUES (
    v_id, p_msisdn, p_name, p_surname, p_email, p_password, SYSTIMESTAMP
  )
  RETURNING CUSTOMER_ID INTO o_customer_id;



  log_dml_stage(v_op_id, 3, 'CUSTOMER', 'INSERT', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'CUSTOMER', 'INSERT', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    RAISE;
END;
/

CREATE OR REPLACE PROCEDURE insert_personal_usage (
  p_giver_msisdn     VARCHAR2 ,
  p_receiver_msisdn  VARCHAR2 DEFAULT NULL,
  p_usage_date       TIMESTAMP,
  p_usage_type       VARCHAR2,
  p_usage_duration   NUMBER,
  o_status_code      OUT NUMBER
) AS
  v_op_id   NUMBER;
  v_id      NUMBER;
  v_dummy   NUMBER; 
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'PERSONAL_USAGE', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'PERSONAL_USAGE', 'INSERT', 'PROCESSING');

  BEGIN
    SELECT 1 INTO v_dummy
    FROM CUSTOMER
    WHERE MSISDN = p_giver_msisdn;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      o_status_code := 404;
      log_dml_stage(v_op_id, 3, 'PERSONAL_USAGE', 'INSERT', 'FAILED', 'Giver not found');
      RETURN;
  END;

  IF p_receiver_msisdn IS NOT NULL THEN
    BEGIN
      SELECT 1 INTO v_dummy
      FROM CUSTOMER
      WHERE MSISDN = p_receiver_msisdn;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        o_status_code := 404;
        log_dml_stage(v_op_id, 3, 'PERSONAL_USAGE', 'INSERT', 'FAILED', 'Receiver not found');
        RETURN;
    END;
  END IF;

  SELECT usage_seq.NEXTVAL INTO v_id FROM dual;

  INSERT INTO PERSONAL_USAGE (
    PERSONAL_USAGE_ID, GIVER_MSISDN, RECEIVER_MSISDN,
    USAGE_DATE, USAGE_TYPE, USAGE_DURATION
  ) VALUES (
    v_id, p_giver_msisdn, p_receiver_msisdn,
    p_usage_date, p_usage_type, p_usage_duration
  );

  log_dml_stage(v_op_id, 3, 'PERSONAL_USAGE', 'INSERT', 'SUCCESS');

  COMMIT;
  o_status_code := 200;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'PERSONAL_USAGE', 'INSERT', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    o_status_code := 500;
END;
/

CREATE OR REPLACE PROCEDURE insert_notification_log (
  p_notification_type VARCHAR2,
  p_notification_time TIMESTAMP,
  p_customer_id       NUMBER
) AS
  v_op_id NUMBER;
  v_id    NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  SELECT notification_seq.NEXTVAL INTO v_id FROM dual;

  log_dml_stage(v_op_id, 1, 'NOTIFICATION_LOGS', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'NOTIFICATION_LOGS', 'INSERT', 'PROCESSING');

  INSERT INTO NOTIFICATION_LOGS (
    NOTIFICATION_ID, NOTIFICATION_TYPE, NOTIFICATION_TIME, CUSTOMER_ID
  ) VALUES (
    v_id, p_notification_type, p_notification_time, p_customer_id
  );

  log_dml_stage(v_op_id, 3, 'NOTIFICATION_LOGS', 'INSERT', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'NOTIFICATION_LOGS', 'INSERT', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    RAISE;
END;
/

CREATE OR REPLACE PROCEDURE insert_login_history (
  p_customer_id  NUMBER,
  p_login_time   TIMESTAMP,
  p_device_type  VARCHAR2,
  p_ip_address   VARCHAR2
) AS
  v_op_id NUMBER;
  v_id    NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  SELECT login_seq.NEXTVAL INTO v_id FROM dual;

  log_dml_stage(v_op_id, 1, 'LOGIN_HISTORY', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'LOGIN_HISTORY', 'INSERT', 'PROCESSING');

  INSERT INTO LOGIN_HISTORY (
    LOGIN_ID, CUSTOMER_ID, LOGIN_TIME, DEVICE_TYPE, IP_ADDRESS
  ) VALUES (
    v_id, p_customer_id, p_login_time, p_device_type, p_ip_address
  );

  log_dml_stage(v_op_id, 3, 'LOGIN_HISTORY', 'INSERT', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'LOGIN_HISTORY', 'INSERT', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    RAISE;
END;
/






