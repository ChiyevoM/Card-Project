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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * CardDeck.java
 * Thread-safe FIFO container holding Card objects.
 * Players draw from the top (front) and discard to the bottom (back).
 */
public class CardDeck {
    private final int deckId;
    private final Queue<Card> cards = new LinkedList<>();

    /**
     * Constructs a CardDeck with a positive integer identifier (1 to n).
     *
     * @param deckId Positive integer deck number
     */
    public CardDeck(int deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be a positive integer.");
        }
        this.deckId = deckId;
    }

    public int getDeckId() {
        return deckId;
    }

    /**
     * Adds a card to the bottom (back) of the deck and notifies waiting threads.
     *
     * @param card Card to add to the deck
     */
    public synchronized void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add a null card to the deck.");
        }
        cards.add(card);
        notifyAll();
    }

    /**
     * Draws and removes a card from the top (front) of the deck.
     *
     * @return The top Card, or null if the deck is empty
     */
    public synchronized Card drawCard() {
        return cards.poll();
    }

    /**
     * Draws a card from the top of the deck in a multithreaded game, waiting briefly
     * if the deck is currently empty unless the game has already been won.
     *
     * @param gameWon Shared flag indicating whether a player has already won
     * @return The top Card, or null if the game ended while waiting
     */
    public synchronized Card drawCard(AtomicBoolean gameWon) {
        while (cards.isEmpty()) {
            if (gameWon.get()) {
                return null;
            }
            try {
                wait(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return cards.poll();
    }

    public synchronized int size() {
        return cards.size();
    }

    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Formats the deck contents as space-separated card values.
     */
    public synchronized String getContentsAsString() {
        return cards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Writes the final contents of the deck to deckX_output.txt.
     * Example output: "deck2 contents: 1 3 3 7"
     */
    public synchronized void writeDeckOutputFile() {
        String filename = "deck" + deckId + "_output.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            String contents = getContentsAsString();
            if (contents.isEmpty()) {
                writer.write("deck" + deckId + " contents:");
            } else {
                writer.write("deck" + deckId + " contents: " + contents);
            }
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing output file for deck " + deckId + ": " + e.getMessage());
        }
    }
}
