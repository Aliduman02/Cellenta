CREATE OR REPLACE PROCEDURE get_customer
(
  p_msisdn        IN VARCHAR2,
  o_customer      OUT SYS_REFCURSOR,
  o_status_code   OUT NUMBER
)
AS
  v_op_id     NUMBER;
  v_count     NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;
  log_dml_stage(v_op_id, 1, 'CUSTOMER', 'SELECT', 'STARTED');
  log_dml_stage(v_op_id, 2, 'CUSTOMER', 'SELECT', 'PROCESSING');

  SELECT COUNT(*) INTO v_count FROM CUSTOMER WHERE MSISDN = p_msisdn;

  IF v_count = 0 THEN
    o_status_code := 404;
    OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', 'User Not Found');
    RETURN;
  END IF;

  OPEN o_customer FOR
    SELECT CUSTOMER_ID,
           MSISDN,
           NAME,
           SURNAME,
           EMAIL,
           PASSWORD,
           CREATED_AT
    FROM CUSTOMER
    WHERE MSISDN = p_msisdn;

  o_status_code := 200;
  log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'SUCCESS');
  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', SQLERRM);
END;
/



CREATE OR REPLACE PROCEDURE get_customer_by_id
(
  p_customer_id IN VARCHAR2,
  o_customer OUT SYS_REFCURSOR,
  o_status_code OUT NUMBER
)
AS
  v_op_id NUMBER;
  v_exists NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'CUSTOMER', 'SELECT', 'STARTED');
  log_dml_stage(v_op_id, 2, 'CUSTOMER', 'SELECT', 'PROCESSING');

  -- Giriş parametresi null ise
  IF p_customer_id IS NULL THEN
    o_status_code := 404;
    OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', 'Customer ID is NULL');
    RETURN;
  END IF;

  SELECT COUNT(*) INTO v_exists
  FROM CUSTOMER
  WHERE CUSTOMER_ID = p_customer_id;

  IF v_exists = 0 THEN
    o_status_code := 404;
    OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', 'User Not Found');
    RETURN;
  END IF;

  OPEN o_customer FOR
      SELECT  CUSTOMER_ID,
              MSISDN,
              NAME,
              SURNAME,
              EMAIL,
              PASSWORD,
              CREATED_AT
      FROM CUSTOMER
      WHERE CUSTOMER_ID = p_customer_id;

  o_status_code := 200;
  log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'SUCCESS');
  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', SQLERRM);
    ROLLBACK;
END;
/



CREATE OR REPLACE PROCEDURE get_msisdns_haspackage(
  o_msisdns OUT SYS_REFCURSOR,
  o_status_code OUT NUMBER
)
AS
  v_op_id NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'CUSTOMER', 'SELECT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'CUSTOMER', 'SELECT', 'PROCESSING');

  OPEN o_msisdns FOR
    SELECT C.MSISDN
    FROM CUSTOMER C
    WHERE EXISTS (
      SELECT 1
      FROM BALANCE B
      WHERE B.CUSTOMER_ID = C.CUSTOMER_ID
    );

  o_status_code := 200;
  log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'SUCCESS');

EXCEPTION
  WHEN OTHERS THEN
    OPEN o_msisdns FOR SELECT NULL AS MSISDN FROM dual WHERE 1 = 0;
    o_status_code := 500;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'ERROR', SQLERRM);
END;
/


CREATE OR REPLACE PROCEDURE get_package
(
  p_package_id IN NUMBER,
  o_package OUT SYS_REFCURSOR,
  o_status_code OUT NUMBER
)
AS
  v_op_id NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'PACKAGE', 'SELECT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'PACKAGE', 'SELECT', 'PROCESSING');

    OPEN o_package FOR
        SELECT  PACKAGE_ID,
                PACKAGE_NAME,
                PRICE,
                AMOUNT_MINUTES,
                AMOUNT_DATA,
                AMOUNT_SMS,
                PERIOD
        FROM SUBSCRIPTION_PACKAGES
        WHERE PACKAGE_ID = p_package_id;

    o_status_code := 200;
    log_dml_stage(v_op_id, 3, 'PACKAGE', 'SELECT', 'SUCCESS');

