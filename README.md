# Checka - Android Board Game

A premium native Android implementation of the "Checka" board game using Kotlin and Jetpack Compose.

## How to Run

1. Open the project in **Android Studio**.
2. Sync Gradle files (Internet connection required for dependencies).
3. Select the `app` configuration.
4. Run on an Emulator or Device (Minimum SDK 26 recommended for best performance).

## Features & Implementation

### 1. Pass-and-Play UI Flipping
To provide a seamless two-player offline experience without passing the device:
- The Board stays fixed in the center.
- **Player 2's UI** (Top Panel) is permanently rotated 180 degrees so the person sitting opposite can read it.
- **Controls Bar** dynamically moves and rotates:
    - On Player 1's turn, it appears at the bottom.
    - On Player 2's turn, it appears at the top, rotated 180 degrees.
- This logic is handled in `GameScreen.kt` using `Box` containers with conditional `Modifier.rotate(180f)`.

### 2. Wall Placement Logic
- Players have 10 walls.
- Walls span 2 cells and are placed on intersections (0..7 grid).
- **Validation**:
    - Walls cannot overlap or intersect (cross) existing walls.
    - **Path Rule**: A wall cannot be placed if it completely blocks **EITHER** player from reaching their goal row. This is verified using BFS (Breadth-First Search) in `Pathfinder.kt`.
- **Interaction**:
    - Toggle "Place Wall" mode.
    - Tap near an intersection (corners of cells). The game uses a hit-testing algorithm to find the nearest valid intersection.
    - A semi-transparent preview shows the wall; tap again to confirm.

### 3. Checka AI
The AI (`CheckaAI.kt`) has three difficulty levels:
- **Easy**: Greedy approach. Always tries to move closer to the goal. Adds random walls rarely (10% chance) to spice things up.
- **Hard**: Balanced. Evaluates move distance. 30% chance to consider defensive wall placement if the player is winning.
- **Master**: 
    - **Exhaustive Evaluation**: Simulates placing a wall at every possible legal position (128 candidates).
    - Selects the wall that **maximizes the increase** in the human player's path length to the goal.
    - If no wall is effective, it plays the optimal shortest-path move.

### 4. Architecture
- **MVVM**: `GameViewModel` manages `GameState` flow.
- **Pure Domain**: `GameEngine`, `Pathfinder`, and `CheckaAI` are pure Kotlin objects with no Android dependencies, making them testable.
- **Room Database**: Stores match results for the local Leaderboard.
- **DataStore**: Persists Theme preferences.
