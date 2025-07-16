import { getConnection } from "./dbconenct.js";
import oracledb from "oracledb";

export async function callLogNotification(email, notificationType) {
  let connection;
  try {
    connection = await getConnection();
    const result = await connection.execute(
      `BEGIN
         log_notification(:p_email, :p_notification_type, :o_status_code);
       END;`,
      {
        p_email: email,
        p_notification_type: notificationType,
        o_status_code: {
          type: oracledb.STRING,
          dir: oracledb.BIND_OUT,
          maxSize: 200,
        },
      }
    );
    return result.outBinds.o_status_code;
  } catch (error) {
    console.error("log_notification error:", error);
    throw error;
  } finally {
    if (connection) {
      try {
        await connection.close();
      } catch {}
    }
  }
}
