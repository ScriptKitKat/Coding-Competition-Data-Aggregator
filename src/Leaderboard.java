import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.*;

/**
 * Class Leaderboard is the main class/driver class of th program.
 * It provides the user with a menu of options to choose from,
 * such as loading data, viewing the leaderboard, adding students,
 * removing students, adding competitions, removing competitions,
 * filtering students, viewing competition data, viewing student data,
 * and exporting the leaderboard.
 */

public class Leaderboard {
  static DatabaseHandler dbHandler;
  static DataHandler dataHandler;
  static SpreadsheetParser parser;
  static boolean fileLoaded = false;
  /**
   * Main method of the program.
   * @param args Command line arguments.
   * @throws SQLException if a database access error occurs.
   */
  public static void main(String[] args) throws SQLException
  {
    dbHandler = new DatabaseHandler("jdbc:sqlite:competition-student-database.db");
    parser = new SpreadsheetParser(dbHandler);
    dataHandler = new DataHandler();

    // Loads pre-existing data from the database
    if (!dbHandler.isDatabaseEmpty()) {
      dataHandler.loadData(dbHandler.getConnection());
      fileLoaded = true;
    }

    int choice;
    // Loop to display menu & get user input
    do
    {
      // choice = 0 is quit
      choice = printMenu();
      if (choice != 0)
      {
        getChoice(choice);
      }
    } while(choice != 0);
  }

