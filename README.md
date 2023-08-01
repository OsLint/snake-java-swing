# 🐍 Snake Game in Java Swing 🎮

## Description

This is a classic Snake game implemented in Java using the Swing framework. The game allows players to control a snake and navigate it around the game board to collect food. As the snake eats the food, it grows longer, and the player's goal is to achieve the highest possible score without colliding with the game's boundaries or the snake's own body.

## Features 🎯

- Simple and intuitive controls: Use arrow keys (Up, Down, Left, Right) to control the snake's direction.
- Score tracking: The game keeps track of the player's score based on the number of food items collected and time/lenght of snake.
- Game Over condition: The game ends if the snake collides with the boundary of the game board or with its own body.
- Custom Food Items: The game includes various custom food items with different effects:
    - 🍎 Apple: Gives points and an extra segment to the snake.
    - 🖤 Black Apple: Kills the snake on contact.
    - 🍏 Golden Apple: Gives extra points and an extra segment to the snake.
    - ✂️ Scissors: Cuts off all segments of the snake.

- Difficulty level: The speed of the snake depends on the lenght.

## Requirements 📋

- Java Development Kit (JDK) 8 or higher.

## How to Play 🕹️

1. Ensure you have Java installed on your system.
2. Download the `SnakeGame.jar` file from the repository's releases page.
3. Double-click on the `SnakeGame.jar` file or run the following command in the terminal:
   ```bash
   java -jar SnakeGame.jar
   ``` 
5. The game window will open, showing the snake and a food item on the game board.
6. Use the arrow keys to control the direction of the snake.
7. The snake will automatically move in the current direction.
8. Aim to eat the different types of food items by moving the snake over them. Each food item has a different effect.
9. The game ends if the snake collides with the game board boundary, its own body, or a Black Apple.
10. The game will continue until the player either wins by getting the maximum score or loses by colliding with obstacles.

## Screenshots 📷

![gameplay](https://github.com/oskalbarczyk/snake-java-swing/assets/106467480/2cf54a16-bc63-44a9-aeb1-607c1aecfbf6)


## Customization 🛠️

If you wish to modify the game or contribute to its development, you can clone the repository and make changes to the source code. The main game logic can be found in the `SnakeGame.java` file.

## Credits 🙌

This Snake game implementation was inspired by various online tutorials and examples. Special thanks to the Java community for providing valuable resources and documentation.

## License 📜

This Snake game is licensed under the [MIT License](./LICENSE). Feel free to use, modify, and distribute it as per the terms of the license.

## Author 👤

- Oskar Kalbarczyk
- [GitHub](https://github.com/oskalbarczyk)
- [Youtube](https://www.youtube.com/@codewithoskar/featured)
- [Twitter](https://twitter.com/oskalbarczyk)

For any issues or suggestions related to the game, please feel free to create an issue or submit a pull request in the GitHub repository.

Enjoy playing the Snake game! 🎉


