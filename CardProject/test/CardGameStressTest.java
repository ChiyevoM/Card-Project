import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

public class CardGameStressTest {

    @TempDir
    Path tempDir;

    @Test
    public void testFourCardConservationUnderStress() throws Exception {
        // 1. Create a valid 4-player pack (32 cards)
        File packFile = tempDir.resolve("stress_pack.txt").toFile();
        try (FileWriter writer = new FileWriter(packFile)) {
            for (int i = 0; i < 32; i++) {
                // Mix the numbers wildly so the game takes many turns to win
                writer.write(((i * 7) % 6 + 1) + "\n"); 
            }
        }

        // 2. Run the game 50 times to actively hunt for race conditions
        for (int iteration = 1; iteration <= 50; iteration++) {
            CardGame game = new CardGame(4, packFile.getAbsolutePath());
            game.setupGame();
            game.startGame(); // Blocks until all threads terminate

            // 3. System-level reconciliation: Verify no cards were lost or duplicated
            for (Player p : game.getPlayers()) {
                assertEquals(4, p.getHand().size(), 
                    "Race condition detected! Player " + p.getPlayerId() + 
                    " ended with " + p.getHand().size() + " cards on iteration " + iteration);
            }
        }
    }
}