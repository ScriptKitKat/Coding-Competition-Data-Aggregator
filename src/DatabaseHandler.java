import java.sql.*;

/**
 * DatabaseHandler class is responsible for handling the data in relation to the SQL database. Database interacts with the driver class/main method.
 */
public class DatabaseHandler {
    private Connection connection;

    /**
     * Constructs a DatabaseHandler object and establishes a connection to the database.
     * @param dbURL The URL of the database.
     */
    public DatabaseHandler(String dbURL) 
    {
        try {
            connection = DriverManager.getConnection(dbURL);
            initializeTables();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Returns the database connection.
     * @return The database connection.
     */
    public Connection getConnection() 
    {
        return connection;
    }

    /**
     * Initializes the necessary tables in the database if they do not already exist.
     * @throws SQLException if a database access error occurs.
     */
    private void initializeTables() throws SQLException 
    {
        String studentTable = """
                CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstname TEXT NOT NULL,
                lastname TEXT NOT NULL,
                email TEXT NOT NULL,
                level BIT NOT NULL
                );""";
        String competitionTable = """
                CREATE TABLE IF NOT EXISTS competitions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
                );""";
        String resultsTable = """
                CREATE TABLE IF NOT EXISTS results (
                student_id INTEGER,
                competition_id INTEGER,
                problems_solved INTEGER,
                placement INTEGER
                );""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(studentTable);
            stmt.execute(competitionTable);
            stmt.execute(resultsTable);
        }
    }

    /**
     * Adds a student and their competition result to the database.
     * @param teamType The type of the team (e.g., Novice, Advanced).
     * @param name The name of the student.
     * @param email The email of the first member.
     * @param problemsSolved The number of problems solved by the student.
     * @param placement The placement of the student in the competition.
     * @param comp The name of the competition.
     * @throws SQLException if a database access error occurs.
     */
    public void addStudentAndCompetition(String teamType, String name, String email, int problemsSolved, int placement, String comp) 
    {
        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is not established or is closed.");
            }

            connection.setAutoCommit(false);
            int student1Id = addStudent(name, email, teamType);
            int competitionId = addCompetition(comp);

            addResult(student1Id, competitionId, problemsSolved, placement);

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                System.out.println("Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Adds multiple students and their competition results to the database.
     * @param teamType The type of the team (e.g., Novice, Advanced).
     * @param member1 The name of the first member.
     * @param email1 The email of the first member.
     * @param member2 The name of the second member.
     * @param email2 The email of the second member.
     * @param member3 The name of the third member.
     * @param email3 The email of the third member.
     * @param problemsSolved The number of problems solved by the students.
     * @param placement The placement of the students in the competition.
     * @param comp The name of the competition.
     * @throws SQLException if a database access error occurs.
     */
    public void addStudentAndCompetition(String teamType, String member1, String email1, String member2, String email2, String member3, String email3, int problemsSolved, int placement, String comp) 
    {
        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is not established or is closed.");
            }

            connection.setAutoCommit(false);
            int student1Id = addStudent(member1, email1, teamType);
            int competitionId = addCompetition(comp);

            addResult(student1Id, competitionId, problemsSolved, placement);

            if (!member2.isEmpty() && !email2.isEmpty()) {
                int student2Id = addStudent(member2, email2, teamType);
                addResult(student2Id, competitionId, problemsSolved, placement);
            }

            if (!member3.isEmpty() && !email3.isEmpty()) {
                int student3Id = addStudent(member3, email3, teamType);
                addResult(student3Id, competitionId, problemsSolved, placement);
            }

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                System.out.println("Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Adds a student to the database.
     * @param name The name of the student.
     * @param email The email of the student.
     * @param teamType The type of the team (e.g., Novice, Advanced).
     * @return The ID of the newly added student, or -1 if the student could not be added.
     * @throws SQLException if a database access error occurs.
     */
    public int addStudent(String name, String email, String teamType) throws SQLException
    {
        String[] nameParts = name.split(" ", 2); // Split into first and last name
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Name must include both first and last name.");
        }

        String firstName = nameParts[0];
        String lastName = nameParts[1];

        // Check if the student already exists
        String selectQuery = "SELECT id FROM students WHERE firstname = ? AND lastname = ? AND email = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, firstName);
            selectStmt.setString(2, lastName);
            selectStmt.setString(3, email);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    // Student already exists, return their ID
                    return rs.getInt("id");
                }
            }
        }

        // If not exists, insert the student
        String insertQuery = "INSERT INTO students (firstname, lastname, email, level) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, firstName);
            insertStmt.setString(2, lastName);
            insertStmt.setString(3, email);
            insertStmt.setString(4, teamType);
            insertStmt.executeUpdate();

            // Retrieve and return the newly generated ID
            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1; // No ID was retrieved
    }

    /**
     * Adds a competition to the database.
     * @param name The name of the competition.
     * @return The ID of the newly added competition, or -1 if the competition could not be added.
     * @throws SQLException if a database access error occurs.
     */
    public int addCompetition(String name) throws SQLException
    {

        // Check if competition already exists
        String selectQuery = "SELECT id FROM competitions WHERE name = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, name);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    // Competition already exists, return their ID
                    return rs.getInt("id");
                }
            }
        }

        // If not exists, insert the competition
        String query = "INSERT OR IGNORE INTO competitions (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }

        return -1; // No ID was retrieved
    }

    /**
     * Adds a result to the results table.
     * @param studentId The ID of the student.
     * @param competitionId The ID of the competition.
     * @param problemsSolved The number of problems solved by the student.
     * @param placement The placement of the student in the competition.
     * @throws SQLException if a database access error occurs.
     */
    private void addResult(int studentId, int competitionId, int problemsSolved, int placement) throws SQLException
    {
        String query = "INSERT INTO results (student_id, competition_id, problems_solved, placement) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, competitionId);
            stmt.setInt(3, problemsSolved);
            stmt.setInt(4, placement);
            stmt.executeUpdate();
        }
    }

    /**
     * Removes a student from the database.
     * @param name The name of the student.
     * @throws SQLException if a database access error occurs.
     */
    public void removeStudent(String name) throws SQLException
    {
        String[] nameParts = name.split(" ", 2); // Split into first and last name (at most 2 parts)
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Name must include both first and last name.");
        }
    
        String firstName = nameParts[0];
        String lastName = nameParts[1];
    
        // Find the student ID
        String selectQuery = "SELECT id FROM students WHERE firstname = ? AND lastname = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, firstName);
            selectStmt.setString(2, lastName);
    
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int studentId = rs.getInt("id");
    
                    // Remove from results table
                    String deleteResultsQuery = "DELETE FROM results WHERE student_id = ?";
                    try (PreparedStatement deleteResultsStmt = connection.prepareStatement(deleteResultsQuery)) {
                        deleteResultsStmt.setInt(1, studentId);
                        deleteResultsStmt.executeUpdate();
                    }
    
                    // Remove from students table
                    String deleteStudentQuery = "DELETE FROM students WHERE id = ?";
                    try (PreparedStatement deleteStudentStmt = connection.prepareStatement(deleteStudentQuery)) {
                        deleteStudentStmt.setInt(1, studentId);
                        deleteStudentStmt.executeUpdate();
                    }
                } else {
                    System.out.println("Student not found: " + name);
                }
            }
        }
    }

    /**
     * Removes a competition from the database.
     * @param competitionName The name of the competition.
     * @throws SQLException if a database access error occurs.
     */
    public void removeCompetition(String competitionName) throws SQLException
    {
        // Find the competition ID
        String selectQuery = "SELECT id FROM competitions WHERE name = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, competitionName);
    
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int competitionId = rs.getInt("id");
    
                    // Remove from results table
                    String deleteResultsQuery = "DELETE FROM results WHERE competition_id = ?";
                    try (PreparedStatement deleteResultsStmt = connection.prepareStatement(deleteResultsQuery)) {
                        deleteResultsStmt.setInt(1, competitionId);
                        deleteResultsStmt.executeUpdate();
                    }
    
                    // Remove from competitions table
                    String deleteCompetitionQuery = "DELETE FROM competitions WHERE id = ?";
                    try (PreparedStatement deleteCompetitionStmt = connection.prepareStatement(deleteCompetitionQuery)) {
                        deleteCompetitionStmt.setInt(1, competitionId);
                        deleteCompetitionStmt.executeUpdate();
                    }
                } else {
                    System.out.println("Competition not found: " + competitionName);
                }
            }
        }
    }

    /**
     * Checks if the database is empty.
     * @return true if the database is empty, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isDatabaseEmpty() throws SQLException
    {
        String sql = "SELECT COUNT(*) AS count FROM students";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        return rs.next() && rs.getInt("count") == 0;
    }

    /**
     * Prints all students in the database.
     * @throws SQLException if a database access error occurs.
     */
    public void printAllStudents() throws SQLException
    {
        String query = "SELECT * FROM students";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("ID\tFirst Name\tLast Name\tEmail\tLevel");
            System.out.println("--------------------------------------------------");

            // Iterate over each row in the students table
            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String email = rs.getString("email");
                String level = rs.getString("level");

                // Print each row
                System.out.printf("%d\t%s\t%s\t%s\t%s%n", id, firstName, lastName, email, level);
            }
        }
    }

    /**
     * Prints all competitions in the database.
     * @throws SQLException if a database access error occurs.
     */
    public void printAllCompetitions() throws SQLException
    {
        String query = "SELECT * FROM competitions";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("ID\tName");
            System.out.println("--------------------------------------------------");

            // Iterate over each row in the competitions table
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");

                // Print each row
                System.out.printf("%d\t%s", id, name);
            }
        }
    }

    /**
     * Prints all results in the database.
     * @throws SQLException if a database access error occurs.
     */
    public void printAllResult() throws SQLException
    {
        String query = "SELECT * FROM results";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Student ID\tCompetition ID\t\tEmail\tLevel");
            System.out.println("--------------------------------------------------");

            // Iterate over each row in the result table
            while (rs.next()) {
                int student_id = rs.getInt("student_id");
                int competition_id = rs.getInt("competition_id");
                int problems_solved = rs.getInt("problems_solved");
                int placement = rs.getInt("placement");

                // Print each row
                System.out.printf("%d\t%d\t%d\t%d%n", student_id, competition_id, problems_solved, placement);
            }
        }
    }

    /**
     * Wipes all data from the database.
     * @throws SQLException if a database access error occurs.
     */
    public void wipeDatabase() throws SQLException
    {
        try (Statement stmt = connection.createStatement()) { 
            connection.setAutoCommit(false); 
    
            stmt.executeUpdate("DELETE FROM students");
            stmt.executeUpdate("DELETE FROM competitions");
            stmt.executeUpdate("DELETE FROM results");
    
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
