--SYNONYMS BEGIN
CREATE PUBLIC SYNONYM SUBSCRIPTION_PACKAGES FOR SYSTEM.SUBSCRIPTION_PACKAGES;
CREATE PUBLIC SYNONYM BALANCE FOR SYSTEM.BALANCE;
CREATE PUBLIC SYNONYM CUSTOMER FOR SYSTEM.CUSTOMER;
CREATE PUBLIC SYNONYM PERSONEL_USAGE FOR SYSTEM.PERSONAL_USAGE;
CREATE PUBLIC SYNONYM NOTIFICATION_LOGS FOR SYSTEM.NOTIFICATION_LOGS;
CREATE PUBLIC SYNONYM LOGIN_HISTORY FOR SYSTEM.LOGIN_HISTORY;
CREATE PUBLIC SYNONYM dml_multi_stage_log FOR SYSTEM.dml_multi_stage_log;
CREATE PUBLIC SYNONYM insert_package FOR SYSTEM.insert_package;
CREATE PUBLIC SYNONYM insert_balance FOR SYSTEM.insert_balance;
CREATE PUBLIC SYNONYM insert_customer FOR SYSTEM.insert_customer;
CREATE PUBLIC SYNONYM insert_notification_log FOR SYSTEM.insert_notification_log;
CREATE PUBLIC SYNONYM insert_login_history FOR SYSTEM.insert_login_history;
CREATE PUBLIC SYNONYM my_dml_logs FOR SYSTEM.my_dml_logs;
CREATE PUBLIC SYNONYM insert_personal_usage FOR SYSTEM.insert_personal_usage;

CREATE PUBLIC SYNONYM login_customer FOR SYSTEM.LOGIN_CUSTOMER;
CREATE PUBLIC SYNONYM register_customer FOR SYSTEM.register_customer;
CREATE PUBLIC SYNONYM get_package FOR SYSTEM.get_package;
CREATE PUBLIC SYNONYM get_all_packages FOR SYSTEM.get_all_packages;
CREATE PUBLIC SYNONYM add_new_balance FOR SYSTEM.add_new_balance;


CREATE PUBLIC SYNONYM log_notification FOR SYSTEM.log_notification;
CREATE PUBLIC SYNONYM get_customer FOR SYSTEM.get_customer;

CREATE PUBLIC SYNONYM get_balance FOR SYSTEM.get_balance;
CREATE PUBLIC SYNONYM update_balance_all FOR SYSTEM.update_balance_all;

CREATE PUBLIC SYNONYM get_customer_by_id FOR SYSTEM.get_customer_by_id;
CREATE PUBLIC SYNONYM get_msisdns_haspackage FOR SYSTEM.get_msisdns_haspackage;

CREATE PUBLIC SYNONYM insert_password_reset_code FOR SYSTEM.insert_password_reset_code;
CREATE PUBLIC SYNONYM verify_password_reset_code FOR SYSTEM.verify_password_reset_code;
CREATE PUBLIC SYNONYM change_customer_password FOR SYSTEM.change_customer_password;

CREATE PUBLIC SYNONYM get_customer_invoices FOR SYSTEM.get_customer_invoices;

CREATE PUBLIC SYNONYM get_last_30_logs_by_user FOR SYSTEM.get_last_30_logs_by_user;
CREATE PUBLIC SYNONYM get_error_logs_by_user FOR SYSTEM.get_error_logs_by_user;


--SYNONYMS END
