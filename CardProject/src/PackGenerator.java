import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackGenerator {

    /**
     * Generates a randomized, valid, and winnable 8n pack file for any given number of players 'n'.
     * 
     * @param n          Number of players (positive integer)
     * @param filename   Path/name of the output text file
     * @throws IOException If file writing fails
     */
    public static void generatePack(int n, String filename) throws IOException {
        int totalCards = 8 * n;
        List<Integer> cards = new ArrayList<>();

        // 1. Guarantee a winning 4-of-a-kind (four '1's) so the game can always finish
        for (int i = 0; i < 4; i++) {
            cards.add(1);
        }

        // 2. Fill the remaining cards with valid denominations distributed across n
        for (int i = 4; i < totalCards; i++) {
            cards.add((i % n) + 1);
        }

        // 3. Shuffle the list randomly so every simulation run is unique!
        Collections.shuffle(cards);

        // 4. Write each card denomination on its own line to the text file
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int cardValue : cards) {
                writer.println(cardValue);
            }
        }

        System.out.println("Successfully generated " + filename + " with " + totalCards + " shuffled lines for n = " + n);
    }
}