import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PackGenerator {
    // Generates a valid 8n pack file for any given number of players 'n'
    public static void generatePack(int n, String filename) throws IOException {
        int totalCards = 8 * n;
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // To ensure the game is winnable, let's make player 1 win 
            // by injecting four '1's right into the mix
            for (int i = 0; i < totalCards; i++) {
                if (i < 4) {
                    writer.println(1); // Guaranteed winning cards for player 1
                } else {
                    writer.println((i % n) + 1); // Distribute other valid denominations
                }
            }
        }
        System.out.println("Generated " + filename + " with " + totalCards + " lines for n = " + n);
    }
}