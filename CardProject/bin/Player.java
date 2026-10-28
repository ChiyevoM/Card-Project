import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Player.java
 * Thread-safe representation of a player in the card game simulation.
 * Implements Runnable and FIFO non-preferred discard strategy to prevent stagnation.
 */
public class Player implements Runnable {

    private final int playerId;
    private final CardDeck leftDeck;
    private final CardDeck rightDeck;
    private final LinkedList<Card> hand = new LinkedList<>();
    private final List<String> logHistory = new ArrayList<>();

    // Shared thread-safe game state tracking across all player instances
    private static final AtomicBoolean gameWon = new AtomicBoolean(false);
    private final AtomicInteger sharedWinner;

   public Player(int playerId, CardDeck leftDeck, CardDeck rightDeck, AtomicInteger sharedWinner) {
    if (playerId <= 0) {
        throw new IllegalArgumentException("Player ID must be a positive integer.");
    }
    this.playerId = playerId;
    this.leftDeck = leftDeck;
    this.rightDeck = rightDeck;
    this.sharedWinner = sharedWinner;
}

public Player(int playerId, CardDeck leftDeck, CardDeck rightDeck) {
    this(playerId, leftDeck, rightDeck, new AtomicInteger(-1));
}

    public static AtomicBoolean getGameWonFlag() {
        return gameWon;
    }

    /**
     * Resets shared game victory state before starting a new game session.
     */
    public static void resetGameWon() {
    gameWon.set(false);
    // Remove sharedWinner.set(-1) from here!
}

    public int getPlayerId() {
        return playerId;
    }

    public synchronized List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    /**
     * Adds a card to the player's hand during initial round-robin dealing.
     */
    public synchronized void receiveInitialCard(Card card) {
        hand.addLast(card);
    }

    /**
     * Logs the initial 4-card hand at the start of the game.
     */
    public synchronized void logInitialHand() {
        logHistory.add("player " + playerId + " initial hand " + formatHand());
    }

    /**
     * Checks if the player holds 4 cards of the exact same face value.
     */
    public synchronized boolean hasWinningHand() {
        if (hand.size() != 4) {
            return false;
        }
        int firstValue = hand.getFirst().getValue();
        for (Card c : hand) {
            if (c.getValue() != firstValue) {
                return false;
            }
        }
        return true;
    }

    /**
     * Chooses and removes a non-preferred card from the hand in strict FIFO order.
     */
    public synchronized Card selectCardToDiscard() {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getValue() != playerId) {
                return hand.remove(i);
            }
        }
        return hand.removeFirst();
    }

    /**
     * Thread execution loop for player turn management.
     */
    @Override
    public void run() {
        logInitialHand();

        // Check for immediate win from initial dealing
        if (hasWinningHand()) {
            if (gameWon.compareAndSet(false, true)) {
                sharedWinner.set(playerId);
                System.out.println("player " + playerId + " wins");
                logHistory.add("player " + playerId + " wins");
            }
        }

        // Action loop: runs until any player thread sets gameWon to true
        while (!gameWon.get()) {
            Card drawnCard = leftDeck.drawCard(gameWon);
            if (drawnCard == null) {
                break;
            }

            synchronized (this) {
                hand.addLast(drawnCard);
                logHistory.add("player " + playerId + " draws a " + drawnCard.getValue()
                        + " from deck " + leftDeck.getDeckId());

                Card discardedCard = selectCardToDiscard();
                rightDeck.addCard(discardedCard);
                logHistory.add("player " + playerId + " discards a " + discardedCard.getValue()
                        + " to deck " + rightDeck.getDeckId());

                logHistory.add("player " + playerId + " current hand is " + formatHand());

                if (hasWinningHand()) {
                    if (gameWon.compareAndSet(false, true)) {
                        sharedWinner.set(playerId);
                        System.out.println("player " + playerId + " wins");
                        logHistory.add("player " + playerId + " wins");
                    }
                    break;
                }
            }
        }

        // Finalize logs and write output file upon game conclusion
        writePlayerOutputFile();
    }

    /**
     * Formats the current hand as space-separated integers.
     */
    public synchronized String formatHand() {
        return hand.stream()
                .map(Card::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Appends final win/loss log lines and writes playerX_output.txt.
     */
    public synchronized void writePlayerOutputFile() {
        int winner = sharedWinner.get();

        if (winner == playerId) {
            logHistory.add("player " + playerId + " exits");
            logHistory.add("player " + playerId + " final hand: " + formatHand());
        } else {
            logHistory.add("player " + winner + " has informed player " + playerId
                    + " that player " + winner + " has won");
            logHistory.add("player " + playerId + " exits");
            logHistory.add("player " + playerId + " hand: " + formatHand());
        }

        String filename = "player" + playerId + "_output.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : logHistory) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing output file for player " + playerId + ": " + e.getMessage());
        }
    }
}