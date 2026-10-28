# Multi-Threaded Card Game Simulation

A concurrent Java simulation of a card game structured around a ring topology. In this simulation, *n* players and *n* card decks alternate in a circular arrangement. Each player operates on an independent thread, concurrently drawing from their left deck, checking for a winning hand of four matching cards, and discarding to their right deck.

## Project Structure

```text
cardsTest/
├── src/
│   ├── Card.java
│   ├── CardDeck.java
│   ├── CardGame.java
│   ├── PackGenerator.java
│   └── Player.java
├── test/
│   ├── CardDeckTest.java
│   ├── CardGameStressTest.java
│   ├── CardGameTest.java
│   ├── CardTest.java
│   ├── CardTestSuite.java
│   ├── PlayerTest.java
│   └── TestSession1.java
├── lib/
│   └── junit-platform-console-standalone.jar
├── test-pack/
│   ├── invalid_pack.txt
│   └── valid_pack_4players.txt
└── README.md
```

## Prerequisites

- **Java Development Kit (JDK 8 or higher)** installed and added to your system PATH.
- A terminal or command-line interface.

## Building and Packaging

To compile the source code and build the executable JAR file (`cards.jar`), run the following commands from the project root directory.

```bash
# 1. Create the compilation output directory
mkdir -p bin

# 2. Compile all source files
javac -d bin src/*.java

# 3. Copy source files into bin to satisfy resource requirements if needed
cp src/*.java bin/

# 4. Create the executable JAR with CardGame as the main entry point
jar cef CardGame cards.jar -C bin .
```

## Running the Simulation

The game can be run using the executable JAR or directly from the compiled class files.

### Option A: Using the Executable JAR

```bash
java -jar cards.jar
```

If no arguments are provided, the program prompts for the number of players (`n`), automatically generates a valid pack file (`pack.txt`), removes stale output logs from previous runs, executes the simulation, and generates the final logs.

### Option B: Specifying Arguments Manually

You can specify the number of players and a custom pack file path directly:

```bash
java -cp bin CardGame 4 test-pack/valid_pack_4players.txt
```

### Output Files

Upon game completion, the simulation automatically generates:

- **`playerX_output.txt`** — Detailed logs of each player's actions, including draws, discards, and win notifications.
- **`deckX_output.txt`** — The final remaining contents of each card deck in the ring.

**Note:** Stale player and deck output logs from previous runs are automatically removed before the simulation starts.

## Running the Test Suite

The project includes unit tests and concurrency stress tests powered by JUnit 5.

To compile and execute the tests using the bundled standalone console runner, run the following commands from the project root directory:

```bash
# 1. Create the compilation output directory
mkdir -p bin

# 2. Compile production and test code
javac -d bin -cp lib/junit-platform-console-standalone.jar src/*.java test/*.java

# 3. Execute all tests on the classpath
java -jar lib/junit-platform-console-standalone.jar --class-path bin --scan-class-path
```

## Concurrency and Design Highlights

- **Thread Safety:** Card decks use thread-safe FIFO queues to support concurrent player access and help prevent race conditions.
- **Atomic Victory Signalling:** The simulation uses a shared `AtomicInteger` victory flag. When a player collects four matching cards, the flag is updated thread-safely, allowing the other player threads to terminate without deadlocking.
- **Strict Pack Validation:** `PackGenerator` and `CardGame` validate pack files, checking the required number of cards (`8n`), positive integer values, and correct formatting.
- **Concurrent Player Execution:** Each player runs on an independent thread, allowing multiple players to draw and discard cards concurrently.
- **Automated Testing:** The test suite includes unit tests for individual components, integration tests for game behaviour, and stress tests for concurrent execution.

## Technologies Used

- **Java** — Core implementation and multithreading.
- **Java Concurrency Utilities** — Atomic variables and thread-safe data structures.
- **JUnit 5** — Unit testing and concurrency stress testing.

## Getting Started

1. Clone or download the repository.
2. Open a terminal in the project root directory.
3. Ensure Java is installed by running `java -version` and `javac -version`.
4. Build the executable JAR using the commands in [Building and Packaging](#building-and-packaging).
5. Run the simulation using the commands in [Running the Simulation](#running-the-simulation).
6. Execute the test suite using the commands in [Running the Test Suite](#running-the-test-suite).