  /**
   * Prompts the user to select a CSV file.
   * @return The selected file, or null if no file was selected.
   */
  public static File selectCSVFile()
  {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select CSV file");
    int userSelection = fileChooser.showOpenDialog(null);
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        return fileChooser.getSelectedFile();
    }
    return null;
  }

  /**
   * Method to display the menu and get user input.
   * @return The user's choice index.
   */
  public static int printMenu()
  {
    String[] options = {"Quit", "Load Data (csv only)", "View Leaderboard", "View All Data", "Add student", "Remove student", "Add competition", "Remove competition", "Filter students", "View Competition Data", "View Student Data", "Export All Data", "Wipe Database"};
    String input = (String) JOptionPane.showInputDialog(null, "Choose Option", "Menu", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    int index = 0;

    // Finding index of user's choice
    for (int i = 0; i < options.length; i++) {
      if (options[i].equals(input)) {
        index = i;
        break;
      }
    }

    return index;
  }

  /**
   * Method to execute the user's choice.
   * @param choice The user's choice.
   * @throws SQLException if a database access error occurs.
   */
  public static void getChoice(int choice) throws SQLException
  {

    // If the user does not load a file for it's first executable choice, display an error message
    if (dbHandler.isDatabaseEmpty() && choice != 1) {
      JOptionPane.showMessageDialog(null, "Please load data first.");
      return;
    }

    // Switch statement to execute user's choice
    switch(choice)
    {
      // Load data
      case 1:
        try {
          File file = selectCSVFile();

          String comp = JOptionPane.showInputDialog("Competition Name?");

          if (file != null && file.getName().endsWith(".csv")) {
            parser.read(file, comp);
            dataHandler.loadData(dbHandler.getConnection());
            fileLoaded = true;

            JOptionPane.showMessageDialog(null, "Data loaded successfully.");
          } else {
            // If no file is selected or the file does not end with ".csv", display an error message
            JOptionPane.showMessageDialog(null, "Failed to find file.");
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        break;
      // View leaderboard
      case 2:
        // Sort student by the # of competitions participated in
        Student[] students = dataHandler.sortRosterByCompetition();

        // Creating arrays to store data for the JTable
        Object[][] rows = new Object[students.length][3];
        Object[] cols = {"Name","# of Competitions Participated in","Total # of problems solved"};

        // Loop through the students and add their data to the 2D array
        for (int i = 0; i < students.length; i++) {
          Object[] row = {students[i].getName(), students[i].getCompetitions().size(), students[i].getTotalProb()};
          rows[i] = row;
        }

        JTable table = new JTable(rows, cols);
        // Display JTable
        JOptionPane.showMessageDialog(null, new JScrollPane(table));
        break;
      // View all data
      case 3:
        Competition[] compList = dataHandler.getCompetition();


        Object[] colsData = {"Competition Name","Student Name","# of problems solved","Placement"};
        ArrayList<Object[]> rowsData = new ArrayList<>();
        // loop for data
        for (Competition compExported : compList) {
          Object[][] competitionData = dataHandler.getCompetitionData(compExported.getName(), dbHandler.getConnection());
          for (int i = 0; i < competitionData.length; i++) {
            rowsData.add(new Object[]{compExported.getName(), competitionData[i][0], competitionData[i][1], competitionData[i][2]});
          }
        }

        JTable tabAllData = new JTable(rowsData.toArray(new Object[0][]), colsData);
        JOptionPane.showMessageDialog(null, new JScrollPane(tabAllData));
        break;
      // Add student
      case 4:
        try {
          String name = JOptionPane.showInputDialog("Name (First & Last)");
          String email = JOptionPane.showInputDialog("Email");

          String[] myChoices = {"Novice", "Advanced"};
          String level = myChoices[JOptionPane.showOptionDialog(null, "Level of Student?", "Level", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, myChoices, myChoices[0])];

          if (name.equals("") && email.equals("")) {
            JOptionPane.showMessageDialog(null, "Failed to add student. No Name or email given.");
            break;
          }

          int cont = 0;

          // Get the list of competition names into a string list of competition names
          Competition[] listOfCompetitions = dataHandler.getCompetition();
          String[] compNameStrings = new String[listOfCompetitions.length];
          for (int i = 0; i < listOfCompetitions.length; i++) {
            compNameStrings[i] = listOfCompetitions[i].getName();
          }

          //Loop to allow the user to add multiple competitions for the student
          do {
            String comp = (String) JOptionPane.showInputDialog(null, "Choose Competition", "Competitions", JOptionPane.PLAIN_MESSAGE, null, compNameStrings, compNameStrings[0]);

            // Removes competition from the list of competitions
            String[] array = Arrays.stream(compNameStrings).filter(value -> !value.equals(comp)).toArray(String[]::new);
            compNameStrings = array;

            // Get student info
            int probSolved = Integer.parseInt(JOptionPane.showInputDialog("Problems Solved"));
            int place = Integer.parseInt(JOptionPane.showInputDialog("Placed"));


            // Add student info into database
            dbHandler.addStudentAndCompetition(level, name, email, probSolved, place, comp);
            cont = JOptionPane.showConfirmDialog(null, "Continue?", "Continue to Add Competitions", JOptionPane.YES_NO_OPTION);
          } while (cont == 0 && compNameStrings.length > 0);

          // Load data from the database to update the datahandler
          dataHandler.loadData(dbHandler.getConnection());

          // Display success message
          JOptionPane.showMessageDialog(null, "success!", "Added Student", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "No Student Added.");
        }
        break;
      // Remove student
      case 5:
        // Get student roster into String form
        Student[] roster = dataHandler.getStudents();
        String[] rosterStrings = new String[roster.length];
        for (int i = 0; i < roster.length; i++) {
          rosterStrings[i] = roster[i].getName();
        }

        try {
            // Ask user for a student's name
            String str = (String) JOptionPane.showInputDialog(null, "Choose Student to Remove", "Student Removal", JOptionPane.PLAIN_MESSAGE, null, rosterStrings, rosterStrings[0]);

            // Remove student from the list and update datahandler
            dbHandler.removeStudent(str);
            dataHandler.loadData(dbHandler.getConnection());

            JOptionPane.showMessageDialog(null, "Student removed successfully.");
          } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No Student removed.");
          }
        break;
      // Add competition
      case 6:

        try {
          String compName = JOptionPane.showInputDialog("Competition Name");

          Student[] studentData = dataHandler.getStudents();

          int addStudents = JOptionPane.showConfirmDialog(null, "Add Students?", "Do you want to add students?", JOptionPane.YES_NO_OPTION);

          if (addStudents == 0) {
            String[] studentNameStrings = new String[studentData.length];
            int cont2 = 0;
            for (int i = 0; i < studentData.length; i++) {
              studentNameStrings[i] = studentData[i].getName();
            }
            do {
              String student = (String) JOptionPane.showInputDialog(null, "Choose Students to Add", "Students", JOptionPane.PLAIN_MESSAGE, null, studentNameStrings, studentNameStrings[0]);
              int studentIndex = Arrays.asList(studentNameStrings).indexOf(student);

              // Removes competition from the list of competitions
              String[] array = Arrays.stream(studentNameStrings).filter(value -> !value.equals(student)).toArray(String[]::new);
              studentNameStrings = array;

              // Get student info
              int probSolved = Integer.parseInt(JOptionPane.showInputDialog("Problems Solved"));
              int place = Integer.parseInt(JOptionPane.showInputDialog("Placed"));

              // Add student info into database
              dbHandler.addStudentAndCompetition(studentData[studentIndex].getLevel(), studentData[studentIndex].getName(), studentData[studentIndex].getEmail(), probSolved, place, compName);
              cont2 = JOptionPane.showConfirmDialog(null, "Continue?", "Continue to Add Students", JOptionPane.YES_NO_OPTION);
            } while (cont2 == 0 && studentNameStrings.length > 0);
          } else {
            dbHandler.addCompetition(compName);
          }
          dataHandler.loadData(dbHandler.getConnection());
          JOptionPane.showMessageDialog(null, "Competition added successfully.");
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "No Competition added.");
        }

        break;
      // Remove competition
      case 7:
        // Get competition list into String form
        Competition[] listOfComp = dataHandler.getCompetition();
        String[] compStrings = new String[listOfComp.length];
        for (int i = 0; i < listOfComp.length; i++) {
          compStrings[i] = listOfComp[i].getName();
        }

        try {
          String comp = (String) JOptionPane.showInputDialog(null, "Choose Competition to Remove", "Competition Removal", JOptionPane.PLAIN_MESSAGE, null, compStrings, compStrings[0]);

          // Remove competition from the list and update datahandler
          dbHandler.removeCompetition(comp);
          dataHandler.loadData(dbHandler.getConnection());

          JOptionPane.showMessageDialog(null, "Competition removed successfully.");
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "No Competition removed.");
        }
        break;
      // Filter students
      case 8:
        // Get the list of choices for filtering students
        String[] choices = {"# of Competitions Participated in", "# of Problems Solved", "Novice", "Advanced"};
        String filter = choices[JOptionPane.showOptionDialog(null, "Filter Students by?", "Filter", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0])];
        // Create an array of students based on the user's choice
        Student[] filteredStudents = new Student[0];

        if (filter.equals(choices[0])) {
          // Sorts students by # of competitions participated in
          filteredStudents = dataHandler.sortRosterByCompetition();
        } else if (filter.equals(choices[1])) {
          // Sorts students by # of problems solved
          filteredStudents = dataHandler.sortRosterByProblem();
        } else if (filter.equals(choices[2])) {
          // Display students with novice level
          filteredStudents = dataHandler.getRosterByLevel("Novice");
          // Display students with advance level
        } else if (filter.equals(choices[3])) {
          filteredStudents = dataHandler.getRosterByLevel("Advanced");
        }

        // Create a 2D array to store the data for the JTable
        Object[][] rows2 = new Object[filteredStudents.length][3];
        Object[] cols2 = {"Name","Problems Solved","# of participated competitions"};

        // Loop through the students and add their data to the 2D array
        for (int i = 0; i < filteredStudents.length; i++) {
          Object[] row = {filteredStudents[i].getName(), Integer.valueOf(filteredStudents[i].getTotalProb()), Integer.valueOf(filteredStudents[i].getCompetitions().size())};
          rows2[i] = row;
        }

        JTable table2 = new JTable(rows2, cols2);
        // Display the JTable
        JOptionPane.showMessageDialog(null, new JScrollPane(table2));
        break;
      // View competition data
      case 9:
        // Get list of competition names
        Competition[] listOfComp2 = dataHandler.getCompetition();
        String[] compStrings2 = new String[listOfComp2.length];
        for (int i = 0; i < listOfComp2.length; i++) {
          compStrings2[i] = listOfComp2[i].getName();
        }

        String comp2 = (String) JOptionPane.showInputDialog(null, "Choose Competition", "Competition Data", JOptionPane.PLAIN_MESSAGE, null, compStrings2, compStrings2[0]);

        // Get the competition data from the datahandler to be displayed with JTable
        Object[][] compData = dataHandler.getCompetitionData(comp2, dbHandler.getConnection());
        Object[] col = {"Name","Problems Solved","Placement"};
        // Displays JTable with data
        JTable tab = new JTable(compData, col);
        JOptionPane.showMessageDialog(null, new JScrollPane(tab));
        break;
      // View student data
      case 10:
        // Get list of student names
        Student[] listOfStudents = dataHandler.getStudents();
        String[] stuStrings2 = new String[listOfStudents.length];
        for (int i = 0; i < listOfStudents.length; i++) {
          stuStrings2[i] = listOfStudents[i].getName();
        }

        String stu = (String) JOptionPane.showInputDialog(null, "Choose Student", "Student Data", JOptionPane.PLAIN_MESSAGE, null, stuStrings2, stuStrings2[0]);

        // Get the competition data from the datahandler to be displayed with JTable
        Object[][] stuData = dataHandler.getStudent(stu, dbHandler.getConnection());
        Object[] col2 = {"Competition","Problems Solved","Placed"};
        // Display JTable
        JTable tab2 = new JTable(stuData, col2);
        JOptionPane.showMessageDialog(null, new JScrollPane(tab2));
        break;
      // Exports All Data
      case 11:
        Competition[] compListExport = dataHandler.getCompetition();
        String fileName = JOptionPane.showInputDialog("File Name to save to?");
        try {
          // Create filewriter that writes to a csv file
          FileWriter writer = new FileWriter(fileName + ".csv");

          // header of file
          writer.append("Competition Name");
          writer.append(',');
          writer.append("Student Name");
          writer.append(',');
          writer.append("Problems Solved");
          writer.append(',');
          writer.append("Placement");
          writer.append('\n');

          // loop for data
          for (Competition compExported : compListExport) {
            Object[][] competitionData = dataHandler.getCompetitionData(compExported.getName(), dbHandler.getConnection());
            for (int i = 0; i < competitionData.length; i++) {
              writer.append(compExported.getName());
              writer.append(',');
              writer.append(String.valueOf(competitionData[i][0]));
              writer.append(',');
              writer.append(String.valueOf(competitionData[i][1]));
              writer.append(',');
              writer.append(String.valueOf(competitionData[i][2]));
              writer.append('\n');
            }
          }

          writer.flush();
          writer.close();
          JOptionPane.showMessageDialog(null, "Exported successfully.");
        } catch(IOException e) {
          e.printStackTrace();
        }
        break;
      // Wipe database memory
      case 12:
        dbHandler.wipeDatabase();
        dataHandler.loadData(dbHandler.getConnection());
        fileLoaded = false;
        JOptionPane.showMessageDialog(null, "Database wiped successfully.");
        break;
      // default: display error message for invalid choice
      default:
        JOptionPane.showMessageDialog(null, "Sorry, invalid choice", "choice", JOptionPane.INFORMATION_MESSAGE);
    }
  }
}