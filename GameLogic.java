/**
 * Contains the main logic part of the game, as it processes.
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.io.IOException;


public class GameLogic {
	
	private Map map;
	private Player player;
	private BotPlayer bot;

	private boolean gameRunning = false;

	private String guide = "\n HELLO - Number of gold to win \n GOLD - Gold owned \n PICKUP - Collect gold \n MOVE <direction> - N,W,S,E \n LOOK - Show map";

	/**
	 * Default constructor
	 */
	public GameLogic() throws Exception {
		gameRunning = true;
		banner();
		chooseMap();

		player = new Player(map);
		player.updatePosition(initalPosition());

		bot = new BotPlayer(map);
		bot.updatePosition(initalPosition());
	}

	/**
	 * Set's the player's initial position, which is a random position.
	 * 
	 * @return int[] - The initial position
	 */
	private int[] initalPosition () {

		int x = (int) (Math.random() * map.mapWidth());
		int y = (int) (Math.random() * map.mapHeight());

		if (map.isWall(x, y) || (player.position()[0] == x && player.position()[1] == y)) {
			// If the position is a wall or the same as the player's position (for bot), try again
			return initalPosition();
		}

		return new int[] {x, y};
	}

	/**
	 * Prints the banner
	 * 
	 * @throws IOException
	 */
	public void banner() throws IOException {
		String bannerFile = "banner/banner.txt";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(bannerFile)));
			String line = reader.readLine();
			while (line != null) {
				// Print the line, then read the next line,
				// and repeat, until there are no more lines
				System.out.println(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Error reading banner file");
		}
	}

	/**
	 * Allows the player to choose the map they wish to play on.
	 * 
	 * @throws IOException
	 */
	public void chooseMap() throws IOException {
		String mapName = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Please enter the name of the map you wish to play on: ");
		try {
			mapName = br.readLine(); // Read the map name
		} catch (IOException e){
			System.out.print("something went wrong on input.");
		}

		// Set the map name based on the input
		switch (mapName) {
			case "small":
				mapName = "small_example_map.txt";
				break;
			case "medium":
				mapName = "medium_example_map.txt";
				break;
			case "large":
				mapName = "large_example_map.txt";
				break;
			case "":
				mapName = "small_example_map.txt";
				break;
		}

		// Create the map
		map = new Map("maps/" + mapName);
	}

	/**
	 * Starts the game
	 * 
	 * @throws IOException
	 */
	public void play() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(gameRunning) {
			String command = "";

			// Read the input
		try {
			command = reader.readLine();
		} catch (IOException e) {
			System.out.println("Error reading input");
		}

		// Process the players' input
		processInput(command, player);
		processInput(bot.makeMove(), bot);

		// Check if the bot has caught the player
		if (Arrays.equals(player.position(), bot.position())) {
			System.out.println("You have been caught by the bot!");
			System.out.println("LOSE");
			gameRunning = false;
		}

		System.out.println(map.displayFullMap(player, bot)); // delete
		}
	}

	/**
	 * Processes the input from the player or bot and calls the appropriate method
	 * 
	 * @param command - The command the user has entered
	 * @param user - The player or bot
	 */
	public void processInput(String command, Player user) {
		command = command.toUpperCase();
		String[] input = command.split(" "); // Needed for MOVE command
		
		switch (input[0]) {
			case "MOVE":
				if (input.length < 2) { // If the user has not entered a direction
					System.out.println("Invalid command");
				} else {
					move(input[1].charAt(0), user); // Move in the specified direction
				}
				break;
			case "LOOK":
				look(user);
				break;
			case "PICKUP":
				pickup(user);
				break;
			case "GOLD":
				System.out.println("Gold owned: " + player.gold());
				break;
			case "HELP":
				System.out.println(guide);
				break;
			case "QUIT":
				System.out.println(quit(user));
				break;
			default:
				System.out.println("Invalid command");
				break;
		}
	}
	

	/**
	 * Moves the player in the specified direction.
	 *
	 * @param : Direction to move in.
	 * @return : If the move was successful.
	 */
	public void move(char direction, Player user) {
		int[] pos = user.position();
		int x = pos[0];
		int y = pos[1];

		// Update the position based on the direction
		switch (direction) {
            case 'N':
                y--;
                break;
            case 'S':
                y++;
                break;
            case 'E':
                x++;
                break;
            case 'W':
                x--;
                break;
			default:
				System.out.println("Invalid command");
        }

		// Check if the move is valid
		if (map.isWall(x, y)) {
			if (user == player) {
				System.out.println("Fail");
			}
		} else {
			user.updatePosition(x, y); // Update the position
			if (user == player) {
				System.out.println("Success");
			}
		}
	}

	/**
	 * Checks the tile the user is standing on is gold or not.
	 * If it is, the user picks it up.
	 * 
	 * @param user - The player or bot
	 */
	public void pickup(Player user) {
		int[] pos = user.position();
		int x = pos[0];
		int y = pos[1];

		if (map.isGold(x, y)) {
			user.incrementGold();

			// Remove the gold from the maps
			map.removeGold(x, y);
			if (user == bot) {
				bot.node[y][x].isGold = false;
			}

			// Check if the user has picked up gold
			if (user == player) {
				System.out.println("Success. Gold owned: " + player.gold());
			}
		} else {
			if (user == player) {
				System.out.println("Fail");
			}
		}
	}

	/**
	 * Checks if the user is on an exit tile.
	 * If they are, the game ends, if they have enough gold they win.
	 * 
	 * @param user - The player or bot
	 * @return - Message to display to the user
	 */
	public String quit(Player user) {
		int[] pos = user.position();
		int x = pos[0];
		int y = pos[1];

		// Check if the user is on an exit tile
		if (map.isExit(x, y)) {
			gameRunning = false;
			// Check if the user has enough gold
			if (user.gold() >= map.goldRequired()) {
				// If the user is the player, they win
				if (user == player) {
					return "WIN";
				}
			}
			// If the user is the bot, they lose or if the player does not have enough gold
			return "LOSE";
		}

		return "Not on Exit";
	}


    /**
	 * Returns the gold required to win.
	 *
     * @return : Gold required to win.
     */
    public String hello() {
        return "Gold required to win: " + map.goldRequired();	
    }

	/**
	 * Displays the view to the user.
	 */
	public void look(Player user) {
		int[] playerPos = player.position();
		int[] botPos = bot.position();

		if (user == player) {
			// If the user is the player, print the view
			System.out.print(map.displayMap( playerPos, botPos));
		} else if (user == bot) {
			// If the user is the bot, add the view to the bot map
			bot.addToMap(map.displayMapArray(playerPos, botPos));
		}
	}


	
	/**
	 * Main method.
	 *
	 * @param args : Command line arguments.
	 */
	public static void main(String[] args) throws Exception {
		GameLogic logic = new GameLogic();
		logic.play();
    }
}