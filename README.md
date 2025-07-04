# Tic-Tac-Toe Android App

This is a simple Android application that allows users to play Tic-Tac-Toe.

## Features

- Classic Tic-Tac-Toe gameplay
- Infinite Tic-Tac-Toe mode
- AI opponent with multiple difficulty levels
- Intuitive user interface with navigation drawer
- Sound effects for game actions
- Haptic feedback for game actions
- Configurable settings (Sound, AI, Haptic Feedback, AI Difficulty)
- In-app help section with game rules and FAQs

## Navigation and UI

The app features an intuitive user interface designed for ease of use:

- **Navigation Drawer:** Access all major sections of the app—Normal Tic-Tac-Toe, Infinite Tic-Tac-Toe, Match History, Settings, and Help—through a convenient navigation drawer, typically opened by swiping from the left edge of the screen or tapping the menu icon.
- **Info Dialogs:** Each main screen (Normal Game, Infinite Game, Match History, Settings, and Help) includes an information icon in the top bar. Tapping this icon reveals a dialog box with a brief description or help text relevant to the current screen, offering quick guidance. The Match History screen, for example, also features context-specific actions in its top app bar, such as a "Clear History" button when viewing the main list of matches.

## Game Modes

### Normal Tic-Tac-Toe
This is the classic 3x3 game where players aim to get three of their marks (X or O) in a row, column, or diagonal.

### Infinite Tic-Tac-Toe
A variation on the 3x3 grid where each player's marks have a limited lifespan. Marks are removed after a set number of subsequent moves *made by that same player*. For example, if a player can have a maximum of 3 marks visible (as implemented), their oldest mark disappears when they place their 4th mark. This process happens independently for each player. The goal remains to get three of *your currently visible* marks in a row, column, or diagonal. This mode adds a dynamic layer of strategy as the board constantly evolves.

### AI Opponent
- Both "Normal" and "Infinite" Tic-Tac-Toe modes support playing against an AI opponent. Configuration options for the AI, such as difficulty level and enabling/disabling AI play, can be found in the "Settings" section.

## Settings

The app provides a dedicated Settings page where you can customize your gameplay experience:

- **Sound Effects:** Toggle game sounds (e.g., for placing marks, game over) on or off.
- **Play vs AI:** Enable or disable playing against the computer opponent. When disabled, games will be in two-player (human vs human) mode on the same device.
- **Haptic Feedback:** Turn on or off vibrational feedback for game actions like placing a mark or game events.
- **AI Difficulty:** When "Play vs AI" is enabled, you can adjust the AI's skill level. Choose from:
    - Easy
    - Medium
    - Hard

## Help

A dedicated Help page is available within the app to assist users. It can be accessed via the navigation drawer and provides:

- **Game Rules:** A clear explanation of the rules for classic Tic-Tac-Toe.
- **Frequently Asked Questions (FAQs):** Answers to common questions about gameplay, features (like AI and different modes), and settings.

## Match History and Replay

The app includes a comprehensive Match History feature, accessible from the main navigation. This section allows users to:

- View a list of all past matches, with the most recent matches appearing first.
- For each match, see:
    - A match number (e.g., "Match #5").
    - The outcome (e.g., "You Won", "AI Won", "Draw"), visually highlighted with a color code (e.g., green for win, red for loss, gray for draw).
    - The date and time of the match (displayed relatively for recent matches, e.g., "2 hr. ago", and as a full date/time for older matches).
    - The final score (e.g., "You: 2 – AI: 1").
- View overall statistics, including:
    - Total matches played.
    - Number of matches won by the player.
    - Number of matches won by the AI.
    - Number of draws.
- Delete individual matches from the history (with a confirmation prompt).
- Clear all match history (with a confirmation prompt).
- Tap on a specific match in the list to navigate to a "Match Details" screen. This screen shows a breakdown of each round within that match. From there, users can select a specific round to view a "Round Replay" screen, which visually reconstructs the selected round move by move on a game board, including descriptive locations for each placement (e.g., "Player X placed X in top-left").

## How to Build

1. **Prerequisites:**
    - Android Studio (latest version recommended)
    - Android SDK (latest version)
    - JDK 11 (Java Development Kit)
2. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/your-repository-name.git # Replace with the actual repository URL
   ```
3. **Open the project in Android Studio.**
4. **Build the project:**
    - Select `Build > Make Project` from the Android Studio menu.
5. **Run the app:**
    - Select `Run > Run 'app'` from the Android Studio menu.
    - Choose an available emulator or connect a physical device.

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these guidelines:

- **Reporting Bugs:** If you find a bug, please open an issue in the GitHub repository, providing as much detail as possible.
- **Suggesting Enhancements:** If you have an idea for a new feature or an improvement to an existing one, please open an issue to discuss it.
- **Pull Requests:**
    1. Fork the repository.
    2. Create a new branch for your feature or bug fix (`git checkout -b feature/your-feature-name` or `bugfix/issue-number`).
    3. Make your changes.
    4. Commit your changes (`git commit -m 'Add some feature'`).
    5. Push to the branch (`git push origin feature/your-feature-name`).
    6. Open a pull request.

Please make sure your code follows the project's coding style and includes appropriate tests.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
