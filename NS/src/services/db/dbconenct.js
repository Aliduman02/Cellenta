import dotenv from "dotenv";
import oracledb from "oracledb";

dotenv.config();

export async function getConnection() {
  try {
    const connection = await oracledb.getConnection({
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      connectionString: `${process.env.DB_HOST}:${process.env.DB_PORT}/${process.env.DB_SERVICE}`,
    });
    return connection;
  } catch (error) {
    throw new Error("oracledb bağlantı hatası");
  }
}
