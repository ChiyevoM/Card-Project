import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger; // 1. Added Import

/**
 * CardGame.java
 * Main entry point and controller for the card game simulation.
 */
public class CardGame {

    private final int numPlayers;
    private final List<Card> pack;
    private final List<Player> players;
    private final List<CardDeck> decks;
    private final AtomicInteger sharedWinner; // 2. Added Shared Variable

    public CardGame(int numPlayers, String packFilePath) throws IOException, IllegalArgumentException {
        if (numPlayers <= 0) {
            throw new IllegalArgumentException("Number of players must be greater than 0.");
        }
        this.numPlayers = numPlayers;
        this.pack = loadAndValidatePack(packFilePath, numPlayers);
        this.players = new ArrayList<>();
        this.decks = new ArrayList<>();
        this.sharedWinner = new AtomicInteger(0); // 3. Initialized to 0
    }

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

    public void setupGame() {
        for (int i = 1; i <= numPlayers; i++) {
            decks.add(new CardDeck(i));
        }

        for (int i = 1; i <= numPlayers; i++) {
            CardDeck leftDeck = decks.get(i - 1);
            CardDeck rightDeck = decks.get(i % numPlayers);
            // 4. Passed sharedWinner into the Player constructor
            players.add(new Player(i, leftDeck, rightDeck, sharedWinner)); 
        }
        
        int cardIndex = 0;
        for (int round = 0; round < 4; round++) {
            for (Player player : players) {
                player.receiveInitialCard(pack.get(cardIndex++));
            }
        }

        while (cardIndex < pack.size()) {
            for (CardDeck deck : decks) {
                if (cardIndex < pack.size()) {
                    deck.addCard(pack.get(cardIndex++));
                }
            }
        }
    }

    public void startGame() {
        List<Thread> threads = new ArrayList<>();
        for (Player player : players) {
            Thread t = new Thread(player, "PlayerThread-" + player.getPlayerId());
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 5. Added the final win announcement
        System.out.println("Game over. Player " + sharedWinner.get() + " wins!");
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<CardDeck> getDecks() {
        return decks;
    }

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
                        scanner.next(); 
                    }
                }
            }

            // Automatically set filePath and generate without prompting again
            filePath = "pack.txt";
            try {
                PackGenerator.generatePack(n, filePath);
                System.out.println("Using automatically generated pack file: " + filePath);
            } catch (IOException e) {
                System.err.println("Error generating pack file: " + e.getMessage());
                return;
            }
        }

        try {
            // Note: I removed Player.resetGameWon() here because we are using AtomicInteger now instead!
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