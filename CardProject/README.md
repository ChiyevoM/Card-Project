Multi-Threaded Card Game SimulationA concurrent Java simulation modeling a card game structured around a ring topology. In this simulation, $n$ players and $n$ card decks alternate in a circular arrangement. Each player operates on an independent thread, concurrently drawing from their left deck, checking for a winning hand of 4 matching cards, and discarding to their right deck.Project StructurePlaintextcardsTest/
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
PrerequisitesJava Development Kit (JDK 8 or higher) installed and added to your system path.A terminal or command-line interface.Building and Packaging (cards.jar)To compile the source code from scratch and build the executable JAR file (cards.jar), run the following commands from the project root directory:Bash# 1. Create the compilation output directory
mkdir -p bin

# 2. Compile all source files
javac -d bin src/*.java

# 3. Copy source files into bin to satisfy resource requirements if needed
cp src/*.java bin/

# 4. Create the executable jar package with CardGame as the main entry point
jar cef CardGame cards.jar -C bin .
Running the SimulationYou can run the game either via the pre-built cards.jar or directly from the compiled class files.Option A: Using the Executable JARBashjava -jar cards.jar
If no arguments are provided, the program will prompt you in the console for the number of players ($n$), automatically generate a valid pack file (pack.txt), clear out any stale output logs from prior runs, execute the simulation, and output final logs.Option B: Specifying Arguments ManuallyYou can pass the number of players and a custom pack file path directly:Bashjava -cp bin CardGame 4 test-pack/valid_pack_4players.txt
Output FilesUpon game completion, the simulation automatically generates:playerX_output.txt: Detailed logs for each player's actions (draws, discards, and win notifications).deckX_output.txt: Final remaining contents of each card deck in the ring.Note: The game automatically purges stale player and deck output logs from previous runs before starting.Running the Test SuiteThe project includes unit tests and concurrency stress tests powered by JUnit 5.To compile and execute the test suite using the bundled standalone console runner:Bash# 1. Ensure the bin directory is ready and compile both production and test code
mkdir -p bin
javac -d bin -cp lib/junit-platform-console-standalone.jar src/*.java test/*.java

# 2. Execute all tests across the classpath
java -jar lib/junit-platform-console-standalone.jar --class-path bin --scan-class-path
Concurrency & Design HighlightsThread Safety: Decks utilize thread-safe FIFO queues to prevent race conditions during concurrent player access.Lock-Free Victory Signaling: The simulation uses an AtomicInteger shared victory flag. The instant any player collects 4 matching cards, the flag is updated thread-safely, causing all player threads to gracefully terminate without deadlocking.Strict Pack Validation: PackGenerator and CardGame enforce rigorous validation on pack files, ensuring exact sizing ($8n$ cards), positive non-null integer values, and clean formatting.
