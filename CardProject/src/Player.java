import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Player.java
 * Thread-safe representation of a player in the card game simulation.
 * Implements FIFO non-preferred discard strategy to prevent stagnation.
 */
public class Player {
    private final int playerId;
    private final CardDeck leftDeck;
    private final CardDeck rightDeck;
    private final LinkedList<Card> hand = new LinkedList<>();
    private final List<String> logHistory = new ArrayList<>();

    public Player(int playerId, CardDeck leftDeck, CardDeck rightDeck) {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Player ID must be a positive integer.");
        }
        this.playerId = playerId;
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
    }

    public int getPlayerId() {
        return playerId;
    }

    public synchronized List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    /**
     * Adds a card to the player's hand during the initial round-robin deal.
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
     * Chooses and removes a non-preferred card from the hand.
     * Drawn cards are added to the back (addLast) and we scan from the front (index 0),
     * ensuring non-preferred cards rotate out in strict FIFO order and never stagnate.
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
     * Executes a single atomic draw-and-discard turn.
     * NOTE: Make sure leftDeck.drawCard() and rightDeck.addCard() match Partner A's CardDeck method names!
     */
    public synchronized boolean takeAtomicTurn() {
        Card drawnCard = leftDeck.drawCard();
        if (drawnCard == null) {
            return false;
        }

        hand.addLast(drawnCard);
        logHistory.add("player " + playerId + " draws a " + drawnCard.getValue()
                + " from deck " + leftDeck.getDeckId());

        Card discardedCard = selectCardToDiscard();
        rightDeck.addCard(discardedCard);
        logHistory.add("player " + playerId + " discards a " + discardedCard.getValue()
                + " to deck " + rightDeck.getDeckId());

        logHistory.add("player " + playerId + " current hand is " + formatHand());

        return hasWinningHand();
    }

    /**
     * Formats the current hand as space-separated integers (e.g., "1 1 2 4").
     */
    public synchronized String formatHand() {
        return hand.stream()
                .map(Card::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Appends final win/loss log lines and writes playerX_output.txt.
     */
    public synchronized void writePlayerOutputFile(int winningPlayerId) {
        if (winningPlayerId == playerId) {
            logHistory.add("player " + playerId + " wins");
            logHistory.add("player " + playerId + " exits");
            logHistory.add("player " + playerId + " final hand: " + formatHand());
        } else {
            logHistory.add("player " + winningPlayerId + " has informed player " + playerId
                    + " that player " + winningPlayerId + " has won");
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

