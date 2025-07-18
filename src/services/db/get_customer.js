import { getConnection } from "./dbconenct.js";
import oracledb from "oracledb";

export async function callGetCustomer(msisdn) {
  let connection;
  try {
    connection = await getConnection();
    const customer = await connection.execute(
      `BEGIN
         get_customer(:p_msisdn, :o_customer, :o_status_code);
       END;`,
      {
        p_msisdn: msisdn,
        o_customer: { type: oracledb.CURSOR, dir: oracledb.BIND_OUT },
        o_status_code: { type: oracledb.NUMBER, dir: oracledb.BIND_OUT },
      },
      {
        outFormat: oracledb.OUT_FORMAT_OBJECT,
      }
    );
    const customerSet = customer.outBinds.o_customer;
    const statusCode = customer.outBinds.o_status_code;
    const rows = await customerSet.getRows();
    return {
      rows,
      statusCode,
    };
  } catch (error) {
    console.error("get_customer error:", error);
    throw error;
  } finally {
    if (connection) {
      try {
        await connection.close();
      } catch {}
    }
  }
}
