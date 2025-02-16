/**
 * The Competition class represents a competition in which students participate.
 */
public class Competition {
  private int id;
  private String name;
  private int placement;
  private int problemsSolved;

  /**
   * Constructs a Competition object with the specified ID and name.
   *
   * @param id The ID of the competition.
   * @param name The name of the competition.
   */ 
  public Competition(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Constructs a Competition object with the specified ID, name, problems solved, and placement.
   *
   * @param id The ID of the competition.
   * @param name The name of the competition.
   * @param problemsSolved The number of problems solved in the competition.
   * @param placement The placement in the competition.
   */
  public Competition(int id, String name, int problemsSolved, int placement) {
    this.id = id;
    this.name = name;
    this.placement = placement;
    this.problemsSolved = problemsSolved;
  }

  /**
   * Gets the ID of the competition.
   *
   * @return The ID of the competition.
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the name of the competition.
   *
   * @return The name of the competition.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the placement of the student in the competition.
   *
   * @return The placement of the student.
   */
  public int getPlacement() {
    return placement;
  }

  /**
   * Gets the number of problems solved in the competition.
   *
   * @return The number of problems solved.
   */
  public int getProblemsSolved() {
    return problemsSolved;
  }

  /**
   * Checks if this competition is equal to another object.
   *
   * @param obj The object to compare to.
   * @return true if the object is a Competition with the same name, false otherwise.
   */
  public boolean equals(Object obj) {
    Competition c = (Competition) obj;

    return this.name.equals(c.getName());
  }
}
