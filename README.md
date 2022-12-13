# Dungeon of Doom

This is a simple game called Dungeon of Doom. It is a text-based game where you can explore a dungeon and collect gold, to escape the bot. The game is written in Java 1.8.0.

## How to run the game

To run the game, you need to have Java 1.8.0 installed on your computer. You can download it from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

To run the game, you need to compile the source code. You can do this by running the following command in the root directory of the project:

    javac GameLogic.java

After the source code is compiled, you can run the game by running the following command:
    
        java GameLogic

## How to play the game

The game is a text-based game, where you can explore the dungeon and collect gold. You can move around the dungeon by typing the following commands:

    MOVE N - Move north
    MOVE S - Move south
    MOVE E - Move east
    MOVE W - Move west

You can also pick up gold by typing the following command:
    
        PICKUP

You can also view a 5x5 map of the dungeon around the player by typing the following command:
    
        LOOK

You can also quit the game if you are on the exit tile by typing the following command:
    
        QUIT

## Explanation of some of the code

The game is written in Java 1.8.0. The game is split into five classes:
 - `GameLogic.java` - This is the main class of the game. It contains the main method, which is the entry point of the game. It also contains the game loop, which is the main loop of the game.

 - `Map.java` - This class contains the map of the game. It contains the map, which is a two-dimensional array of `char`'s.

- `Player.java` - This class contains the player of the game. It contains the player's position on the map, and the amount of gold the player has collected.

- `BotPlayer.java` - This class contains the bot player of the game. It is an extension of the `Player` class. It is also an AI player, which means that it can move around the map by itself, it uses the A* algorithm to find the shortest path to the player/gold/exit. I used this [video](https://youtu.be/2JNEme00ZFA) for a guide to implement the A* algorithm.

- `Node.java` - This class contains a node of the A* algorithm. It contains the position of the node, the parent node, the costs of the node, and the type of the node, e.g. if it is a wall, or if it is the player, or if it is the exit.