import java.io.*;
import java.sql.*;
import java.util.*;

public class JDBCexample {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if (args.length < 3) {
            System.out.println("Usage DBConnectTest <yourportnumber> <sandbox password> <dbname>");
        } else {
            Connection con = null;
            Statement stmt = null, stmt2 = null;
            try {
                int nRemotePort = Integer.parseInt(args[0]); // remote port number of your database
                String strDbPassword = args[1]; // database login password
                String dbName = args[2];

                /*
                 * STEP 1 and 2 LOAD the Database DRIVER and obtain a CONNECTION
                 */
                System.out.println("Attempting to connect to the database...");
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("jdbc:mysql://localhost:" + nRemotePort + "/test?verifyServerCertificate=false&useSSL=true");
                con = DriverManager.getConnection("jdbc:mysql://localhost:" + nRemotePort + "/test?verifyServerCertificate=false&useSSL=true&serverTimezone=UTC", "msandbox",
                        strDbPassword);
                // Do something with the Connection
                System.out.println("Database [test db] connection succeeded!");
                System.out.println();

                /*
                 * STEP 3 EXECUTE STATEMENTS (by using Transactions)
                 */

                con.setAutoCommit(false);//transaction block starts

                stmt = con.createStatement();

                /* TO EXECUTE A QUERY */

                System.out.println("Executing SELECT query...");
                ResultSet resultSet = stmt.executeQuery("select * from `" + dbName + "`.`Student`;");

                /* TO INSERT INTO TABLES */

                System.out.println("Inserting into the Class table...");
                String insert = "Insert into `" + dbName + "`.`Class` (Name, Code) Values ('Databases','CS410')";
                stmt2 = con.createStatement();
                int res = stmt2.executeUpdate(insert);

                //String[] data = {"boise", "nampa"};
                //stmt2 = insertLocations(con,data);

                con.commit(); //transaction block ends

                System.out.println("Transaction done!");

                /*
                 * STEP 4 Use result sets (tables) to navigate through the results
                 */

                ResultSetMetaData rsmd = resultSet.getMetaData();

                int columnsNumber = rsmd.getColumnCount();
                while (resultSet.next()) {
                    for (int i = 1; i <= columnsNumber; i++) {
                        if (i > 1) System.out.print(",  ");
                        String columnValue = resultSet.getString(i);
                        System.out.print(columnValue + " " + rsmd.getColumnName(i));
                    }
                    System.out.println(" ");
                }

                System.out.println("Number of rows affected by the insert statement: " + res);

            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                if (con != null) {
                    System.out.println("Rolling back the transaction...");
                    con.rollback(); // In case of any exception, we roll back to the database state we had before starting this transaction
                }
            } finally {

                /*
                 * STEP 5 CLOSE CONNECTION AND SSH SESSION
                 */

                if (stmt != null) {
                    stmt.close();
                }

                if (stmt2 != null) {
                    stmt2.close();
                }

                if (con != null) {
                    con.setAutoCommit(true); // restore default mode
                    con.close();
                    System.out.println("Database connection closed.");
                }
            }
        }
    }
}
