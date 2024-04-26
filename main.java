import java.util.Scanner;

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

    public static void main(String[] args) {
        main program = new main();
        program.start();
    }
}