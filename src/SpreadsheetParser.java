import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * The SpreadsheetParser class is responsible for parsing a CSV file and adding the data to the database.
 */
public class SpreadsheetParser {
    private DatabaseHandler db;

    /**
     * Constructs a SpreadsheetParser object with the specified DatabaseHandler.
     * @param dbHandler The DatabaseHandler to interact with the database.
     */
    public SpreadsheetParser(DatabaseHandler dbHandler) 
    {
        this.db = dbHandler;
    }

    /**
     * Reads the specified CSV file and adds the data to the database.
     * @param file The CSV file to read.
     * @param nameOfComp The name of the competition.
     * @throws FileNotFoundException if the specified file is not found.
     */
    public void read(File file, String nameOfComp) throws FileNotFoundException 
    {
        Scanner scn = new Scanner(file);
        boolean isAdvanced = false;
        boolean isNovice = false;

        while(scn.hasNextLine()) {
            String line = scn.nextLine().trim();

            // Starts reading the advanced students after a "#" is found
            if (line.contains("#")) {
                isAdvanced = true;
                isNovice = false;
                continue;
            }

            // Starts reading the novice students after a "~" is found
            if (line.contains("~")) {
                isAdvanced = false;
                isNovice = true;
                continue;
            }

            if (isAdvanced || isNovice) {
                String[] data = line.split(",");
                if (data.length < 5) continue; // Skipping the invalid rows
                String member1 = data[3].trim();
                String email1 = data[4].trim();
                if (member1.isEmpty() && email1.isEmpty()) {
                    isAdvanced = false;
                    isNovice = false;
                    continue;
                }
                String member2 = data[5].trim();
                String email2 = data[6].trim();
                String member3 = data[7].trim();
                String email3 = data[8].trim();
                int problemsSolved = Integer.parseInt(data[9].trim());
                int placement = Integer.parseInt(data[10].trim());

                db.addStudentAndCompetition(isAdvanced ? "Advanced" : "Novice", member1, email1, member2, email2, member3, email3, problemsSolved, placement, nameOfComp);
            }
        }
        scn.close();
    }
}
