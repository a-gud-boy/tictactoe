# Tic-Tac-Toe Android App

This is a simple Android application that allows users to play Tic-Tac-Toe.

## Features

- Classic Tic-Tac-Toe gameplay
- Infinite Tic-Tac-Toe mode
- AI opponent with multiple difficulty levels
- Simple and intuitive user interface
- Sound effects for game actions

## Game Modes

### Normal Tic-Tac-Toe
This is the classic 3x3 game where players aim to get three of their marks (X or O) in a row, column, or diagonal.

### Infinite Tic-Tac-Toe
A variation on the 3x3 grid where players' marks disappear after a set number of subsequent moves by *either* player. For example, if the maximum visible moves per player is 3 (as implemented), a player's oldest mark disappears when they make their 4th move. This process happens independently for each player. The goal remains to get three of *your currently visible* marks in a row, column, or diagonal. This mode adds a dynamic layer of strategy as the board constantly evolves.

### AI Opponent
- Both "Normal" and "Infinite" Tic-Tac-Toe modes support playing against an AI opponent.
- Players can choose the AI's difficulty level (Easy, Medium, Hard), allowing for a customizable challenge.

## How to Build

1. **Prerequisites:**
    - Android Studio (latest version recommended)
    - Android SDK (latest version)
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
