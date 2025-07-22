CREATE OR REPLACE PROCEDURE get_balance
(
  p_msisdn IN VARCHAR2,
  o_balance OUT SYS_REFCURSOR,
  o_status_code OUT NUMBER
)
AS
  v_customer_id NUMBER;
  v_op_id NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'BALANCE', 'SELECT', 'STARTED');

  SELECT CUSTOMER_ID
  INTO v_customer_id
  FROM CUSTOMER
  WHERE MSISDN = p_msisdn;

  log_dml_stage(v_op_id, 2, 'BALANCE', 'SELECT', 'PROCESSING');

  
  IF p_msisdn IS NULL THEN
    o_status_code := 404;
    OPEN o_balance FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'BALANCE', 'SELECT', 'ERROR','User Not Found');
    RETURN;
  ELSE
    OPEN o_balance FOR
        SELECT  CUSTOMER_ID,
                PACKAGE_ID,
                REMAINING_MINUTES,
                REMAINING_SMS,
                REMAINING_DATA,
                START_DATE,
                END_DATE
        FROM BALANCE
        WHERE CUSTOMER_ID = v_customer_id;

    o_status_code := 200;
    log_dml_stage(v_op_id, 3, 'BALANCE', 'SELECT', 'SUCCESS');
  END IF;
  
  COMMIT;

EXCEPTION
  WHEN NO_DATA_FOUND THEN
    o_status_code := 404;
    OPEN o_balance FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'BALANCE', 'SELECT', 'ERROR','User Not Found');
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_balance FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'BALANCE', 'SELECT', 'ERROR', SQLERRM);
    
END;
/




CREATE OR REPLACE PROCEDURE add_new_balance(
  p_customer_id  IN NUMBER,
  p_package_id   IN NUMBER,
  o_status_code       OUT VARCHAR
)
AS
  v_start_date        TIMESTAMP;
  v_end_date          TIMESTAMP;
  v_remaining_minutes NUMBER;
  v_remaining_sms     NUMBER;
  v_remaining_data    NUMBER;
  v_balance_id        NUMBER;
BEGIN
  SELECT AMOUNT_MINUTES, AMOUNT_SMS, AMOUNT_DATA
  INTO v_remaining_minutes, v_remaining_sms, v_remaining_data
  FROM SUBSCRIPTION_PACKAGES
  WHERE PACKAGE_ID = p_package_id;

  v_start_date := SYSTIMESTAMP;
  v_end_date   := ADD_MONTHS(v_start_date, 1);

  insert_balance(
    p_customer_id       => p_customer_id,
    p_package_id        => p_package_id,
    p_remaining_minutes => v_remaining_minutes,
    p_remaining_sms     => v_remaining_sms,
    p_remaining_data    => v_remaining_data,
    p_start_date        => v_start_date,
    p_end_date          => v_end_date
  );

  o_status_code   := 200;

EXCEPTION
  WHEN OTHERS THEN
    o_status_code   := 500;
END;
/





CREATE OR REPLACE PROCEDURE update_balance_all (
    p_msisdn         IN VARCHAR2,
    p_new_minutes    IN NUMBER, 
    p_new_sms        IN NUMBER, 
    p_new_data       IN NUMBER, 
    o_status_code    OUT NUMBER 
)
AS
    v_op_id NUMBER;
    v_row_count NUMBER;
    v_customer_id NUMBER;
