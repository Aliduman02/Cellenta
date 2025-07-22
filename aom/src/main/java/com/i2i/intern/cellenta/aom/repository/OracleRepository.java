package com.i2i.intern.cellenta.aom.repository;

import com.i2i.intern.cellenta.aom.exception.*;
import com.i2i.intern.cellenta.aom.model.Balance;
import com.i2i.intern.cellenta.aom.model.Customer;
import com.i2i.intern.cellenta.aom.model.Invoice;
import com.i2i.intern.cellenta.aom.model.Oracle.ResetCodeResponse;
import com.i2i.intern.cellenta.aom.model.Paket;
import com.i2i.intern.cellenta.aom.model.enums.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class OracleRepository {

    private static final String url = "jdbc:oracle:thin:@//34.155.38.208:1521/XEPDB1";
    private static final String username = "AOM_USERS";
    private static final String password = "aom37";

    private Connection conn = null;

    public boolean connect(){
        try{
            conn = DriverManager.getConnection(url, username, password);
        }catch (SQLException e){
            conn = null;
            throw new GeneralSQLException("Could not connect to Oracle database");
        }
        return true;
    }

    public boolean disconnect(){
        try{
            conn.close();
        }catch (SQLException e){
            throw new GeneralSQLException("Could not disconnect from Oracle database");
        }
        return true;
    }

    // BALANCE OPERATIONS

    public boolean addNewBalanceToCustomer(Long customerId, Long packageId){
        if(conn == null){connect();}
        try(CallableStatement stmt = conn.
                prepareCall("{ call add_new_balance(?, ?, ?) }")){

            stmt.setLong(1, customerId);
            stmt.setLong(2, packageId);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(3);

            if(statusCode == 200)
                return true;
            else if (statusCode == 500)
                throw new BalanceAddException("ORACLE - Add new balance failed - Oracle Code: 500");

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Add new balance failed - SQL Exception - STMT");
        }
        return false;
    }

    public Optional<Balance> getBalanceByMsisdn(String msisdn){
        if(conn == null){connect();}
        try(CallableStatement stmt = conn.
                prepareCall("{ call get_balance(?, ?, ?) }")){

            stmt.setString(1, msisdn);
            stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(3);

            if(statusCode == 200){
                try(ResultSet rs = (ResultSet) stmt.getObject(2)){
                    if (rs.next()) {
                        return Optional.of(Balance.builder()
                                .packageId(rs.getLong("PACKAGE_ID"))
                                .remainingSms(rs.getInt("REMAINING_SMS"))
                                .remainingMinutes(rs.getInt("REMAINING_MINUTES"))
                                .remainingData(rs.getInt("REMAINING_DATA"))
                                .startDate(rs.getTimestamp("START_DATE"))
                                .endDate(rs.getTimestamp("END_DATE"))
                                .build());
                    }
                }catch (SQLException e){
                    throw new GeneralSQLException("ORACLE - Get Balance By Msisdn - SQL Exception - RS");
                }
            }else if (statusCode == 404){
                throw new BalanceNotFoundException("ORACLE - Get Balance By Msisdn - Oracle Code: 404 - Balance Not Found");
            }else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Get Balance By Msisdn - Oracle Code: 500 - Balance Not Found");
            }else{
                return Optional.empty();
            }

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get Balance By Msisdn - SQL Exception - STMT");
        }
        return Optional.empty();
    }

    // PACKAGE OPERATIONS

    public List<Paket> getAllPackages() {
        if(conn == null){connect();}

        List<Paket> packages = new ArrayList<>();
        try(CallableStatement stmt = conn.
                prepareCall("{ call get_all_packages(?, ?) }")){
            stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(2);

            if(statusCode == 200){
                try(ResultSet rs = (ResultSet) stmt.getObject(1)){
                    while (rs.next()) {
                        Paket p = Paket.builder()
                                .id(rs.getLong("PACKAGE_ID"))
                                .packageName(rs.getString("PACKAGE_NAME"))
                                .price(rs.getDouble("PRICE"))
                                .amountMinutes(rs.getInt("AMOUNT_MINUTES"))
                                .amountData(rs.getInt("AMOUNT_DATA"))
                                .amountSms(rs.getInt("AMOUNT_SMS"))
                                .period(rs.getInt("PERIOD"))
                                .build();
                        packages.add(p);
                    }
                }catch (SQLException e){
                    throw new GeneralSQLException("ORACLE - Get All Packages Failed - SQL Exception - RS");
                }
            }else if(statusCode == 500){
                throw new GeneralOracleException("ORACLE - Get All Packages Failed - Code:500");
            }

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get All Packages - SQL Exception - STMT");
        }
        return packages;
    }

    public Optional<Paket> getPaketById(long id) {
        if(conn == null){connect();}

        try(CallableStatement stmt = conn.
                prepareCall("{ call get_package(?, ?, ?) }")){

            stmt.setLong(1, id);
            stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(3);

            if(statusCode == 200){
                try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                    if (rs.next()) {
                        return Optional.of(
                                Paket.builder()
                                        .id(rs.getLong("PACKAGE_ID"))
                                        .packageName(rs.getString("PACKAGE_NAME"))
                                        .price(rs.getDouble("PRICE"))
                                        .amountMinutes(rs.getInt("AMOUNT_MINUTES"))
                                        .amountData(rs.getInt("AMOUNT_DATA"))
                                        .amountSms(rs.getInt("AMOUNT_SMS"))
                                        .period(rs.getInt("PERIOD"))
                                        .build()
                        );
                    } else {
                        return Optional.empty();
                    }
                }catch (SQLException e){
                    throw new GeneralSQLException("ORACLE - Get Package with id: " + id + " - SQL Exception - RS");
                }
            }else if(statusCode == 404){
                throw new PackageNotFoundException("ORACLE - Get Package with id: " + id + "Failed - Code:404 - Package Not Found");
            }else if(statusCode == 500){
                throw new GeneralOracleException("ORACLE - Get Package with id: " + id + " Failed - Code:500");
            }
        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get Package with id: " + id + " - SQL Exception - STMT");
        }
        return Optional.empty();
    }


    // AUTH OPERATIONS

    public Optional<Customer> login(String msisdn, String password, String deviceType, String ipAddress){

        if(conn == null)
            connect();

        String sql = "{ call login_customer(?, ?, ?, ?, ?, ?) }";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            // IN parametreleri
            stmt.setString(1, msisdn);
            stmt.setString(2, password);
            stmt.setString(3, deviceType);
            stmt.setString(4, ipAddress);

            stmt.registerOutParameter(5, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(6, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(6);

            if (statusCode == 200) {
                try (ResultSet rs = (ResultSet) stmt.getObject(5)) {
                    if (rs.next()) {
                        return Optional.of(
                                Customer.builder()
                                        .cust_id(rs.getLong("CUSTOMER_ID"))
                                        .msisdn(rs.getString("MSISDN"))
                                        .name(rs.getString("NAME"))
                                        .surname(rs.getString("SURNAME"))
                                        .email(rs.getString("EMAIL"))
                                        .sdate(rs.getTimestamp("CREATED_AT"))
                                        .build()
                        );
                    } else {
                        return Optional.empty();
                    }
                }catch (SQLException e){
                    throw new GeneralSQLException("ORACLE - Login Failed - SQL Exception - RS");
                }
            }else if(statusCode == 401){
                throw new UserWrongPasswordException("ORACLE - Login Failed - Oracle Code:401 - Wrong Password");
            }
            else if (statusCode == 404){
                throw new UserNotFoundException("ORACLE - Login Failed - Oracle Code:404 - Customer Not Found");
            }
            else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Login Failed - Oracle Code:500");
            }

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Login Failed - SQL Exception - STMT");
        }
        return Optional.empty();
    }

    public Optional<Customer> register(String msisdn, String name, String surname, String email, String password){
        if(conn == null)
            connect();

        String sql = "{ call register_customer(?, ?, ?, ?, ?, ?, ?) }";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            // IN parametreleri
            stmt.setString(1, msisdn);
            stmt.setString(2, name);
            stmt.setString(3, surname);
            stmt.setString(4, email);
            stmt.setString(5, password);

            stmt.registerOutParameter(6, Types.INTEGER);
            stmt.registerOutParameter(7, Types.INTEGER);
            stmt.execute();

            // OUT parametreleri
            Long customerId = stmt.getLong(6);
            int statusCode = stmt.getInt(7);

            if (statusCode == 200) {
                return Optional.of(
                        Customer.builder()
                                .cust_id(customerId)
                                .msisdn(msisdn)
                                .name(name)
                                .surname(surname)
                                .email(email)
                                .password(password)
                                .sdate(Timestamp.valueOf(LocalDateTime.now()))
                                .build());
            }else if (statusCode == 409){
                throw new UserRegistrationException("ORACLE - Register Failed - Oracle Code:409 - User already exists");
            }
            else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Register Failed - Oracle Code:500");
            }
        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Register Failed - SQL Exception - STMT");
        }
        return Optional.empty();
    }

    public Optional<ResetCodeResponse> getResetCode(String email, String deviceType, String ipAddress){
        if(conn == null)
            connect();

        String sql = "{ call insert_password_reset_code(?, ?, ?, ?, ?, ?) }";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            // IN parametreleri
            stmt.setString(1, email);
            stmt.setString(2, ipAddress);
            stmt.setString(3, deviceType);

            stmt.registerOutParameter(4, Types.INTEGER);
            stmt.registerOutParameter(5, Types.VARCHAR);
            stmt.registerOutParameter(6, Types.INTEGER);
            stmt.execute();

            int minutes = stmt.getInt(4);
            String code = stmt.getString(5);
            int statusCode = stmt.getInt(6);

            if(statusCode == 200)
                return Optional.of(ResetCodeResponse.builder().code(code).minutes(minutes).build());
            else if(statusCode == 500)
                throw new GeneralOracleException("ORACLE - Reset Failed - Code:500");

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get Reset Code Failed - SQL Exception - STMT");
        }
        return Optional.empty();
    }

    public int checkTheCode(String email, String code){
        if(conn == null)
            connect();

        String sql = "{ call verify_password_reset_code(?, ?, ?) }";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            // IN parametreleri
            stmt.setString(1, email);
            stmt.setString(2, code);

            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            // OUT parametreleri
            int statusCode = stmt.getInt(3);

            if (statusCode == 200) {
                return statusCode;
            }else if (statusCode == 404){
                throw new VerifyCodeNotFoudExcepiton("ORACLE - Check Verify Code - Oracle Code:404 - Verify Code Not Found");
            }else if (statusCode == 409){
                throw new VerifyCodeAlreadyUsedException("ORACLE - Check Verify Code - Oracle Code:409 - Verify Code Already Used");
            }else if (statusCode == 410){
                throw new VerifyCodeHasExpiredException("ORACLE - Check Verify Code - Oracle Code:410 - Verify Code Expired");
            } else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Check Verify Code - Oracle Code:500");
            }
            return statusCode;

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Verify Code Failed - SQL Exception - STMT");
        }

    }

    // CUSTOMER OPERATIONS

    public Optional<Customer> getCustomerById(long id){
        if(conn == null){connect();}

        try(CallableStatement stmt = conn.
                prepareCall("{ call get_customer_by_id(?, ?, ?) }")){

            stmt.setLong(1, id);
            stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(3);

            if (statusCode == 200) {
                try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                    if (rs.next()) {
                        return Optional.of(
                                Customer.builder()
                                        .cust_id(rs.getLong("CUSTOMER_ID"))
                                        .msisdn(rs.getString("MSISDN"))
                                        .name(rs.getString("NAME"))
                                        .surname(rs.getString("SURNAME"))
                                        .email(rs.getString("EMAIL"))
                                        .password(rs.getString("PASSWORD"))
                                        .sdate(rs.getTimestamp("CREATED_AT"))
                                        .build()
                        );
                    } else {
                        return Optional.empty();
                    }
                } catch (SQLException e) {
                    throw new GeneralSQLException("ORACLE - Get Customer By Id Failed - SQL Exception - STMT");
                }
            }else if (statusCode == 404){
                throw new UserNotFoundException("ORACLE - Get Customer By Id Failed - Oracle Code:404 - Not Found");
            }else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Get Customer By Id Failed - Oracle Code:500");
            }else{
                return Optional.empty();
            }

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get Customer By Id Failed - SQL Exception - STMT");
        }
    }

    public boolean changePassword(String email, String password){
        if(conn == null)
            connect();

        String sql = "{ call change_customer_password(?, ?, ?) }";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(3);

            if (statusCode == 200) {
                return true;
            }else if (statusCode == 404){
                throw new UserNotFoundException("ORACLE - Change Password Failed - OracleCode:404 - User not found with email");
            }else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Change Password Failed - OracleCode:500");
            }

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Change Password Failed - SQL Exception - STMT");
        }
        return false;
    }

    public List<Invoice> getInvoiceByMsisdn(String msisdn){
        if(conn == null){connect();}

        try(CallableStatement stmt = conn.
                prepareCall("{ call get_customer_invoices(?, ?, ?) }")){

            stmt.setString(1, msisdn);
            stmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(3);

            if (statusCode != 200) {
                throw new GeneralOracleException("ORACLE - Get Invoice By Msisdn Failed - Oracle Code: 500 ");
            }
            List<Invoice> invoices = new ArrayList<>();
            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {

                while (rs.next()) {
                    Invoice deger = Invoice.builder()
                                    .id(rs.getLong("INVOICE_ID"))
                                    .customerId(rs.getLong("CUSTOMER_ID"))
                                    .packageId(rs.getLong("PACKAGE_ID"))
                                    .startDate(rs.getString("START_DATE"))
                                    .endDate(rs.getString("END_DATE"))
                                    .price(rs.getDouble("PRICE"))
                                    .paymentStatus(PaymentStatus.valueOf(rs.getString("PAYMENT_STATUS")))
                                    .isActive(rs.getString("IS_ACTIVE"))
                                    .daysLeft(rs.getString("DAYS_LEFT"))
                                    .build();
                    invoices.add(deger);
                }
                return invoices;
            }catch (SQLException e){
                throw new GeneralSQLException("ORACLE - Get Invoice By Msisdn - SQL Exception - RS");
            }
        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get Invoice By Msisdn - SQL Exception - STMT");
        }
    }

    public List<String> getMsisdnsHasPackage(){
        if(conn == null){connect();}

        try(CallableStatement stmt = conn.
                prepareCall("{ call get_msisdns_haspackage(?, ?) }")){

            stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();

            int statusCode = stmt.getInt(2);
            List<String> msisdns = new ArrayList<>();
            if (statusCode == 200){
                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    while (rs.next()) {
                        String msisdn = rs.getString("MSISDN");
                        msisdns.add(msisdn);
                    }
                    return msisdns;
                }catch (SQLException e){
                    throw new GeneralSQLException("ORACLE - Get Msisdns Failed - SQL Exception - RS");
                }
            }else if (statusCode == 500){
                throw new GeneralOracleException("ORACLE - Get Msisdns Failed - Oracle Code:500");
            }

        }catch (SQLException e){
            throw new GeneralSQLException("ORACLE - Get Msisdns Failed - SQL Exception - STMT");
        }
        return Collections.emptyList();
    }


}
