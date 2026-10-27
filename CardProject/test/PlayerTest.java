import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerTest {

    @Test
    public void testAntiStagnationDiscardStrategy() {
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player player1 = new Player(1, left, right); // Player 1 prefers denomination 1

        // Add 1 preferred card (1) and 3 non-preferred cards (2, 3, 4)
        player1.receiveInitialCard(new Card(1));
        player1.receiveInitialCard(new Card(2));
        player1.receiveInitialCard(new Card(3));
        player1.receiveInitialCard(new Card(4));

        left.addCard(new Card(5));
        left.addCard(new Card(6));

        // Test 1: First discard must select the oldest non-preferred card (2)
        Card discarded1 = player1.selectCardToDiscard();
        assertEquals(2, discarded1.getValue(), "First discard should be the oldest non-preferred card (2)");

        // Test 2: Second discard must select the next non-preferred card (3)
        Card discarded2 = player1.selectCardToDiscard();
        assertEquals(3, discarded2.getValue(), "Second discard should be the next non-preferred card (3)");

        // Test 3: Third discard must select remaining non-preferred card (4)
        Card discarded3 = player1.selectCardToDiscard();
        assertEquals(4, discarded3.getValue(), "Third discard should be the remaining non-preferred card (4)");

        // Test 4: When holding only preferred cards (1), it should discard the first card
        player1.getHand().clear(); // Reset hand
        player1.receiveInitialCard(new Card(1));
        player1.receiveInitialCard(new Card(1));
        player1.receiveInitialCard(new Card(1));
        player1.receiveInitialCard(new Card(1));

        Card discardedPreferred = player1.selectCardToDiscard();
        assertEquals(1, discardedPreferred.getValue(), "When holding only preferred cards, discard first card");
    }

    @Test
    public void testWinningHandDetection() {
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player player = new Player(1, left, right);

        player.receiveInitialCard(new Card(3));
        player.receiveInitialCard(new Card(3));
        player.receiveInitialCard(new Card(3));
        player.receiveInitialCard(new Card(3));

        assertTrue(player.hasWinningHand(), "Hand with four identical cards should be a winning hand");
    }

    @Test
    public void testPlayerOutputFileFormatWhenOtherPlayerWins() throws IOException {
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        AtomicInteger sharedWinner = new AtomicInteger(3); // Player 3 won
        Player player1 = new Player(1, left, right, sharedWinner);

        player1.receiveInitialCard(new Card(2));
        player1.receiveInitialCard(new Card(2));
        player1.receiveInitialCard(new Card(3));
        player1.receiveInitialCard(new Card(5));
        player1.logInitialHand();

        player1.writePlayerOutputFile();

        File out = new File("player1_output.txt");
        assertTrue(out.exists(), "Output file player1_output.txt should be created");
        
        String content = new String(Files.readAllBytes(out.toPath()));
        assertTrue(content.contains("player 1 initial hand 2 2 3 5"));
        assertTrue(content.contains("player 3 has informed player 1 that player 3 has won"));
        assertTrue(content.contains("player 1 exits"));

        out.delete(); // Clean up test file
    }
}