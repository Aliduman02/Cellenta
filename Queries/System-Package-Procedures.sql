CREATE OR REPLACE PROCEDURE get_all_packages
(
  o_packages OUT SYS_REFCURSOR,
  o_status_code OUT NUMBER
)
AS
  v_op_id NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'PACKAGE', 'SELECT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'PACKAGE', 'SELECT', 'PROCESSING');

  OPEN o_packages FOR
    SELECT  PACKAGE_ID,
            PACKAGE_NAME,
            PRICE,
            AMOUNT_MINUTES,
            AMOUNT_DATA,
            AMOUNT_SMS,
            PERIOD
    FROM SUBSCRIPTION_PACKAGES;

  o_status_code := 200;
  log_dml_stage(v_op_id, 3, 'PACKAGE', 'SELECT', 'SUCCESS');

EXCEPTION
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_packages FOR SELECT NULL FROM dual WHERE 1 = 0; -- boş cursor
    log_dml_stage(v_op_id, 3, 'PACKAGE', 'SELECT', 'ERROR', SQLERRM);
END;
/

CREATE OR REPLACE PROCEDURE register_customer(
  p_msisdn      IN VARCHAR2,
  p_name        IN VARCHAR2,
  p_surname     IN VARCHAR2,
  p_email       IN VARCHAR2,
  p_password    IN VARCHAR2,
  o_customer_id OUT NUMBER,
  o_result      OUT NUMBER
)
AS
  v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists
  FROM CUSTOMER
  WHERE MSISDN = p_msisdn OR EMAIL = p_email;

  IF v_exists > 0 THEN
    o_result := 409; 
    o_customer_id := NULL;
    RETURN;
  END IF;

  -- Yoksa kayıt yap
  insert_customer(p_msisdn, p_name, p_surname, p_email, p_password, o_customer_id);
  o_result := 200;

EXCEPTION
  WHEN OTHERS THEN
    o_result := 500;
    o_customer_id := NULL;
END;
/
