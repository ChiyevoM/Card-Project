import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * CardGameTest.java
 * Session 3 & Session 4 Verification Suite: Tests pack parsing/validation using @TempDir
 * and verifies multithreaded ring execution, atomic win states, and output file writing.
 */
public class CardGameTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        Player.resetGameWon();
    }

    private File createTempPackFile(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    // ==========================================
    // SESSION 3: Pack Validation Tests
    // ==========================================

    @Test
    public void testValidPackLoadsCorrectly() throws IOException {
        // n=1 requires 8 cards
        File validFile = createTempPackFile("valid.txt", "1\n2\n3\n4\n5\n6\n7\n8\n");
        List<Card> pack = CardGame.loadAndValidatePack(validFile.getAbsolutePath(), 1);
        
        assertNotNull(pack, "Pack should be valid");
        assertEquals(8, pack.size());
    }

    @Test
    public void testPackWithTooFewCardsIsInvalid() throws IOException {
        // n=2 requires 16 cards, but we only give it 8
        File shortFile = createTempPackFile("short.txt", "1\n2\n3\n4\n5\n6\n7\n8\n");
        assertThrows(IllegalArgumentException.class, () -> {
            CardGame.loadAndValidatePack(shortFile.getAbsolutePath(), 2);
        }, "Pack with missing cards should throw IllegalArgumentException");
    }

    @Test
    public void testPackWithNegativeNumbersIsInvalid() throws IOException {
        // Contains a -5
        File negativeFile = createTempPackFile("negative.txt", "1\n2\n-5\n4\n5\n6\n7\n8\n");
        assertThrows(IllegalArgumentException.class, () -> {
            CardGame.loadAndValidatePack(negativeFile.getAbsolutePath(), 1);
        }, "Pack with negative numbers should throw IllegalArgumentException");
    }

    @Test
    public void testPackWithNonIntegerCharactersIsInvalid() throws IOException {
        // Contains a letter 'A'
        File letterFile = createTempPackFile("letter.txt", "1\n2\nA\n4\n5\n6\n7\n8\n");
        assertThrows(IllegalArgumentException.class, () -> {
            CardGame.loadAndValidatePack(letterFile.getAbsolutePath(), 1);
        }, "Pack with non-integer characters should throw IllegalArgumentException");
    }

    // ==========================================
    // SESSION 4: Multithreaded Execution Tests
    // ==========================================

    @Test
    public void testMultithreadedGameExecutionAndLogGeneration() throws IOException {
        // Generate a valid 2-player pack (16 cards)
        StringBuilder packContent = new StringBuilder();
        for (int i = 1; i <= 16; i++) {
            packContent.append(i).append("\n");
        }
        File validPack = createTempPackFile("valid_2player.txt", packContent.toString());

        CardGame game = new CardGame(2, validPack.getAbsolutePath());
        game.setupGame();

        // Ensure multithreaded simulation completes without freezing or deadlocks
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            game.startGame();
        }, "Multithreaded execution timed out - potential deadlock detected!");

        // Verify player output files exist and are not empty
        for (int i = 1; i <= 2; i++) {
            File playerLog = new File("player" + i + "_output.txt");
            assertTrue(playerLog.exists(), "player" + i + "_output.txt must exist");

            List<String> lines = Files.readAllLines(playerLog.toPath());
            assertFalse(lines.isEmpty(), "Player " + i + " log must not be empty");
        }

        // Verify deck output files exist
        for (int i = 1; i <= 2; i++) {
            File deckLog = new File("deck" + i + "_output.txt");
            assertTrue(deckLog.exists(), "deck" + i + "_output.txt must exist");
        }
    }
}