# Multi-Threaded Java Card Game Simulation

A concurrent Java application simulating a $N$-player card game in a circular ring topology using thread-safe synchronization, FIFO card queues, and atomic victory tracking.

---

## 📌 Status: Session 5 Complete

- **Current Phase**: Concurrency Debugging & Deadlock Prevention
- **Build Status**: Decoupled source code, clean compilation (`javac -d bin src/*.java`)
- **Testing**: Integrated JUnit 5 stress-testing for non-blocking `wait(10)` execution and lock-free win state evaluation

---

## 🏛️ System Overview

Players ($1 \dots N$) and Decks ($1 \dots N$) form a circular ring:
- **Player $i$** draws from **Deck $i$** and discards non-preferred cards to **Deck $i+1 \pmod N$**.
- Draws and discards follow strict **First-In, First-Out (FIFO)** order.
- The first player to hold 4 matching cards signals victory via an `AtomicBoolean`, prompting all player threads to gracefully exit and write execution logs.

---

## ⚙️ Concurrency Highlights

1. **Deadlock Prevention**: `CardDeck.drawCard(AtomicBoolean)` uses a timed `wait(10)` loop so empty-deck waits regularly re-evaluate the global win state and unblock cleanly.
2. **Thread Safety**: Synchronized `CardDeck` operations with `notifyAll()` calls.
3. **Atomic Victory**: Lock-free coordination via `AtomicBoolean` and `AtomicInteger`.

---

## 🛠️ Quick Commands

### Compile
```bash
javac -d bin src/*.java
