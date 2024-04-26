import java.util.Scanner;
import java.sql.*;

public class main {
    private Scanner scanner;

    public main() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Class Management System (CMS)!");

        boolean running = true;
        while (running) {
            System.out.print("CMS> ");
            String input = scanner.nextLine();
            
            String[] tokens = input.split(" ");
            String command = tokens[0];
            String[] arguments = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, arguments, 0, arguments.length);

            switch (command.toLowerCase()) {
                case "exit":
                    running = false;
                    break;
                case "new-class":
                    //TODO
                    break;
                case "select-class":
                    //Three cases here select-class <name> [<term>|<term> <section>]
                    //TODO
                    break;
                case "list-classes":
                    //TODO
                    break;
                case "show-class":
                    //TODO
                    break;
                case "show-categories":
                    //TODO
                    break;
                case "add-category":
                    //TODO
                    break;
                case "show-assignment":
                    //TODO
                    break;
                case "add-assignment":
                    //TODO
                    break;
                case "add-student":
                    //Two varieties add-students [username studentid Last First]
                    //TODO
                    break;
                case "show-students":
                    //Two varieties show-students [string]
                    //TODO
                    break;
                case "student-grades":
                    //TODO
                    break;
                case "grade":
                    //TODO
                    break;
                case "gradebook":
                    //TODO
                    break;
                case "help":
                    help();
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }

        System.out.println("Exiting Class Management System (CMS). Goodbye!");
        scanner.close();
    }
    
    private void help() {
        System.out.println("Available commands:");
        System.out.println("exit - Exit the program");
        //Add other available commands
        System.out.println("");
        System.out.println("help - Display available commands");
    }

    // public static void main(String[] args) throws SQLException {
    //     Connection connection = getDatabaseConnection();
    //    // main program = new main();
    //     //program.start();
    // }
    
    private static Connection getDatabaseConnection() throws SQLException {
        // Load the MySQL JDBC driver
        String DATABASE_URL = "jdbc:mysql://onyx.boisestate.edu:22/School?verifyServerCertificate=false&useSSL=false&serverTimezone=UTC";
        String USERNAME = "msandbox";
        String PASSWORD = "5524An";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found.");
            e.printStackTrace();
            throw new SQLException("MySQL JDBC driver not found.", e);
        }

        // Establish the database connection
        try {
            return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("SQLException was thrown while trying to connect to the database:");
            e.printStackTrace();
            throw e;
        }
    }
    

    public static void main(String[] args) {
        try {
            Connection connection = getDatabaseConnection();
            System.out.println("Connected to the database.");
            // Perform database operations here
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database:");
            e.printStackTrace();
        }
    }
}