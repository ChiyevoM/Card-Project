import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for CardDeck.java.
 */
public class CardDeckTest {

    @Test
    public void testInvalidDeckIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new CardDeck(0));
        assertThrows(IllegalArgumentException.class, () -> new CardDeck(-2));
    }

    @Test
    public void testAddNullCardThrowsException() {
        CardDeck deck = new CardDeck(1);
        assertThrows(IllegalArgumentException.class, () -> deck.addCard(null));
    }

    @Test
    public void testFifoDrawAndDiscardOrder() {
        CardDeck deck = new CardDeck(1);
        assertTrue(deck.isEmpty());

        deck.addCard(new Card(1));
        deck.addCard(new Card(3));
        deck.addCard(new Card(7));

        assertEquals(3, deck.size());
        assertFalse(deck.isEmpty());

        // Must draw in strict First-In, First-Out (FIFO) order: 1, then 3, then 7
        assertEquals(1, deck.drawCard().getValue());
        assertEquals(3, deck.drawCard().getValue());
        assertEquals(7, deck.drawCard().getValue());
        assertTrue(deck.isEmpty());
    }

    @Test
    public void testDrawCardWithGameWonFlagReturnsNullWhenEmpty() {
        CardDeck deck = new CardDeck(1);
        AtomicBoolean gameWon = new AtomicBoolean(true);

        // When deck is empty and gameWon is true, drawCard should return null immediately
        assertNull(deck.drawCard(gameWon));
    }

    @Test
    public void testWriteDeckOutputFileFormat() throws IOException {
        CardDeck deck = new CardDeck(2);
        deck.addCard(new Card(1));
        deck.addCard(new Card(3));
        deck.addCard(new Card(3));
        deck.addCard(new Card(7));

        deck.writeDeckOutputFile();

        File file = new File("deck2_output.txt");
        assertTrue(file.exists());

        String content = Files.readString(file.toPath()).trim();
        assertEquals("deck2 contents: 1 3 3 7", content);

        file.delete(); // Clean up test file
    }
}
