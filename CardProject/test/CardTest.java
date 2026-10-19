import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Card.java.
 */
public class CardTest {

    @Test
    public void testValidCardCreationAndZeroBoundary() {
        Card zeroCard = new Card(0);
        Card normalCard = new Card(7);

        assertEquals(0, zeroCard.getValue());
        assertEquals(7, normalCard.getValue());
        assertEquals("7", normalCard.toString());
    }

    @Test
    public void testNegativeCardThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Card(-1)
        );
        assertEquals("Card value must be non-negative.", exception.getMessage());
    }

    @Test
    public void testEqualsAndHashCode() {
        Card card1 = new Card(4);
        Card card2 = new Card(4);
        Card card3 = new Card(9);

        assertEquals(card1, card1); // Reflexive
        assertEquals(card1, card2); // Equal values
        assertEquals(card1.hashCode(), card2.hashCode());

        assertNotEquals(card1, card3); // Different values
        assertNotEquals(card1, null);  // Null check
        assertNotEquals(card1, "4");   // Different class check
    }
}
