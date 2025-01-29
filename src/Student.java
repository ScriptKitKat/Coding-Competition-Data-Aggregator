import java.util.ArrayList;
import java.util.List;

/**
 * The Student class represents a student participating in competitions.
 */
public class Student {
  private int id;
  private String firstName;
  private String lastName;
  private String email;
  private String level; // "Advanced" or "Novice"
  private List<Competition> competitions;

  /**
   * Constructs a Student object.
   *
   * @param id The ID of the student.
   * @param firstName The first name of the student.
   * @param lastName The last name of the student.
   * @param email The email of the student.
   * @param level The level of the student (e.g., "Advanced" or "Novice").
   */
  public Student(int id, String firstName, String lastName, String email, String level) 
  {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.level = level;
    this.competitions = new ArrayList<>();
  }

  /**
   * Adds a competition to the student's list of competitions.
   *
   * @param competition The competition to add.
   */
  public void addCompetition(Competition competition)
  {
    this.competitions.add(competition);
  }

  /**
   * Gets the ID of the student.
   *
   * @return The ID of the student.
   */
  public int getId() 
  {
    return id;
  }

  /**
   * Gets the full name of the student.
   *
   * @return The full name of the student.
   */
  public String getName() 
  {
    return firstName + " " + lastName;
  }
  
  /**
   * Gets the email of the student.
   *
   * @return The email of the student.
   */
  public String getEmail() 
  {
    return email;
  }

  /**
   * Gets the level of the student.
   *
   * @return The level of the student.
   */
  public String getLevel()
  {
    return level;
  }

  /**
   * Gets the list of competitions the student has participated in.
   *
   * @return The list of competitions.
   */
  public List<Competition> getCompetitions()
  {
    return competitions;
  }

  /**
   * Gets the total number of problems solved by the student across all competitions.
   *
   * @return The total number of problems solved.
   */
  public int getTotalProb()
  {
    int total = 0;
    for (Competition c : competitions) {
      total += c.getProblemsSolved();
    }

    return total;
  }

  /**
   * Compares the total number of problems solved by this student to another student.
   *
   * @param o The other student to compare to.
   * @return The difference in the total number of problems solved.
   */
  public int compareToProb(Object o)
  {
    Student s = (Student) o;
    return this.getTotalProb() - s.getTotalProb();
  }
  
  /**
   * Checks if this student is equal to another object.
   *
   * @param obj The object to compare to.
   * @return true if the object is a Student with the same name, false otherwise.
   */
  public boolean equals(Object obj)
  {
    Student s = (Student) obj;

    return this.getName().equals(s.getName());
  }
}
