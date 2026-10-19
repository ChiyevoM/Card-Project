import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CardGameTest {

    @TempDir
    Path tempDir;

    private File createTempPackFile(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

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
        List<Card> pack = CardGame.loadAndValidatePack(shortFile.getAbsolutePath(), 2);
        
        assertNull(pack, "Pack with missing cards should return null");
    }

    @Test
    public void testPackWithNegativeNumbersIsInvalid() throws IOException {
        // Contains a -5
        File negativeFile = createTempPackFile("negative.txt", "1\n2\n-5\n4\n5\n6\n7\n8\n");
        List<Card> pack = CardGame.loadAndValidatePack(negativeFile.getAbsolutePath(), 1);
        
        assertNull(pack, "Pack with negative numbers should return null");
    }

    @Test
    public void testPackWithNonIntegerCharactersIsInvalid() throws IOException {
        // Contains a letter 'A'
        File letterFile = createTempPackFile("letter.txt", "1\n2\nA\n4\n5\n6\n7\n8\n");
        List<Card> pack = CardGame.loadAndValidatePack(letterFile.getAbsolutePath(), 1);
        
        assertNull(pack, "Pack with letters should return null");
    }
}