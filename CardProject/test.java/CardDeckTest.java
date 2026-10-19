import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

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

        // Using readAllBytes is compatible with older Java versions
        String content = new String(Files.readAllBytes(file.toPath())).trim();
        assertEquals("deck2 contents: 1 3 3 7", content);

        file.delete(); // Clean up test file
    }

    // ==========================================
    // SESSION 5: Timed-Wait Concurrency Tests
    // ==========================================

    @Test
    public void testTimedWaitPreventsDeadlockWhenEmpty() {
        CardDeck deck = new CardDeck(1);
        AtomicBoolean gameWon = new AtomicBoolean(false);

        // Verify drawing from an empty deck with timed wait unblocks when gameWon triggers
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Thread worker = new Thread(() -> {
                deck.drawCard(gameWon);
            });
            worker.start();

            // Allow worker thread to enter wait(10) loop on empty deck
            Thread.sleep(50);

            // Signal game victory from main thread
            gameWon.set(true);

            // Worker thread MUST exit timed-wait loop and terminate without deadlocking
            worker.join(1000);
            assertFalse(worker.isAlive(), "Worker thread must exit wait loop after gameWon flag is set");
        }, "Timed-wait failed - worker thread deadlocked on empty deck!");
    }

    @Test
    public void testConcurrentAddAndDrawUnblocksThread() {
        CardDeck deck = new CardDeck(1);
        AtomicBoolean gameWon = new AtomicBoolean(false);

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Thread consumer = new Thread(() -> {
                Card drawn = deck.drawCard(gameWon);
                assertNotNull(drawn, "Consumer thread should receive the card added by producer");
                assertEquals(5, drawn.getValue());
            });
            consumer.start();

            Thread.sleep(50); // Ensure consumer is waiting on empty deck
            deck.addCard(new Card(5)); // Producer adds card and triggers notifyAll()

            consumer.join(1000);
            assertFalse(consumer.isAlive(), "Consumer thread should finish cleanly after receiving card");
        });
    }
}