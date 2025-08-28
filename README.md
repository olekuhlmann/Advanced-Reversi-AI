# Advanced Reversi AI Player

This repository contains the source code for an AI player developed for a university course by a team of four students over one semester. The goal was to design and implement an intelligent agent capable of playing a complex, extended version of the classic board game Reversi against other teams in a client-server environment.

The primary challenge was to create an AI that could find the optimal move within a strict time budget for the entire game, necessitating **highly efficient algorithms and data structures**.


## 🎲 The Game: An Extended Reversi Challenge

Our AI was built to compete in a Reversi variant with several extensions that significantly increase the game's complexity from a computational standpoint:

  * **Larger & Custom Maps**: The game is played on various maps larger than the traditional 8x8 board.
  * **Multiplayer**: Games can involve more than two players, changing the dynamics of the game tree.
  * **Special Fields**: The board contains unique tiles like **bonus fields** (granting items), **bombs**, and **inversion tiles**.
  * **Special Transitions**: Certain maps feature "wrap-around" edges, where a tile on one edge is considered adjacent to a tile on the opposite edge.


## 🛠️ Technical Approach

The core of this project is the efficient implementation of search algorithms and game state representation. We focused on performance at every level to stay within the strict time limits.

### Efficient Data Structures

To handle the large state space and the need for rapid move simulation, we designed our data structures for maximum efficiency and a low memory footprint.

  * **Bit-Packed Game Board**: The game map is encoded as a `byte[][]` array. Each byte represents two adjacent tiles on the board, with 4 bits allocated per tile. This halves the memory required for the board and allows for very fast deep copies of the game state—a critical operation in game tree search.
  * **Byte-Encoded Moves**: Possible moves and their associated data are also encoded using bytes, making them lightweight and easy to pass through the system.

These choices were crucial for quickly simulating thousands of possible game futures during the AI's decision-making process.

### Advanced Algorithms

Finding the best move in a complex game like this is a classic AI problem. Our solution is built around a highly optimized game tree search algorithm.

  * **Minimax with Alpha-Beta Pruning**: The AI's brain is a **Minimax** algorithm that explores future moves to determine the best possible outcome. To make this feasible, we implemented **Alpha-Beta Pruning**, which dramatically reduces the number of nodes the algorithm needs to evaluate by eliminating branches that are provably worse than already-found solutions.

  * **Iterative Deepening**: To manage the time limit, our AI uses iterative deepening. It starts by searching for the best move at a shallow depth (e.g., 1 move ahead), and if time permits, it restarts the search with progressively deeper limits. The best move found in the last completed search is always available, ensuring the AI can return a good move even if it runs out of time during a deeper search.

  * **Sophisticated Heuristic Evaluation**: A powerful search algorithm is only as good as its evaluation function. We developed a detailed heuristic function (`MoveHeuristic.java`) that scores a given board state. It considers multiple factors, including:

      * **Piece Count**: The number of stones each player has.
      * **Mobility**: The number of possible moves available.
      * **Positional Advantage**: Corner and edge pieces are weighted more heavily as they are more stable.
      * **Frontier Stones**: We penalize having stones on the outer edge of a player's territory, as they are vulnerable to being flipped.
      * **Special Items**: The value of holding bombs or override stones.

  * **Pruning Enhancements**: To further improve the efficiency of Alpha-Beta Pruning, we also implemented:

      * **Move Ordering**: We perform a quick, shallow evaluation of possible moves to explore the most promising ones first. This significantly increases the number of branches that can be pruned.
      * **Aspiration Windows (Experimental)**: A technique that narrows the search window for alpha and beta values, which can lead to even faster pruning in certain situations.

## 🚀 How to Run

This project is built using **Apache Maven**. To run the AI client, you'll need **JDK 17** (or newer) and **Maven** installed on your system.

1.  **Build the Executable JAR**

    First, compile the source code and package it into a single executable JAR file. This command will create `swp2023_group5.jar` in the `target` directory.

    ```bash
    mvn clean package
    ```

2.  **Execute the AI Client**

    Once the build is complete, you can run the AI client from the command line. The client will attempt to connect to a game server.

    ```bash
    # Connect to a server running on localhost:7777
    java -jar target/swp2023_group5.jar -s 127.0.0.1 -p 7777
    ```

    You can pass various flags to configure the client's behavior:

      * `--server <ip>` or `-s <ip>`: Specify the server's IP address.
      * `--port <port>` or `-p <port>`: Specify the server's port number.
      * `--nopruning` or `-np`: Disable alpha-beta pruning to run pure Minimax.
      * `--verbose` or `-v`: Enable expanded output for debugging.
      * `--help` or `-h`: Display a full list of available commands.


## 👥 Contributors

This project was a collaborative effort by:

  * **Ole Kuhlmann** ([@olekuhlmann](https://github.com/olekuhlmann))
  * **Simon Peters**
  * **Yunjia**
  * **Luca**