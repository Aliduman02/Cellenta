
--Paket Numara sayacı
CREATE SEQUENCE package_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

--Müşteri numara sayacı
CREATE SEQUENCE customer_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

--Müşteri kullanım num. sayacı
CREATE SEQUENCE usage_seq
START WITH 1 
INCREMENT BY 1
NOCACHE;

--Bildirim num. sayacı
CREATE SEQUENCE notification_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

--Login num. sayacı
CREATE SEQUENCE login_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

--Dml Log Sayacı
CREATE SEQUENCE dml_log_seq START WITH 1 INCREMENT BY 1;