EXCEPTION
  WHEN NO_DATA_FOUND THEN
    o_status_code := 404;
    OPEN o_package FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'PACKAGE', 'SELECT', 'ERROR','User Not Found');
  WHEN OTHERS THEN
    o_status_code := 500;
    OPEN o_package FOR SELECT NULL FROM dual WHERE 1 = 0;
    log_dml_stage(v_op_id, 3, 'PACKAGE', 'SELECT', 'ERROR', SQLERRM);
END;
/





CREATE OR REPLACE PROCEDURE LOGIN_CUSTOMER (
    p_msisdn      IN VARCHAR2,
    p_password    IN  VARCHAR2,
    p_device_type IN VARCHAR2 DEFAULT 'notDefined',
    p_ip_address IN VARCHAR2 DEFAULT 'notDefined',
    o_customer    OUT SYS_REFCURSOR,
    o_status_code OUT NUMBER
)
IS
    v_op_id NUMBER;
    v_count NUMBER;
    v_customer_id CUSTOMER.CUSTOMER_ID%TYPE;
BEGIN
    
    SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

    log_dml_stage(v_op_id, 1, 'CUSTOMER', 'SELECT', 'STARTED');

    log_dml_stage(v_op_id, 2, 'CUSTOMER', 'SELECT', 'PROCESSING');

    SELECT COUNT(*) INTO v_count
    FROM CUSTOMER
    WHERE MSISDN = p_msisdn;

    IF v_count = 0 THEN
        o_status_code := 404;
        log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'FAILED', 'Email doesn''t exist');

        OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;

        
    ELSE
        SELECT COUNT(*) INTO v_count
        FROM CUSTOMER
        WHERE MSISDN = p_msisdn AND PASSWORD = p_password;


        IF v_count = 0 THEN
            -- Şifre yanlış
            o_status_code := 401;
            log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'FAILED', 'Wrong Password');
            OPEN o_customer FOR SELECT NULL FROM dual WHERE 1 = 0;

        ELSE
            o_status_code := 200;

            SELECT CUSTOMER_ID INTO v_customer_id
            FROM CUSTOMER
            WHERE MSISDN = p_msisdn AND PASSWORD = p_password;

            OPEN o_customer FOR
                SELECT CUSTOMER_ID,
                       MSISDN,
                       NAME,
                       SURNAME,
                       EMAIL,
                       CREATED_AT
                       
                FROM CUSTOMER
                WHERE CUSTOMER_ID = v_customer_id;

            insert_login_history(v_customer_id,SYSTIMESTAMP,p_device_type,p_ip_address);
            log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'SUCCESS');

        END IF;
    END IF;
    
EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'CUSTOMER', 'SELECT', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    RAISE;
END;
/





