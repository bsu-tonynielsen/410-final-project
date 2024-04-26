import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This application will keep track of things like what classes are offered by
 * the school, and which students are registered for those classes and provide
 * basic reporting. This application interacts with a database to store and
 * retrieve data.
 */
public class SchoolManagementSystem {

    public static void getAllClassesByInstructor(String first_name, String last_name) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {
        	connection = Database.getDatabaseConnection();
        			sqlStatement = connection.createStatement();
        			
        			String sql = "SELECT first_name, last_name, title, classes.code, classes.name as class_name, term FROM classes\n"
        					+ "JOIN\n"
        					+ "(SELECT class_id, instructor_id, term_id FROM class_sections) as class\n"
        					+ "ON class.class_id = classes.class_id\n"
        					+ "JOIN\n"
        					+ "(SELECT instructor_id, instructors.first_name, instructors.last_name, academic_title_id FROM instructors) as faculty\n"
        					+ "ON faculty.instructor_id = class.instructor_id\n"
        					+ "JOIN\n"
        					+ "(SELECT term_id, name as term FROM terms) as term_taught\n"
        					+ "ON term_taught.term_id = class.term_id\n"
        					+ "JOIN\n"
        					+ "(SELECT academic_title_id, title as title FROM academic_titles GROUP BY academic_title_id ) as prof_title\n"
        					+ "ON prof_title.academic_title_id = faculty.academic_title_id\n"
        					+ "WHERE faculty.first_name = \"%s\" AND faculty.last_name = \"%s\";";
        			sql = String.format(sql, first_name, last_name);
        			ResultSet resultSet = sqlStatement.executeQuery(sql);
        	
        			String output = "first_name | last_name | title | classes.code | classes.name | term\n";
        	
        			while(resultSet.next()) {
        				String firstName = resultSet.getString(1);
        				String lastName = resultSet.getString(2);
        				String title = resultSet.getString(3);
        				String code = resultSet.getString(4);
        				String className = resultSet.getString(5);
        				String term = resultSet.getString(6);

        				String row = firstName + " | " + lastName + " | " + title + " | " + code + " | " + className + " | " + term;
        				output = output + row + "\n";

        			}
        			System.out.println(output);
        			connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to get class sections");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }

    public static void submitGrade(String studentId, String classSectionID, String grade) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {connection = Database.getDatabaseConnection();
		sqlStatement = connection.createStatement();
		
		String sql = "UPDATE class_registration SET grade_id = \"%s\"\n" +
				" WHERE student_id = \"%s\" AND section_id\"%s\"";
		sql = String.format(sql, grade, studentId, classSectionID);;
		int resultSet = sqlStatement.executeUpdate(sql);

		String output = "Grade has been submitted";

		System.out.println(output);
		connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to submit grade");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void registerStudent(String studentId, String classSectionID) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {connection = Database.getDatabaseConnection();
		sqlStatement = connection.createStatement();
		
		String sql = "INSERT INTO class_registrations(student_id, class_section_id)\n" +
				"VALUES (\"%s\", \"%s\")";
		sql = String.format(sql, studentId, classSectionID);

		sqlStatement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet resultSet = sqlStatement.getGeneratedKeys();
		resultSet.next();

		int regisID = resultSet.getInt(1);

		String output = "Registration ID | Student ID | Class Section ID\n";

		String row = regisID + " | " + studentId + " | " + classSectionID;
		output = output + row;

		System.out.println(output);
		connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to register student");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void deleteStudent(String studentId) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {connection = Database.getDatabaseConnection();
		sqlStatement = connection.createStatement();
		
		String sql = "DELETE FROM students WHERE student_id = \"%s\" ";
		sql = String.format(sql, studentId);
		sqlStatement.execute(sql);

			String output = "Student with id: " + studentId + " was deleted";

		System.out.println(output);
		connection.close();
             
        } catch (SQLException sqlException) {
            System.out.println("Failed to delete student");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }


    public static void createNewStudent(String firstName, String lastName, String birthdate) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {connection = Database.getDatabaseConnection();
		sqlStatement = connection.createStatement();
		
		String sql = "INSERT INTO students(first_name,last_name, birthdate)\n" +
				"VALUES (\"%s\", \"%s\", \"%s\")";
		sql = String.format(sql, firstName, lastName, birthdate);

		sqlStatement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet resultSet = sqlStatement.getGeneratedKeys();
		resultSet.next();

		int studID = resultSet.getInt(1);

		String output = "Student ID | First Name | Last Name | Birthdate\n";

		String row = studID + " | " + firstName + " | " + lastName + " | " + birthdate;
		output = output + row;

		System.out.println(output);
		connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to create student");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }

    public static void listAllClassRegistrations() {
        Connection connection = null;
        Statement sqlStatement = null;

        try {
        	connection = Database.getDatabaseConnection();
			sqlStatement = connection.createStatement();
			
			String sql = "SELECT class_registration_id, class_section_id, student_id, grade_id FROM class_registrations";
			sql = String.format(sql);
			ResultSet resultSet = sqlStatement.executeQuery(sql);
	
			String output = "Class Registration ID | Class Section ID | student ID | Grade ID \n";
	
			while(resultSet.next()) {
				String classRegID = resultSet.getString(1);
				String classSecID = resultSet.getString(2);
				String studentID = resultSet.getString(3);
				String gradeID = resultSet.getString(4);

				String row = classRegID + " | " + classSecID + " | " + studentID + " | " + gradeID;
				output = output + row + "\n";

			}
			System.out.println(output);
			connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to get class sections");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void listAllClassSections() {
        Connection connection = null;
        Statement sqlStatement = null;

        try {
        	connection = Database.getDatabaseConnection();
			sqlStatement = connection.createStatement();
			
			String sql = "SELECT * FROM class_registrations";
			sql = String.format(sql);
			ResultSet resultSet = sqlStatement.executeQuery(sql);
	
			String output = "Class Section ID | Class Section ID | Instructor ID | Term ID \n";
	
			while(resultSet.next()) {
				String classRegID = resultSet.getString(1);
				String classSecID = resultSet.getString(2);
				String studentID = resultSet.getString(3);
				String gradeID = resultSet.getString(4);

				String row = classRegID + " | " + classSecID + " | " + studentID + " | " + gradeID;
				output = output + row + "\n";

			}
        } catch (SQLException sqlException) {
            System.out.println("Failed to get class sections");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void listAllClasses() {
        Connection connection = null;
        Statement sqlStatement = null;

        try {connection = Database.getDatabaseConnection();
			sqlStatement = connection.createStatement();
		
			String sql = "SELECT class_id, classes.code, classes.name, classes.description FROM classes";
			sql = String.format(sql);
			ResultSet resultSet = sqlStatement.executeQuery(sql);

			String output = "Class ID | Code | Name | Description\n";

			while(resultSet.next()) {
				String classID = resultSet.getString(1);
				String code = resultSet.getString(2);
				String name = resultSet.getString(3);
				String desc = resultSet.getString(4);

				String row = classID + " | " + code + " | " + name + " | " + desc;
				output = output + row + "\n";

			}
		System.out.println(output);
		connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to get students");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }


    public static void listAllStudents() {
        Connection connection = null;
        Statement sqlStatement = null;

        try {connection = Database.getDatabaseConnection();
		sqlStatement = connection.createStatement();
		
		String sql = "SELECT student_id, first_name, last_name, birthdate FROM students";
		sql = String.format(sql);
		ResultSet resultSet = sqlStatement.executeQuery(sql);

		String output = "Student ID | First Name | Last Name | Birthdate\n";

		while(resultSet.next()) {
			String studID = resultSet.getString(1);
			String firstName = resultSet.getString(2);
			String lastName = resultSet.getString(3);
			String bDay = resultSet.getString(4);

			String row = studID + " | " + firstName + " | " + lastName + " | " + bDay;
			output = output + row + "\n";

		}
		System.out.println(output);
		connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to get students");
            System.out.println(sqlException.getMessage());

        } finally {
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    /***
     * Splits a string up by spaces. Spaces are ignored when wrapped in quotes.
     *
     * @param command - School Management System cli command
     * @return splits a string by spaces.
     */
    public static List<String> parseArguments(String command) {
        List<String> commandArguments = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
        while (m.find()) commandArguments.add(m.group(1).replace("\"", ""));
        return commandArguments;
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the School Management System");
        System.out.println("-".repeat(80));

        Scanner scan = new Scanner(System.in);
        String command = "";

        do {
            System.out.print("Command: ");
            command = scan.nextLine();
            ;
            List<String> commandArguments = parseArguments(command);
            command = commandArguments.get(0);
            commandArguments.remove(0);

            if (command.equals("help")) {
                System.out.println("-".repeat(38) + "Help" + "-".repeat(38));
                System.out.println("test connection \n\tTests the database connection");

                System.out.println("list students \n\tlists all the students");
                System.out.println("list classes \n\tlists all the classes");
                System.out.println("list class_sections \n\tlists all the class_sections");
                System.out.println("list class_registrations \n\tlists all the class_registrations");
                System.out.println("list instructor <first_name> <last_name>\n\tlists all the classes taught by that instructor");


                System.out.println("delete student <studentId> \n\tdeletes the student");
                System.out.println("create student <first_name> <last_name> <birthdate> \n\tcreates a student");
                System.out.println("register student <student_id> <class_section_id>\n\tregisters the student to the class section");

                System.out.println("submit grade <studentId> <class_section_id> <letter_grade> \n\tcreates a student");
                System.out.println("help \n\tlists help information");
                System.out.println("quit \n\tExits the program");
            } else if (command.equals("test") && commandArguments.get(0).equals("connection")) {
                Database.testConnection();
            } else if (command.equals("list")) {
                if (commandArguments.get(0).equals("students")) listAllStudents();
                if (commandArguments.get(0).equals("classes")) listAllClasses();
                if (commandArguments.get(0).equals("class_sections")) listAllClassSections();
                if (commandArguments.get(0).equals("class_registrations")) listAllClassRegistrations();

                if (commandArguments.get(0).equals("instructor")) {
                    getAllClassesByInstructor(commandArguments.get(1), commandArguments.get(2));
                }
            } else if (command.equals("create")) {
                if (commandArguments.get(0).equals("student")) {
                    createNewStudent(commandArguments.get(1), commandArguments.get(2), commandArguments.get(3));
                }
            } else if (command.equals("register")) {
                if (commandArguments.get(0).equals("student")) {
                    registerStudent(commandArguments.get(1), commandArguments.get(2));
                }
            } else if (command.equals("submit")) {
                if (commandArguments.get(0).equals("grade")) {
                    submitGrade(commandArguments.get(1), commandArguments.get(2), commandArguments.get(3));
                }
            } else if (command.equals("delete")) {
                if (commandArguments.get(0).equals("student")) {
                    deleteStudent(commandArguments.get(1));
                }
            } else if (!(command.equals("quit") || command.equals("exit"))) {
                System.out.println(command);
                System.out.println("Command not found. Enter 'help' for list of commands");
            }
            System.out.println("-".repeat(80));
        } while (!(command.equals("quit") || command.equals("exit")));
        System.out.println("Bye!");
    }
}

