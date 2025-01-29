import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DataHandler class is responsible for handling the data objects. DataHandler interacts with the driver class/main method and object classes.
 */
public class DataHandler {
  private ArrayList<Student> roster;
  private ArrayList<Competition> listOfComp;
  /**
   * Constructor for DataHandler.
   */
  public DataHandler()
  {
    roster = new ArrayList<Student>();
    listOfComp = new ArrayList<Competition>();
  }

  /**
   * Loads data from the database.
   *
   * @param connection The SQL database connection to load data from.
   * @throws SQLException if a database access error occurs.
   */
  public void loadData(Connection connection) throws SQLException
  {
    roster.clear();
    listOfComp.clear();
    String query = "SELECT * FROM students";

    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int id = rs.getInt("id");
        String firstName = rs.getString("firstname");
        String lastName = rs.getString("lastname");
        String email = rs.getString("email");
        String level = rs.getString("level");

        // Create Student object
        Student student = new Student(id, firstName, lastName, email, level);

        // Add competitions for this student
        List<Competition> competitions = getCompetitionsForStudent(id, connection);
        for (Competition comp : competitions) {
          student.addCompetition(comp);
        }

        // Add to the list of students
        roster.add(student);
      }
    }

    String queryComp = "SELECT * FROM competitions";
    try (PreparedStatement stmtComp = connection.prepareStatement(queryComp);
         ResultSet rs = stmtComp.executeQuery()) {

      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");

        // Create Competition object
        Competition comp = new Competition(id, name);

        // Add to the list of competitions
        listOfComp.add(comp);
      }
    }
  }

  /**
   * Retrieves the competitions for a specific student.
   *
   * @param studentId The ID of the student.
   * @param connection The SQL database connection.
   * @return A list of competitions for the student.
   * @throws SQLException if a database access error occurs.
   */
  public List<Competition> getCompetitionsForStudent(int studentId, Connection connection) throws SQLException
  {
    String query = "SELECT * FROM results WHERE student_id = ?";
    List<Competition> competitions = new ArrayList<>();

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, studentId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int competitionId = rs.getInt("competition_id");
          int problemsSolved = rs.getInt("problems_solved");
          int placement = rs.getInt("placement");

          String queryComp = "SELECT name FROM competitions WHERE id = ?";
          String competitionName = null;

          try (PreparedStatement stmtComp = connection.prepareStatement(queryComp)) {
            // Set the competition ID in the query
            stmtComp.setInt(1, competitionId);

            try (ResultSet rsComp = stmtComp.executeQuery()) {
              // If a row exists, get the competition name
              if (rsComp.next()) {
                competitionName = rsComp.getString("name");
              }
            }
          }

          competitions.add(new Competition(competitionId, competitionName, problemsSolved, placement));
        }
      }
    }
    return competitions;
  }

  /**
   * Gets the students in the roster.
   *
   * @return An array of students.
   */
  public Student[] getStudents()
  {
    Student[] s = new Student[roster.size()];
    for (int i = 0; i < roster.size(); i++) {
      s[i] = roster.get(i);
    }
    return s;
  }

  /**
   * Gets the competitions in the list of competitions.
   *
   * @return An array of competitions.
   */
  public Competition[] getCompetition()
  {
    Competition[] c = new Competition[listOfComp.size()];
    for (int i = 0; i < listOfComp.size(); i++) {
      c[i] = listOfComp.get(i);
    }
    return c;
  }

  /**
   * Gets the students in the roster by level.
   *
   * @param s The level of the students to get.
   * @return An array of students based on the level.
   */
  public Student[] getRosterByLevel(String s)
  {
    if (s.equals("Novice")) {
      ArrayList<Student> temp = new ArrayList<Student>();
      for (Student student : roster) {
        if (student.getLevel().equals("Novice")) {
          temp.add(student);
        }
      }
      Student[] students = new Student[temp.size()];
      for (int i = 0; i < temp.size(); i++) {
        students[i] = temp.get(i);
      }
      return students;
    } else if (s.equals("Advanced")) {
      ArrayList<Student> temp = new ArrayList<Student>();
      for (Student student : roster) {
        if (student.getLevel().equals("Advanced")) {
          temp.add(student);
        }
      }
      Student[] students = new Student[temp.size()];
      for (int i = 0; i < temp.size(); i++) {
        students[i] = temp.get(i);
      }
      return students;
    } else {
      return getStudents();
    }
  }

  /**
   * Gets the competition data for a specific competition.
   *
   * @param competitionName The name of the competition.
   * @param connection The SQL database connection.
   * @return A 2D array of competition data.
   * @throws SQLException if a database access error occurs.
   */
  public Object[][] getCompetitionData(String competitionName, Connection connection) throws SQLException
  {
    Competition comp = new Competition(-1, competitionName);
    int compId = -1;
    for (Competition c : listOfComp) {
      if (c.equals(comp)) {
        compId = c.getId();
      }
    }

    ArrayList<Object[]> data = new ArrayList<>();

    String query = "SELECT * FROM results WHERE competition_id = ?";

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
      stmt.setInt(1, compId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int studentId = rs.getInt("student_id");
          int problemsSolved = rs.getInt("problems_solved");
          int placement = rs.getInt("placement");

          String queryStu = "SELECT firstname, lastname FROM students WHERE id = ?";
          String studentName = null;

          try (PreparedStatement stmtStu = connection.prepareStatement(queryStu)) {
            stmtStu.setInt(1, studentId);

            try (ResultSet rsStu = stmtStu.executeQuery()) {
              if (rsStu.next()) {
                studentName = rsStu.getString("firstname") + " " + rsStu.getString("lastname");
              }
            }
          }
          data.add(new Object[]{studentName, problemsSolved, placement});
        }
      }
    }

    return data.toArray(new Object[0][]);
  }

  /**
   * Gets the student data for a specific student.
   *
   * @param stu The name of the student.
   * @param connection The SQL database connection.
   * @return A 2D array of student data.
   */
  public Object[][] getStudent(String stu, Connection connection)
  {
    Student student = new Student(-1, stu.split(" ")[0], stu.split(" ")[1], "", "");

    for (Student s : roster) {
      if (s.equals(student)) {
        student = s;
      }
    }

    ArrayList<Object[]> data = new ArrayList<>();
    for (Competition comp : student.getCompetitions()) {
      data.add(new Object[]{comp.getName(), comp.getProblemsSolved(), comp.getPlacement()});
    }

    return data.toArray(new Object[0][]);
  }

  /**
   * Empties the roster.
   */
  public void emptyStudent()
  {
    roster.clear();
  }

  /**
   * Empties the list of competitions.
   */
  public void emptyCompetition()
  {
    listOfComp.clear();
  }

  /**
   * Sorts the roster by competition.
   *
   * @return An array of sorted students.
   */
  public Student[] sortRosterByCompetition()
  {
    if (roster.size() > 0) {
      quickSortComp(0, roster.size() - 1);
    }
    return getStudents();
  }

  /**
   * Sorts the roster by problem.
   *
   * @return An array of sorted students.
   */
 public Student[] sortRosterByProblem()
 {
   if (roster.size() > 0) {
     quickSortProb(0, roster.size() - 1);
   }
   return getStudents();
 }

  /**
   * Quick sorts the roster by problem.
   *
   * @param low The low index of the roster to sort.
   * @param high The high index of the roster to sort.
   */
 private void quickSortProb(int low, int high)
 {
   int left = low;
   int right = high;
   Student pivot = roster.get(low+(high-low)/2);
   while (left <= right) {
     while (roster.get(left).compareToProb(pivot) > 0) {
       left++;
     }
     while (roster.get(right).compareToProb(pivot) < 0) {
       right--;
     }
     if (left <= right) {
       Student temp = roster.get(left);
       roster.set(left, roster.get(right));
       roster.set(right, temp);
       left++;
       right--;
     }
   }
   if (low < right) {
     quickSortProb(low, right);
   }
   if (left < high) {
     quickSortProb(left, high);
   }
 }

  /**
   * Quick sorts the roster by competition.
   *
   * @param low The low index of the roster to sort.
   * @param high The high index of the roster to sort.
   */
  private void quickSortComp(int low, int high)
  {
    int left = low;
    int right = high;
    Student pivot = roster.get(low+(high-low)/2);
    while (left <= right) {
      while (roster.get(left).getCompetitions().size() - pivot.getCompetitions().size() > 0) {
        left++;
      }
      while (roster.get(right).getCompetitions().size() - pivot.getCompetitions().size() < 0) {
        right--;
      }
      if (left <= right) {
        Student temp = roster.get(left);
        roster.set(left, roster.get(right));
        roster.set(right, temp);
        left++;
        right--;
      }
    }
    if (low < right) {
      quickSortComp(low, right);
    }
    if (left < high) {
      quickSortComp(left, high);
    }
  }
}

