public class TestSession1 {
    public static void main(String[] args) {
        System.out.println("=== TESTING SESSION 1 DOMAIN MODELS ===");

        // Test Card
        Card c1 = new Card(5);
        System.out.println("Card 1 Value: " + c1.getValue() + " (Expected: 5)");

        // Test CardDeck FIFO
        CardDeck deck = new CardDeck(1);
        deck.addCard(new Card(10));
        deck.addCard(new Card(20));

        Card drawn1 = deck.drawCard();
        Card drawn2 = deck.drawCard();

        System.out.println("First drawn: " + drawn1.getValue() + " (Expected: 10)");
        System.out.println("Second drawn: " + drawn2.getValue() + " (Expected: 20)");

        if (drawn1.getValue() == 10 && drawn2.getValue() == 20) {
            System.out.println("\nSUCCESS: Card and CardDeck FIFO order are working!");
        } else {
            System.out.println("\nFAILURE: FIFO order is incorrect!");
        }
    }
 {
    
}
}
