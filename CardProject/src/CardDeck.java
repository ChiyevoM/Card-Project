import java.util.LinkedList;
import java.util.Queue;

/**
 * CardDeck.java
 * Thread-safe, ordered FIFO container representing a deck of cards.
 */
public class CardDeck {
    private final int id;
    private final Queue<Card> cards;

    /**
     * Constructs a CardDeck with a given deck identifier.
     *
     * @param id The 1-based index of the deck
     */
    public CardDeck(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Deck ID must be a positive integer.");
        }
        this.id = id;
        this.cards = new LinkedList<>();
    }

    public int getId() {
        return id;
    }

    /**
     * Adds a card to the bottom (back) of the deck (FIFO discard).
     *
     * @param card The card to add
     */
    public synchronized void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add a null card to the deck.");
        }
        cards.add(card);
    }

    /**
     * Draws and removes a card from the top (front) of the deck (FIFO draw).
     *
     * @return The drawn card, or null if the deck is empty
     */
    public synchronized Card drawCard() {
        return cards.poll();
    }

    /**
     * Returns the number of cards currently in the deck.
     */
    public synchronized int size() {
        return cards.size();
    }

    /**
     * Checks if the deck contains no cards.
     */
    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns a space-separated string of card values in FIFO order for deck output files.
     * Example output format: "1 3 3 7"
     */
    public synchronized String getContentsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.getValue()).append(" ");
        }
        return sb.toString().trim();
    }
}