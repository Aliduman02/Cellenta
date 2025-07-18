package com.cellenta.cgf.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UsageRecordRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall jdbcCall;

    @PostConstruct
    public void init() {
        jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("insert_personal_usage")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_giver_msisdn", Types.VARCHAR),
                        new SqlParameter("p_receiver_msisdn", Types.VARCHAR),
                        new SqlParameter("p_usage_date", Types.TIMESTAMP),
                        new SqlParameter("p_usage_type", Types.VARCHAR),
                        new SqlParameter("p_usage_duration", Types.NUMERIC),
                        new SqlOutParameter("o_status_code", Types.INTEGER)
                );
    }

    public int callInsertUsageProcedure(
            String giverMsisdn,
            String usageType,
            Timestamp usageDate,
            int duration,
            String receiverMsisdn
    ) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_giver_msisdn", giverMsisdn);
        inParams.put("p_usage_date", usageDate);
        inParams.put("p_usage_type", usageType);
        inParams.put("p_usage_duration", duration);
        inParams.put("p_receiver_msisdn", receiverMsisdn);

        Map<String, Object> out = jdbcCall.execute(inParams);
        return (Integer) out.get("o_status_code");
    }
}