BEGIN
    SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

    
    log_dml_stage(v_op_id, 1, 'BALANCE', 'UPDATE', 'STARTED');

    
    log_dml_stage(v_op_id, 2, 'BALANCE', 'UPDATE', 'PROCESSING');

    
    BEGIN
        SELECT CUSTOMER_ID INTO v_customer_id
        FROM CUSTOMER
        WHERE MSISDN = p_msisdn;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            o_status_code := 404;
            log_dml_stage(v_op_id, 3, 'BALANCE', 'UPDATE', 'ERROR', 'Customer not found for given MSISDN');
            ROLLBACK;
            RETURN;
    END;


    UPDATE BALANCE
    SET 
        REMAINING_MINUTES = GREATEST(p_new_minutes, 0),
        REMAINING_SMS     = GREATEST(p_new_sms, 0),
        REMAINING_DATA    = GREATEST(p_new_data, 0)
    WHERE CUSTOMER_ID = v_customer_id;

    v_row_count := SQL%ROWCOUNT;

    IF v_row_count = 0 THEN

        o_status_code := 404;
        log_dml_stage(v_op_id, 3, 'BALANCE', 'UPDATE', 'ERROR', 'Balance not found for customer');
        ROLLBACK;
        RETURN;
    END IF;


    log_dml_stage(v_op_id, 3, 'BALANCE', 'UPDATE', 'SUCCESS');
    o_status_code := 200;
    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        o_status_code := 500;
        log_dml_stage(v_op_id, 3, 'BALANCE', 'UPDATE', 'ERROR', SQLERRM);
        ROLLBACK;
END;
/




CREATE OR REPLACE PROCEDURE get_customer_invoices (
    p_msisdn     IN  VARCHAR2,
    o_cursor     OUT SYS_REFCURSOR,
    o_status_code OUT NUMBER
)
IS
    v_customer_id     CUSTOMER.CUSTOMER_ID%TYPE;
    v_today           DATE := TRUNC(SYSDATE);
    v_op_id NUMBER;
BEGIN
    SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

    log_dml_stage(v_op_id, 1, 'INVOICE', 'SELECT', 'STARTED');

    log_dml_stage(v_op_id, 2, 'INVOICE', 'SELECT', 'PROCESSING');
    SELECT CUSTOMER_ID
    INTO v_customer_id
    FROM CUSTOMER
    WHERE MSISDN = p_msisdn;

    OPEN o_cursor FOR
        SELECT 
            i.INVOICE_ID,
            i.CUSTOMER_ID,
            i.PACKAGE_ID,
            TO_CHAR(i.START_DATE, 'DD/MM/YY') AS START_DATE,
            TO_CHAR(i.END_DATE, 'DD/MM/YY') AS END_DATE,
            i.PRICE,
            i.PAYMENT_STATUS,            
            'N' AS IS_ACTIVE,
            NULL AS DAYS_LEFT
        FROM INVOICE i
        WHERE i.CUSTOMER_ID = v_customer_id
        ORDER BY i.START_DATE DESC

        UNION ALL

        SELECT 
            NULL AS INVOICE_ID,
            b.CUSTOMER_ID,
            b.PACKAGE_ID,
            TO_CHAR(b.START_DATE, 'DD/MM/YY') AS START_DATE,
            TO_CHAR(b.END_DATE, 'DD/MM/YY') AS END_DATE,
            sp.PRICE,
            'UNPAID' AS PAYMENT_STATUS,       
            'Y' AS IS_ACTIVE,
            GREATEST(TRUNC(b.END_DATE) - v_today, 0) AS DAYS_LEFT
        FROM BALANCE b
        JOIN SUBSCRIPTION_PACKAGES sp ON b.PACKAGE_ID = sp.PACKAGE_ID
        WHERE b.CUSTOMER_ID = v_customer_id
          AND NOT EXISTS (
              SELECT 1
              FROM INVOICE i
              WHERE i.CUSTOMER_ID = b.CUSTOMER_ID
                AND i.PACKAGE_ID = b.PACKAGE_ID
                AND i.START_DATE = b.START_DATE
                AND i.END_DATE = b.END_DATE
          );

    log_dml_stage(v_op_id, 3, 'INVOICE', 'SELECT', 'SUCCESS');
    o_status_code := 200;
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        o_status_code := 404;
        log_dml_stage(v_op_id, 3, 'INVOICE', 'SELECT', 'ERROR', SQLERRM);
        OPEN o_cursor FOR
            SELECT 
                NULL AS INVOICE_ID,
                NULL AS CUSTOMER_ID,
                NULL AS PACKAGE_ID,
                NULL AS START_DATE,
                NULL AS END_DATE,
                NULL AS PRICE,
                NULL AS PAYMENT_STATUS,
                NULL AS IS_ACTIVE,
                NULL AS DAYS_LEFT
            FROM DUAL WHERE 1 = 0;

    WHEN OTHERS THEN
        RAISE;
END;
/
