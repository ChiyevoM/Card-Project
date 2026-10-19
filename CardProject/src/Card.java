
/**
 * Card.java
 * Immutable and inherently thread-safe representation of a card in the simulation.
 */
public class Card {
    private final int value;

    /**
     * Constructs a Card with a non-negative integer face value.
     *
     * @param value Non-negative integer denomination
     */
    public Card(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Card value must be non-negative.");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return value == card.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
