import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * CardGame.java
 * Main entry point and controller for the card game simulation.
 * Handles CLI input validation, pack file parsing, round-robin dealing,
 * setting up the ring topology, and launching concurrent player threads.
 */
public class CardGame {

    private final int numPlayers;
    private final List<Card> pack;
    private final List<Player> players;
    private final List<CardDeck> decks;

    public CardGame(int numPlayers, String packFilePath) throws IOException, IllegalArgumentException {
        if (numPlayers <= 0) {
            throw new IllegalArgumentException("Number of players must be greater than 0.");
        }
        this.numPlayers = numPlayers;
        this.pack = loadAndValidatePack(packFilePath, numPlayers);
        this.players = new ArrayList<>();
        this.decks = new ArrayList<>();
    }

    /**
     * Loads and validates the card pack file against specification rules:
     * 1. Must exist and be readable.
     * 2. Must contain exactly 8n integers (where n = numPlayers).
     * 3. All integer values must be non-negative.
     */
    public static List<Card> loadAndValidatePack(String filePath, int n) throws IOException, IllegalArgumentException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Pack file does not exist or is invalid: " + filePath);
        }

        List<Card> loadedCards = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                try {
                    int val = Integer.parseInt(line);
                    if (val < 0) {
                        throw new IllegalArgumentException("Line " + lineNumber + ": Card value cannot be negative (" + val + ").");
                    }
                    loadedCards.add(new Card(val));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": Non-integer card value found (\"" + line + "\").");
                }
            }
        }

        int requiredCards = 8 * n;
        if (loadedCards.size() != requiredCards) {
            throw new IllegalArgumentException("Invalid pack size. Expected " + requiredCards + " cards (8 * " + n + "), but found " + loadedCards.size() + ".");
        }

        return loadedCards;
    }

    /**
     * Instantiates Decks and Players, establishes Ring Topology,
     * and performs Round-Robin dealing to hands and decks.
     */
    public void setupGame() {
        // 1. Instantiate N Decks
        for (int i = 1; i <= numPlayers; i++) {
            decks.add(new CardDeck(i));
        }

        // 2. Instantiate N Players in Ring Topology
        // Player i draws from Deck i (left) and discards to Deck i+1 (right, wrapping around)
        for (int i = 1; i <= numPlayers; i++) {
            CardDeck leftDeck = decks.get(i - 1);
            CardDeck rightDeck = decks.get(i % numPlayers);
            players.add(new Player(i, leftDeck, rightDeck));
        }

        // 3. Round-Robin deal 4 cards to each player
        int cardIndex = 0;
        for (int round = 0; round < 4; round++) {
            for (Player player : players) {
                player.receiveInitialCard(pack.get(cardIndex++));
            }
        }

        // 4. Round-Robin deal remaining 4n cards to decks
        while (cardIndex < pack.size()) {
            for (CardDeck deck : decks) {
                if (cardIndex < pack.size()) {
                    deck.addCard(pack.get(cardIndex++));
                }
            }
        }
    }

    /**
     * Starts all player threads concurrently and waits for the simulation to finish.
     */
    public void startGame() {
        List<Thread> threads = new ArrayList<>();
        for (Player player : players) {
            Thread t = new Thread(player, "PlayerThread-" + player.getPlayerId());
            threads.add(t);
            t.start();
        }

        // Block until all player threads terminate
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Write deck log files upon completion
        for (CardDeck deck : decks) {
            deck.writeDeckLog();
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<CardDeck> getDecks() {
        return decks;
    }

    /**
     * Interactive CLI input gathering when non-command-line arguments are given.
     * Uses try-with-resources to ensure zero Scanner resource leaks.
     */
    public static void main(String[] args) {
        int n = 0;
        String filePath = "";

        if (args.length == 2) {
            try {
                n = Integer.parseInt(args[0]);
                filePath = args[1];
            } catch (NumberFormatException e) {
                System.out.println("Invalid command line argument for number of players.");
                return;
            }
        } else {
            // Scanner wrapped in try-with-resources to eliminate resource leaks
            try (Scanner scanner = new Scanner(System.in)) {
                while (n <= 0) {
                    System.out.print("Please enter the number of players: ");
                    if (scanner.hasNextInt()) {
                        n = scanner.nextInt();
                        if (n <= 0) {
                            System.out.println("Number of players must be a positive integer.");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a valid integer.");
                        scanner.next(); // Clear invalid token
                    }
                }

                System.out.print("Please enter location of pack file: ");
                filePath = scanner.next();
            }
        }

        try {
            Player.resetGameWon();
            CardGame game = new CardGame(n, filePath);
            game.setupGame();
            System.out.println("Game setup successful. Starting multithreaded simulation...");
            game.startGame();
            System.out.println("Game finished successfully! Log output files generated.");
        } catch (Exception e) {
            System.err.println("Error running game: " + e.getMessage());
        }
    }
}