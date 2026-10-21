import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PlayerTest {

    @Test
    public void testAntiStagnationDiscardStrategy() {
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player player1 = new Player(1, left, right); // Prefers 1s

        player1.receiveInitialCard(new Card(1));
        player1.receiveInitialCard(new Card(2));
        player1.receiveInitialCard(new Card(3));
        player1.receiveInitialCard(new Card(4));

        left.addCard(new Card(5));
        left.addCard(new Card(6));

       
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

        assertTrue(player.hasWinningHand());
    }

    @Test
    public void testPlayerOutputFileFormatWhenOtherPlayerWins() throws IOException {
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player player1 = new Player(1, left, right);

        player1.receiveInitialCard(new Card(2));
        player1.receiveInitialCard(new Card(2));
        player1.receiveInitialCard(new Card(3));
        player1.receiveInitialCard(new Card(5));
        player1.logInitialHand();

        player1.writePlayerOutputFile();

        File out = new File("player1_output.txt");
        assertTrue(out.exists());
        String content = new String(Files.readAllBytes(out.toPath()));
        
        assertTrue(content.contains("player 1 initial hand 2 2 3 5"));
        assertTrue(content.contains("player 3 has informed player 1"));
        out.delete();
    }
}