CREATE OR REPLACE PROCEDURE insert_password_reset_code (
  p_email       IN VARCHAR2,
  p_ip_address  IN VARCHAR2 DEFAULT NULL,
  p_device_type IN VARCHAR2 DEFAULT NULL,
  o_validity_period OUT NUMBER,
  o_reset_code   OUT VARCHAR2,
  o_status_code      OUT NUMBER
) AS
  v_op_id     NUMBER;
  v_code      VARCHAR2(6);
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'PASSWORD_RESET_CODES', 'INSERT', 'STARTED');

  log_dml_stage(v_op_id, 2, 'PASSWORD_RESET_CODES', 'INSERT', 'PROCESSING');

  SELECT LPAD(TRUNC(DBMS_RANDOM.VALUE(0, 1000000)), 6, '0') INTO v_code FROM dual;

  o_validity_period := 2;


  INSERT INTO PASSWORD_RESET_CODES (
    EMAIL, RESET_CODE, CREATED_AT, EXPIRED_AT, IS_USED, IP_ADDRESS, DEVICE_TYPE
  ) VALUES (
    p_email,
    v_code,
    CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Istanbul',
    (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Istanbul') + INTERVAL '2' MINUTE,
    'N',
    p_ip_address,
    p_device_type
  );
  o_reset_code := v_code;
  o_status_code := 200;

  log_dml_stage(v_op_id, 3, 'PASSWORD_RESET_CODES', 'INSERT', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'PASSWORD_RESET_CODES', 'INSERT', 'FAILED', v_err);
      o_status_code := 500;
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    ROLLBACK;
    RAISE;
END;
/





CREATE OR REPLACE PROCEDURE verify_password_reset_code (
  p_email        IN VARCHAR2,
  p_reset_code   IN VARCHAR2,
  o_status_code  OUT NUMBER
) AS
  v_op_id        NUMBER;
  v_reset_id     PASSWORD_RESET_CODES.RESET_ID%TYPE;
  v_expired_at   TIMESTAMP;
  v_is_used      CHAR(1);
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;
  log_dml_stage(v_op_id, 1, 'PASSWORD_RESET_CODES', 'UPDATE', 'STARTED');

  SELECT RESET_ID, EXPIRED_AT, IS_USED
  INTO v_reset_id, v_expired_at, v_is_used
  FROM PASSWORD_RESET_CODES
  WHERE EMAIL = p_email AND RESET_CODE = p_reset_code;

  IF v_is_used = 'Y' THEN
    o_status_code := 409;  
    log_dml_stage(v_op_id, 2, 'PASSWORD_RESET_CODES', 'UPDATE', 'FAILED', 'Code already used');
    RETURN;
  END IF;

  IF v_expired_at <  CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Istanbul' THEN
    o_status_code := 410;  
    log_dml_stage(v_op_id, 2, 'PASSWORD_RESET_CODES', 'UPDATE', 'FAILED', 'Code expired');
    RETURN;
  END IF;

  log_dml_stage(v_op_id, 2, 'PASSWORD_RESET_CODES', 'UPDATE', 'PROCESSING');

  UPDATE PASSWORD_RESET_CODES
  SET IS_USED = 'Y',
      USED_AT =  CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Istanbul'
  WHERE RESET_ID = v_reset_id;

  log_dml_stage(v_op_id, 3, 'PASSWORD_RESET_CODES', 'UPDATE', 'SUCCESS');
  o_status_code := 200; 
  COMMIT;

EXCEPTION
  WHEN NO_DATA_FOUND THEN
    o_status_code := 404;  
    log_dml_stage(v_op_id, 2, 'PASSWORD_RESET_CODES', 'UPDATE', 'FAILED', 'Code not found');
    ROLLBACK;
  WHEN OTHERS THEN
    BEGIN
      log_dml_stage(v_op_id, 3, 'PASSWORD_RESET_CODES', 'UPDATE', 'FAILED', SUBSTR(SQLERRM, 1, 4000));
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    o_status_code := 500;  
    ROLLBACK;
    RAISE;
END;
/



CREATE OR REPLACE PROCEDURE change_customer_password (
  p_email         IN VARCHAR2,
  p_new_password  IN VARCHAR2,
  o_status_code   OUT NUMBER
) AS
  v_op_id       NUMBER;
BEGIN
  SELECT dml_log_seq.NEXTVAL INTO v_op_id FROM dual;

  log_dml_stage(v_op_id, 1, 'CUSTOMER', 'UPDATE', 'STARTED');

  log_dml_stage(v_op_id, 2, 'CUSTOMER', 'UPDATE', 'PROCESSING');

  UPDATE CUSTOMER
  SET PASSWORD = p_new_password
  WHERE EMAIL = p_email;

  IF SQL%ROWCOUNT = 0 THEN
    o_status_code := 404;
    log_dml_stage(v_op_id, 3, 'CUSTOMER', 'UPDATE', 'FAILED', 'Email not found');
    RETURN;
  END IF;

  o_status_code := 200;

  log_dml_stage(v_op_id, 3, 'CUSTOMER', 'UPDATE', 'SUCCESS');

  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    DECLARE
      v_err VARCHAR2(4000);
    BEGIN
      v_err := SUBSTR(SQLERRM, 1, 4000);
      log_dml_stage(v_op_id, 3, 'CUSTOMER', 'UPDATE', 'FAILED', v_err);
    EXCEPTION
      WHEN OTHERS THEN NULL;
    END;
    o_status_code := 500;
    ROLLBACK;
    RAISE;
END;
/