import java.sql.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This application will keep track of things like what classes are offered by
 * the school, and which students are registered for those classes and provide
 * basic reporting. This application interacts with a database to store and
 * retrieve data.
 * 
 * Built atop hilltop of a re-purpossed 310 final project
 * @author tonynielsen
 */
public class SchoolManagementSystem {
	static int activeClassID = -1;
	static String currentClass = "";

	/**
	 * Retrieves and lists all classes along with their details from the database.
	 * This method fetches class information including class ID, course number, term, section,
	 * class description, and the number of students enrolled in each class.
	 * The class information is retrieved from the database using a SQL query.
	 * If there is an error while accessing the database or executing the SQL query,
	 * an error message is printed to the console.
	 * 
	 * @throws SQLException if there is an error accessing the database or executing the SQL query
	 */
    public static void listAllClasses() {
        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "SELECT c.classID, c.courseNumber, c.term, c.section, c.classDescription, COUNT(e.studentID) AS numStudents " +
                         "FROM Class c LEFT JOIN Enrollment e ON c.classID = e.classID " +
                         "GROUP BY c.classID";
            ResultSet resultSet = sqlStatement.executeQuery(sql);

            String output = "Class ID | Course Number | Term | Section | Description | Num Students\n";

            while(resultSet.next()) {
                int classID = resultSet.getInt("classID");
                String courseNumber = resultSet.getString("courseNumber");
                String term = resultSet.getString("term");
                String section = resultSet.getString("section");
                String classDescription = resultSet.getString("classDescription");
                int numStudents = resultSet.getInt("numStudents");

                String row = classID + " | " + courseNumber + " | " + term + " | " + section + " | " +
                             classDescription + " | " + numStudents;
                output = output + row + "\n";
            }

            System.out.println(output);
        } catch (SQLException sqlException) {
            System.out.println("Failed to get classes");
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
    
    /**
     * Creates a new class in the database with the provided course details.
     * This method inserts a new record into the "Class" table of the database
     * with the specified course number, term, section, and class description.
     * After successfully creating the class, the generated class ID along with
     * the course details is printed to the console.
     * 
     * @param courseNumber      the course number of the class
     * @param term              the term of the class
     * @param section           the section of the class
     * @param classDescription the description of the class
     * @throws SQLException     if there is an error accessing the database or executing the SQL query
     */
    public static void createNewClass(String courseNumber, String term, String section, String classDescription) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "INSERT INTO Class(courseNumber, term, section, classDescription)\n" +
                         "VALUES (\"%s\", \"%s\", \"%s\", \"%s\")";
            sql = String.format(sql, courseNumber, term, section, classDescription);

            sqlStatement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = sqlStatement.getGeneratedKeys();
            resultSet.next();

            int classID = resultSet.getInt(1);

            String output = "Class ID | Course Number | Term | Section | Class Description\n";

            String row = classID + " | " + courseNumber + " | " + term + " | " + section + " | " + classDescription;
            output = output + row;

            System.out.println(output);
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println("Failed to create class");
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
    
    /**
     * Activates a class based on the provided parameters.
     * This method queries the database to find a class that matches the provided criteria.
     * If a matching class is found, its details including class ID, course number, term,
     * section, and description are printed to the console, and the class is marked as active.
     * If no matching class is found, an appropriate message is printed to the console.
     * 
     * @param params an array of strings representing the parameters to match against the database
     * @throws SQLException if there is an error accessing the database or executing the SQL query
     */
    public static void activateClass(String... params) {
        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = buildActivationQuery(params);
            ResultSet resultSet = sqlStatement.executeQuery(sql);

            if (resultSet.next()) {
                System.out.println("Class activated:");
                System.out.println("Class ID | Course Number | Term | Section | Description");
                int classID = resultSet.getInt("classID");
                activeClassID = classID;
                String courseNumber = resultSet.getString("courseNumber");
                currentClass = courseNumber;
                String term = resultSet.getString("term");
                String section = resultSet.getString("section");
                String classDescription = resultSet.getString("classDescription");
                System.out.println(classID + " | " + courseNumber + " | " + term + " | " + section + " | " + classDescription);
            } else {
                System.out.println("No class found matching the provided criteria.");
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to activate class");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    
    /**
     * Displays the categories and their weights for the currently active class.
     * If no class is currently selected, it prints a message indicating so and returns.
     * This method queries the database to retrieve category names and their corresponding weights
     * for the active class and prints them to the console.
     * 
     * @throws SQLException if there is an error accessing the database or executing the SQL query
     */
    public static void showCategories() {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "SELECT categoryName, weight " +
                         "FROM Category " +
                         "WHERE classID = " + activeClassID;
            ResultSet resultSet = sqlStatement.executeQuery(sql);

            System.out.println("Categories and their weights for the active class:");
            System.out.println("Category Name | Weight");

            while(resultSet.next()) {
                String categoryName = resultSet.getString("categoryName");
                double weight = resultSet.getDouble("weight");
                System.out.println(categoryName + " | " + weight);
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to show categories");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Adds a new category to the currently active class.
     * If no class is currently selected, it prints a message indicating so and returns.
     * This method inserts a new category into the "Category" table of the database
     * with the provided name, weight, and the ID of the active class.
     * It then prints a success message if the category is added successfully,
     * otherwise, it prints a failure message.
     * 
     * @param name   the name of the category to be added
     * @param weight the weight of the category to be added
     * @throws SQLException if there is an error accessing the database or executing the SQL query
     */
    public static void addCategory(String name, String weight) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "INSERT INTO Category (categoryName, weight, classID) " +
                         "VALUES ('" + name + "', " + weight + ", " + activeClassID + ")";
            int rowsAffected = sqlStatement.executeUpdate(sql);

            if (rowsAffected > 0) {
                System.out.println("Category added successfully.");
            } else {
                System.out.println("Failed to add category.");
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to add category");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    /**
     * Displays the currently active class.
     * 
     * This method checks if a class is currently active. If not, it prints an error message.
     * It then retrieves the database connection and creates a statement.
     * Next, it constructs a SQL query to select class information based on the active class ID.
     * It executes the query and prints the details of the active class if found.
     * If no class is found matching the active class ID, it prints an appropriate message.
     */
    public static void showClass() {
        if (activeClassID == -1) {
            System.out.println("No class is currently active.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "SELECT classID, courseNumber, term, section, classDescription " +
                         "FROM Class " +
                         "WHERE classID = '" + activeClassID + "'";
            ResultSet resultSet = sqlStatement.executeQuery(sql);

            if (resultSet.next()) {
                System.out.println("Currently active class:");
                System.out.println("Class ID | Course Number | Term | Section | Description");
                int classID = resultSet.getInt("classID");
                String courseNumber = resultSet.getString("courseNumber");
                String term = resultSet.getString("term");
                String section = resultSet.getString("section");
                String classDescription = resultSet.getString("classDescription");
                System.out.println(classID + " | " + courseNumber + " | " + term + " | " + section + " | " + classDescription);
            } else {
                System.out.println("No class found matching the currently active class ID.");
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to show active class");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Displays all assignments grouped by category for the current class.
     * 
     * This method checks if a class is currently selected. If not, it prints an error message.
     * It then retrieves the database connection and creates a statement.
     * Next, it constructs a SQL query to retrieve assignments grouped by category, ordered by assignment name.
     * It executes the query and populates a map with assignments grouped by category.
     * Finally, it iterates over the map and prints each category along with its assignments and point values.
     */
    public static void showAssignments() {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Query to retrieve assignments grouped by category, ordered by assignmentID
            String sql = "SELECT c.categoryName, a.assignmentName, a.pointValue " +
                    "FROM Category c " +
                    "LEFT JOIN Assignment a ON c.categoryID = a.categoryID " +
                    "WHERE c.classID = " + activeClassID +
                    " ORDER BY a.assignmentName, c.categoryName";

            ResultSet resultSet = sqlStatement.executeQuery(sql);

            // Map to store assignments grouped by category
            Map<String, Map<String, Integer>> assignmentsByCategory = new LinkedHashMap<>();

            // Iterate over the result set to populate the map
            while (resultSet.next()) {
                String categoryName = resultSet.getString("categoryName");
                String assignmentName = resultSet.getString("assignmentName");
                int pointValue = resultSet.getInt("pointValue");

                // If category doesn't exist in the map, add it
                assignmentsByCategory.putIfAbsent(categoryName, new HashMap<>());

                // Add assignment to the category
                assignmentsByCategory.get(categoryName).put(assignmentName, pointValue);
            }

            System.out.println("Assignments grouped by category:");
            for (Map.Entry<String, Map<String, Integer>> categoryEntry : assignmentsByCategory.entrySet()) {
                System.out.println("Category: " + categoryEntry.getKey());
                Map<String, Integer> assignments = categoryEntry.getValue();
                for (Map.Entry<String, Integer> assignmentEntry : assignments.entrySet()) {
                    System.out.println("\t" + assignmentEntry.getKey() + " - Point Value: " + assignmentEntry.getValue());
                }
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve assignments");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Adds an assignment to the current class.
     * 
     * This method checks if a class is currently selected. If not, it prints an error message.
     * It then retrieves the category ID for the provided category name.
     * If the category does not exist in the current class, it prints an error message.
     * Otherwise, it inserts the new assignment into the database with the provided name, description, point value, 
     * and the retrieved category ID and active class ID.
     * 
     * @param name        the name of the assignment
     * @param category    the category of the assignment
     * @param description the description of the assignment
     * @param string      the point value of the assignment
     */
    public static void addAssignment(String name, String category, String description, String string) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            int categoryID = getCategoryIDByName(sqlStatement, category);

            if (categoryID == -1) {
                System.out.println("Category '" + category + "' does not exist in the current class.");
                return;
            }

            // Insert the new assignment into the database with active class ID
            String insertAssignmentSql = "INSERT INTO Assignment (assignmentName, assignmentDescription, pointValue, categoryID, classID) " +
                                         "VALUES ('" + name + "', '" + description + "', " + string + ", " + categoryID + ", " + activeClassID + ")";
            sqlStatement.executeUpdate(insertAssignmentSql);

            System.out.println("Assignment added successfully.");

        } catch (SQLException sqlException) {
            System.out.println("Failed to add assignment");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Adds a student to the current class or updates their information if they already exist.
     * 
     * This method checks if a class is currently selected. If not, it prints an error message.
     * It then checks if the student already exists in the database based on the provided username.
     * If the student exists, it compares the provided school ID, last name, and first name with the stored values.
     * If any of these values differ, it updates the student's information in the database.
     * Finally, it enrolls the student in the current class.
     * 
     * If the student does not exist, it inserts a new record for the student in the database and enrolls them in the current class.
     * 
     * @param username  the username of the student
     * @param schoolID  the school ID of the student
     * @param lastName  the last name of the student
     * @param firstName the first name of the student
     */
    public static void addStudent(String username, String schoolID, String lastName, String firstName) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Check if the student already exists
            String checkStudentSql = "SELECT * FROM Student WHERE username = '" + username + "'";
            ResultSet resultSet = sqlStatement.executeQuery(checkStudentSql);

            if (resultSet.next()) {
                // Student exists, update if necessary and enroll in the class
                String storedSchoolID = resultSet.getString("schoolID");
                String storedLastName = resultSet.getString("lastName");
                String storedFirstName = resultSet.getString("firstName");
                int storedStudentID = resultSet.getInt("studentID");

                if (storedSchoolID != schoolID) {
                    System.out.println("Error: School ID provided does not match the stored value for the student.");
                    return;
                }

                if (!storedLastName.equals(lastName) || !storedFirstName.equals(firstName)) {
                    System.out.println("Warning: Updating student name for username: " + username);
                    // Update student name
                    String updateStudentSql = "UPDATE Student SET lastName = '" + lastName + "', firstName = '" + firstName + "' WHERE username = '" + username + "'";
                    sqlStatement.executeUpdate(updateStudentSql);
                }

                // Enroll student in the class
                enrollStudent(sqlStatement, storedStudentID);
            } else {
                // Student doesn't exist, create and enroll in the class
                String insertStudentSql = "INSERT INTO Student (schoolID, username, lastName, firstName) VALUES (" + schoolID + ", '" + username + "', '" + lastName + "', '" + firstName + "')";
                sqlStatement.executeUpdate(insertStudentSql, Statement.RETURN_GENERATED_KEYS);
                ResultSet generatedKeys = sqlStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newStudentID = generatedKeys.getInt(1);
                    enrollStudent(sqlStatement, newStudentID);
                }
            }

            System.out.println("Student added and enrolled successfully.");

        } catch (SQLException sqlException) {
            System.out.println("Failed to add or enroll student");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Adds a student to the current class.
     * This method checks if a class is currently selected. If not, it prints an error message.
     * Then, it checks if the student exists in the database. If the student exists, it verifies if the student is already enrolled in the current class.
     * If the student is not enrolled, it enrolls the student in the class.
     * If successful, it prints a success message; otherwise, it prints an error message.
     * 
     * @param username the username of the student to be added to the class
     */
    public static void addStudent(String username) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Check if the student exists
            String checkStudentSql = "SELECT * FROM Student WHERE username = '" + username + "'";
            ResultSet resultSet = sqlStatement.executeQuery(checkStudentSql);

            if (resultSet.next()) {
                int studentID = resultSet.getInt("studentID");

                // Check if the student is already enrolled in the current class
                String checkEnrollmentSql = "SELECT * FROM Enrollment WHERE studentID = " + studentID + " AND classID = " + activeClassID;
                ResultSet enrollmentResultSet = sqlStatement.executeQuery(checkEnrollmentSql);

                if (enrollmentResultSet.next()) {
                    System.out.println("Error: Student with username '" + username + "' is already enrolled in the current class.");
                } else {
                    // Enroll student in the class
                    enrollStudent(sqlStatement, studentID);
                    System.out.println("Student enrolled successfully.");
                }
            } else {
                System.out.println("Error: Student with username '" + username + "' does not exist.");
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to enroll student");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Grades an assignment for a specified student.
     * This method first checks if the assignment exists and retrieves its point value.
     * Then, it verifies if the student exists and converts the provided grade string to an integer.
     * If the provided grade exceeds the assignment's point value, it issues a warning.
     * The method then inserts or updates the grade for the student and assignment in the database.
     * If successful, it prints a success message; otherwise, it prints a failure message.
     * If the grade format is invalid, it prints an error message.
     * 
     * @param assignmentName the name of the assignment to be graded
     * @param username the username of the student for whom the grade is assigned
     * @param grade the grade to be assigned to the assignment
     */
    public static void gradeAssignment(String assignmentName, String username, String grade) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Get database connection
            connection = Database.getDatabaseConnection();

            // Check if the assignment exists and get its point value
            String getAssignmentPointValueQuery = "SELECT pointValue FROM Assignment WHERE assignmentName = ?";
            preparedStatement = connection.prepareStatement(getAssignmentPointValueQuery);
            preparedStatement.setString(1, assignmentName);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int assignmentPointValue = resultSet.getInt("pointValue");

                // Check if the student exists
                String getStudentIDQuery = "SELECT studentID FROM Student WHERE username = ?";
                preparedStatement = connection.prepareStatement(getStudentIDQuery);
                preparedStatement.setString(1, username);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int studentID = resultSet.getInt("studentID");

                    // Convert grade string to integer
                    int gradeValue = Integer.parseInt(grade);

                    // Check if the grade exceeds the point value configured for the assignment
                    if (gradeValue > assignmentPointValue) {
                        System.out.println("Warning: The provided grade exceeds the point value configured for the assignment (" + assignmentPointValue + " points).");
                    }

                    // Insert or update the grade for the student and assignment in the Assigned table
                    String insertOrUpdateGradeQuery = "INSERT INTO Graded (assignmentID, studentID, grade) " +
                            "VALUES ((SELECT assignmentID FROM Assignment WHERE assignmentName = ?), ?, ?) " +
                            "ON DUPLICATE KEY UPDATE grade = ?";
                    preparedStatement = connection.prepareStatement(insertOrUpdateGradeQuery);
                    preparedStatement.setString(1, assignmentName);
                    preparedStatement.setInt(2, studentID);
                    preparedStatement.setInt(3, gradeValue);
                    preparedStatement.setInt(4, gradeValue);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Grade assigned successfully.");
                    } else {
                        System.out.println("Failed to assign grade.");
                    }
                } else {
                    System.out.println("Error: Student with username '" + username + "' does not exist.");
                }
            } else {
                System.out.println("Error: Assignment '" + assignmentName + "' does not exist.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid grade format. Grade must be a valid integer.");
        } catch (SQLException sqlException) {
            System.out.println("Failed to grade assignment.");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves and displays the list of students enrolled in the currently active class whose username,
     * last name, or first name contains the specified search string.
     * If no class is currently selected, it prints a message indicating so and returns.
     * This method queries the database to retrieve information about students matching the search criteria.
     * It then prints the student ID, username, last name, and first name for each matching student.
     * 
     * @param searchString the search string to be used to filter the students' information
     */
    public static void showStudents(String searchString) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "SELECT studentID, username, lastName, firstName FROM Student " +
                         "WHERE (LOWER(username) LIKE '%" + searchString.toLowerCase() + "%' " +
                         "OR LOWER(lastName) LIKE '%" + searchString.toLowerCase() + "%' " +
                         "OR LOWER(firstName) LIKE '%" + searchString.toLowerCase() + "%')";

            ResultSet resultSet = sqlStatement.executeQuery(sql);

            System.out.println("Students with '" + searchString + "' in their name or username:");
            System.out.println("Student ID | Username | Last Name | First Name");

            while (resultSet.next()) {
                int studentID = resultSet.getInt("studentID");
                String username = resultSet.getString("username");
                String lastName = resultSet.getString("lastName");
                String firstName = resultSet.getString("firstName");

                System.out.println(studentID + " | " + username + " | " + lastName + " | " + firstName);
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve students");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Retrieves and displays the list of students enrolled in the currently active class.
     * If no class is currently selected, it prints a message indicating so and returns.
     * This method queries the database to retrieve information about students enrolled in the current class.
     * It then prints the student ID, username, last name, and first name for each student.
     */
    public static void showStudents() {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            String sql = "SELECT s.studentID, s.username, s.lastName, s.firstName " +
                         "FROM Student s " +
                         "INNER JOIN Enrollment e ON s.studentID = e.studentID " +
                         "WHERE e.classID = " + activeClassID;

            ResultSet resultSet = sqlStatement.executeQuery(sql);

            System.out.println("Students in the current class:");
            System.out.println("Student ID | Username | Last Name | First Name");

            while (resultSet.next()) {
                int studentID = resultSet.getInt("studentID");
                String username = resultSet.getString("username");
                String lastName = resultSet.getString("lastName");
                String firstName = resultSet.getString("firstName");

                System.out.println(studentID + " | " + username + " | " + lastName + " | " + firstName);
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve students");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    
    /**
     * Retrieves and displays the grades for a student in the currently active class.
     * If no class is currently selected or the student is not found, it prints an appropriate message and returns.
     * This method queries the database to retrieve assignments and grades for the specified student in the current class.
     * It then calculates the category scores based on the earned points and total points for each category,
     * and computes the overall score as a weighted average of the category scores.
     * Additionally, it calls the {@code studentPerformance()} method to display the student's attempted grade.
     * 
     * @param username the username of the student whose grades are to be retrieved and displayed
     */
    public static void studentGrades(String username) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Retrieve student ID based on username
            String studentQuery = "SELECT studentID FROM Student WHERE username = '" + username + "'";
            ResultSet studentResult = sqlStatement.executeQuery(studentQuery);
            if (!studentResult.next()) {
                System.out.println("Student with username " + username + " not found.");
                return;
            }
            int studentID = studentResult.getInt("studentID");

            // Retrieve all assignments and grades for the student in the current class
            String sql = "SELECT a.assignmentID, a.assignmentName, c.categoryName, g.grade, " +
                         "c.weight, a.pointValue " +
                         "FROM Assignment a " +
                         "INNER JOIN Category c ON a.categoryID = c.categoryID " +
                         "LEFT JOIN Graded g ON a.assignmentID = g.assignmentID AND g.studentID = " + studentID + " " +
                         "WHERE a.classID = " + activeClassID;

            ResultSet resultSet = sqlStatement.executeQuery(sql);

            System.out.println("Grades for " + username + " in the current class:");
            System.out.println();

            double attemptedScore = 0.0;
            double attemptedWeight = 0.0;

            // Map to store total points and earned points for each category
            Map<String, Double> categoryTotalPoints = new HashMap<>();
            Map<String, Double> categoryEarnedPoints = new HashMap<>();
            Map<String, List<String>> categoryAssignments = new HashMap<>();

            while (resultSet.next()) {
                String assignmentName = resultSet.getString("assignmentName");
                String categoryName = resultSet.getString("categoryName");
                int grade = resultSet.getInt("grade");
                int pointValue = resultSet.getInt("pointValue");

                // Calculate earned points
                double earnedPoints = (grade == -1) ? 0.0 : (double) grade;

                // Update category total and earned points
                categoryTotalPoints.put(categoryName, categoryTotalPoints.getOrDefault(categoryName, 0.0) + pointValue);
                categoryEarnedPoints.put(categoryName, categoryEarnedPoints.getOrDefault(categoryName, 0.0) + earnedPoints);
                
                // Add assignment to categoryAssignments map
                String assignmentEntry = assignmentName + " - " + (grade == -1 ? "Not graded" : grade);
                categoryAssignments.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(assignmentEntry);
            }

            // Calculate category scores and total score
            for (String category : categoryTotalPoints.keySet()) {
                double totalPoints = categoryTotalPoints.get(category);
                double earnedPoints = categoryEarnedPoints.getOrDefault(category, 0.0);
                List<String> assignments = categoryAssignments.getOrDefault(category, new ArrayList<>());

                double categoryScore = (totalPoints == 0) ? 0.0 : (earnedPoints / totalPoints) * 100.0;
                double categoryWeight = getCategoryWeight(connection, category); // Function to retrieve category weight

                System.out.println("Category: " + category);
                for (String assignment : assignments) {
                    System.out.println(assignment);
                }
                System.out.println("Category Score: " + categoryScore);
                System.out.println();
                
                // If the category has a grade, add its weight to calculate the attempted score
                if (categoryEarnedPoints.containsKey(category)) {
                    attemptedWeight += categoryWeight;
                    attemptedScore += (categoryScore * categoryWeight);
                }
            }

            // Calculate the overall score based on attempted weight
            double finalScore = (attemptedWeight == 0) ? 0.0 : (attemptedScore / attemptedWeight);
            
            studentPerformance(username);
            System.out.println("Overall Score: " + finalScore + "%");

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve student grades");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Calculates and displays the overall grade for a student in the currently active class.
     * If no class is currently selected or the student is not found, it prints an appropriate message and returns.
     * This method queries the database to retrieve assignments and grades for the specified student in the current class.
     * It then calculates the category scores based on the earned points and total points for each category,
     * and computes the overall grade as a weighted average of the category scores.
     * 
     * @param username the username of the student whose overall grade is to be calculated
     */
    public static void overallGrade(String username) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Retrieve student ID based on username
            String studentQuery = "SELECT studentID FROM Student WHERE username = '" + username + "'";
            ResultSet studentResult = sqlStatement.executeQuery(studentQuery);
            if (!studentResult.next()) {
                System.out.println("Student with username " + username + " not found.");
                return;
            }
            int studentID = studentResult.getInt("studentID");

            // Retrieve all assignments and grades for the student in the current class
            String sql = "SELECT a.assignmentID, a.assignmentName, c.categoryName, g.grade, " +
                         "c.weight, a.pointValue " +
                         "FROM Assignment a " +
                         "INNER JOIN Category c ON a.categoryID = c.categoryID " +
                         "LEFT JOIN Graded g ON a.assignmentID = g.assignmentID AND g.studentID = " + studentID + " " +
                         "WHERE a.classID = " + activeClassID;

            ResultSet resultSet = sqlStatement.executeQuery(sql);
            
            double attemptedScore = 0.0;
            double attemptedWeight = 0.0;

            // Map to store total points and earned points for each category
            Map<String, Double> categoryTotalPoints = new HashMap<>();
            Map<String, Double> categoryEarnedPoints = new HashMap<>();
            Map<String, List<String>> categoryAssignments = new HashMap<>();

            while (resultSet.next()) {
                String assignmentName = resultSet.getString("assignmentName");
                String categoryName = resultSet.getString("categoryName");
                int grade = resultSet.getInt("grade");
                int pointValue = resultSet.getInt("pointValue");

                // Calculate earned points
                double earnedPoints = (grade == -1) ? 0.0 : (double) grade;

                // Update category total and earned points
                categoryTotalPoints.put(categoryName, categoryTotalPoints.getOrDefault(categoryName, 0.0) + pointValue);
                categoryEarnedPoints.put(categoryName, categoryEarnedPoints.getOrDefault(categoryName, 0.0) + earnedPoints);
                
                // Add assignment to categoryAssignments map
                String assignmentEntry = assignmentName + " - " + (grade == -1 ? "Not graded" : grade);
                categoryAssignments.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(assignmentEntry);
            }

            // Calculate category scores and total score
            for (String category : categoryTotalPoints.keySet()) {
                double totalPoints = categoryTotalPoints.get(category);
                double earnedPoints = categoryEarnedPoints.getOrDefault(category, 0.0);

                double categoryScore = (totalPoints == 0) ? 0.0 : (earnedPoints / totalPoints) * 100.0;
                double categoryWeight = getCategoryWeight(connection, category); // Function to retrieve category weight
                
                // Add category score weighted by its weight to the total score
                
                // If the category has a grade, add its weight to calculate the attempted score
                if (categoryEarnedPoints.containsKey(category)) {
                    attemptedWeight += categoryWeight;
                    attemptedScore += (categoryScore * categoryWeight);
                }
            }

            // Calculate the overall score based on attempted weight
            double finalScore = (attemptedWeight == 0) ? 0.0 : (attemptedScore / attemptedWeight);
            
            System.out.println("Total Score: " + finalScore + "%");

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve student grades");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Calculates and displays the attempted grade for a student in the currently active class.
     * If no class is currently selected or the student is not found, it prints an appropriate message and returns.
     * This method queries the database to retrieve graded assignments for the specified student in the current class.
     * It then calculates the weighted performance of the student based on the assignment grades and category weights,
     * and finally computes the overall attempted grade as a percentage.
     * 
     * @param username the username of the student whose attempted grade is to be calculated
     */
    public static void studentPerformance(String username) {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Retrieve student ID based on username
            String studentQuery = "SELECT studentID FROM Student WHERE username = '" + username + "'";
            ResultSet studentResult = sqlStatement.executeQuery(studentQuery);
            if (!studentResult.next()) {
                System.out.println("Student with username " + username + " not found.");
                return;
            }
            int studentID = studentResult.getInt("studentID");

            // Retrieve all graded assignments for the student in the current class
            String sql = "SELECT a.pointValue, c.weight, g.grade " +
                         "FROM Assignment a " +
                         "INNER JOIN Category c ON a.categoryID = c.categoryID " +
                         "LEFT JOIN Graded g ON a.assignmentID = g.assignmentID AND g.studentID = " + studentID + " " +
                         "WHERE a.classID = " + activeClassID + " AND g.grade IS NOT NULL";

            ResultSet resultSet = sqlStatement.executeQuery(sql);

            double totalWeight = 0.0; // Total weight of categories with assignments
            double weightedPerformance = 0.0;

            while (resultSet.next()) {
                double pointValue = resultSet.getDouble("pointValue");
                double weight = resultSet.getDouble("weight");
                int grade = resultSet.getInt("grade");

                // Calculate performance within the category
                double performance = (grade / pointValue);

                // Multiply performance by category weight
                weightedPerformance += performance * weight;

                // Increment total weight of categories with assignments
                totalWeight += weight;
            }

            // Calculate the student's overall performance
            double overallPerformance = (weightedPerformance / totalWeight) * 100;
            
            if (Double.isNaN(overallPerformance)) {
                overallPerformance = 0.0;
            }

            System.out.println("Attempted Grade is: " + overallPerformance + "%");

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve attempted grade");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    /**
     * Displays the grade book for the currently active class.
     * If no class is currently selected, it prints a message indicating so and returns.
     * This method queries the database to retrieve students enrolled in the current class,
     * and then iterates over the result set to display each student's information along with their performance.
     * 
     */
    public static void gradeBook() {
        if (activeClassID == -1) {
            System.out.println("No class is currently selected. Please select a class first.");
            return;
        }

        Connection connection = null;
        Statement sqlStatement = null;

        try {
            connection = Database.getDatabaseConnection();
            sqlStatement = connection.createStatement();

            // Query to retrieve students enrolled in the current class
            String sql = "SELECT s.studentID, s.username, s.lastName, s.firstName " +
                         "FROM Student s " +
                         "JOIN Enrollment e ON s.studentID = e.studentID " +
                         "WHERE e.classID = " + activeClassID;

            ResultSet resultSet = sqlStatement.executeQuery(sql);

            // Display header for grade book
            System.out.println("Grade Book for Class (ID: " + activeClassID + "):");
            System.out.println("--------------------------------------------------");

            // Iterate over the result set to display each student's information
            while (resultSet.next()) {
                int studentID = resultSet.getInt("studentID");
                String username = resultSet.getString("username");
                String lastName = resultSet.getString("lastName");
                String firstName = resultSet.getString("firstName");

                // Display student information
                System.out.println("Student ID: " + studentID);
                System.out.println("Username: " + username);
                System.out.println("Name: " + firstName + " " + lastName);

                // Allow the user to view the student's performance by calling studentPerformance() function
                studentPerformance(username);
                overallGrade(username);
                System.out.println("--------------------------------------------------");
            }

        } catch (SQLException sqlException) {
            System.out.println("Failed to retrieve grade book information");
            System.out.println(sqlException.getMessage());
        } finally {
            // Close resources
            try {
                if (sqlStatement != null)
                    sqlStatement.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    /**
     * Enrolls a student into the currently active class.
     * This method inserts a new enrollment record into the "Enrollment" table of the database
     * for the specified student and the active class.
     * 
     * @param sqlStatement the SQL statement to execute the enrollment query
     * @param studentID    the ID of the student to enroll
     * @throws SQLException if there is an error executing the SQL query
     */
    private static void enrollStudent(Statement sqlStatement, int studentID) throws SQLException {
        String enrollStudentSql = "INSERT INTO Enrollment (classID, studentID) VALUES (" + activeClassID + ", " + studentID + ")";
        sqlStatement.executeUpdate(enrollStudentSql);
    }

    /**
     * Retrieves the weight of a category from the database.
     * This method queries the database to retrieve the weight of the specified category
     * for the currently active class.
     * 
     * @param connection the database connection
     * @param category   the name of the category
     * @return the weight of the category
     * @throws SQLException if there is an error accessing the database or executing the SQL query
     */
    private static double getCategoryWeight(Connection connection, String category) throws SQLException {
        double weight = 0.0;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();

            // Query to retrieve the weight of the category
            String sql = "SELECT weight FROM Category WHERE categoryName = '" + category + "'";
            resultSet = statement.executeQuery(sql);

            // Check if the result set has data
            if (resultSet.next()) {
                weight = resultSet.getDouble("weight");
            } else {
                System.out.println("Category weight not found for category: " + category);
            }
        } finally {
            // Close resources
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }

        return weight;
    }

    /**
     * Retrieves the ID of a category by its name for the currently active class.
     * This method queries the database to retrieve the ID of the specified category
     * for the currently active class.
     * 
     * @param sqlStatement  the SQL statement to execute the query
     * @param categoryName  the name of the category
     * @return the ID of the category
     * @throws SQLException if there is an error executing the SQL query
     */
    private static int getCategoryIDByName(Statement sqlStatement, String categoryName) throws SQLException {
        int categoryID = -1;
        String getCategoryIDSql = "SELECT categoryID FROM Category WHERE categoryName = '" + categoryName + "' AND classID = " + activeClassID;
        ResultSet resultSet = sqlStatement.executeQuery(getCategoryIDSql);
        if (resultSet.next()) {
            categoryID = resultSet.getInt("categoryID");
        }
        return categoryID;
    }

    /**
     * Builds the SQL query to activate a class based on the provided parameters.
     * This method constructs the SQL query dynamically based on the number of parameters provided.
     * 
     * @param params an array of strings representing the parameters to match against the database
     * @return the constructed SQL query
     */
    private static String buildActivationQuery(String... params) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT classID, courseNumber, term, section, classDescription ");
        sqlBuilder.append("FROM Class ");

        if (params.length == 1) {
            sqlBuilder.append("WHERE courseNumber = '").append(params[0]).append("'");
        } else if (params.length == 2) {
            sqlBuilder.append("WHERE courseNumber = '").append(params[0]).append("' AND term = '").append(params[1]).append("'");
        } else if (params.length == 3) {
            sqlBuilder.append("WHERE courseNumber = '").append(params[0]).append("' AND term = '").append(params[1]).append("' AND section = '").append(params[2]).append("'");
        } else {
            // If incorrect number of parameters provided
            return "";
        }

        return sqlBuilder.toString();
    }
    
    

    /***
     * Splits a string up by spaces. Spaces are ignored when wrapped in quotes.
     * 
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
    
    /**
     * Main method for function, processes commands and then redirects to more specific
     * functionality within a specific method.
     */
    public static void main(String[] args) {
        System.out.println("Welcome to the School Management System");
        System.out.println("-".repeat(80));
        Scanner scan = new Scanner(System.in);
        String command = "";

        do {
        	System.out.print(currentClass);
            System.out.print("> ");
            command = scan.nextLine();
            ;
            List<String> commandArguments = parseArguments(command);
            command = commandArguments.get(0);
            commandArguments.remove(0);

            if (command.equals("help")) {
            	System.out.println("Available commands:");
                System.out.println("- test connection: Test the database connection.");
                System.out.println("- list-classes: List all available classes.");
                System.out.println("- create-class [name] [semester] [section] [description]: Create a new class with the given details.");
                System.out.println("- select-class [class_id]: Activate a class by its ID.");
                System.out.println("- deselect-class: Deselect the currently active class.");
                System.out.println("- show-class: Display information about the currently active class.");
                System.out.println("- show-categories: Display all categories in the active class.");
                System.out.println("- add-category [name] [weight]: Add a new category to the active class.");
                System.out.println("- show-assignment: Display all assignments in the active class.");
                System.out.println("- add-assignment [name] [category] [description] [points value]: Add a new assignment to the active class.");
                System.out.println("- add-student [username] [student id] [last name] [first name]: Add a new student to the active class.");
                System.out.println("- show-students [class_id]: Display all students in the active class.");
                System.out.println("- grade [assignment_id] [username] [grade]: Grade an assignment for a specific student.");
                System.out.println("- student-grades [username]: Display grades for a specific student.");
                System.out.println("- gradebook: Display the gradebook for the active class.");
                System.out.println("- quit or exit: Exit the program.");
            } else if (command.equals("test") && commandArguments.get(0).equals("connection")) {
                Database.testConnection();
            } else if (command.equals("list-classes")) {
            	listAllClasses();
            } else if (command.equals("create-class")) {
                    createNewClass(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2), commandArguments.get(3));
            } else if (command.equals("select-class")) {
            	if(commandArguments.get(0) != null) {
                	activateClass(commandArguments.get(0));
            	} else if (commandArguments.get(1) != null) {
            		activateClass(commandArguments.get(0), commandArguments.get(1));
            	} else if (commandArguments.get(2) != null) {
            		activateClass(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2));
            	}
            	
            } else if (command.equals("show-class")){
            	showClass();
            } else if (command.equals("deselect-class")){
            	if (activeClassID == -1) {
            		System.out.println("no class currently selected");
            	} else {
            	activeClassID = -1;
            	currentClass = "";
            	System.out.println("class has been de-selected");
            	}
            } else if (command.equals("show-categories")) {
            	showCategories();
            } else if(command.equals("add-category")) {
            	addCategory(commandArguments.get(0), commandArguments.get(1));
            } else if (command.equals("show-assignment")) {
            	showAssignments();
            } else if (command.equals("add-assignment")) {
            	addAssignment(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2), commandArguments.get(3));
            } else if (command.equals("add-student")) {
            	if (command.equals("add-student")) {
            	    if (commandArguments.size() >= 4) {
            	        addStudent(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2), commandArguments.get(3));
            	    } else if (commandArguments.size() == 1) {
            	        addStudent(commandArguments.get(0));
            	    } else {
            	        System.out.println("Invalid number of arguments for add-student command.");
            	    }
            	}
            } else if (command.equals("show-students")) {
            	if(commandArguments.size() == 1) {
            		showStudents(commandArguments.get(0));
            	} else {
            	showStudents();
            	}
            } else if (command.equals("grade")) {
            	if(commandArguments.size() == 3) {
            		gradeAssignment(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2));
            	}
            } else if (command.equals("student-grades")) {
            	if(commandArguments.size() == 1) {
            		studentGrades(commandArguments.get(0));
            	}
            } else if (command.equals("gradebook")) {
            	gradeBook();
            } else if (!(command.equals("quit") || command.equals("exit"))) {
                System.out.println(command);
                System.out.println("Command not found. Enter 'help' for list of commands");
            }
            System.out.println("-".repeat(80));
        } while (!(command.equals("quit") || command.equals("exit")));
        System.out.println("Bye!");
    }
}

