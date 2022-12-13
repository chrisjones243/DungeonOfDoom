/**
 * Contains the main logic part of the game, as it processes.
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.io.IOException;


public class GameLogic {
	
	/* Reference to the map being used */
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

	private int[] initalPosition () {

		int x = (int) (Math.random() * map.mapWidth());
		int y = (int) (Math.random() * map.mapHeight());

		if (map.isWall(x, y) || (player.position()[0] == x && player.position()[1] == y)) {
			return initalPosition();
		}

		return new int[] {x, y};
	}

	/**
	 * Prints the banner
	 */
	public void banner() throws IOException {
		String bannerFile = "banner/banner.txt";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(bannerFile)));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Error reading banner file");
		}
	}

	/**
	 * Prints the map
	 */
	public void chooseMap() throws IOException {
		String mapName = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Please enter the name of the map you wish to play on: ");
		try {
			mapName = br.readLine();
		} catch (IOException e){
			System.out.print("something went wrong on input.");
		}

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
			default:
				map = new Map();
				return;
		}
		map = new Map("maps/" + mapName);
	}

	public void play() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(gameRunning) {
			String command = "";

		try {
				command = reader.readLine();
		} catch (IOException e) {
            // System.in has been closed
			e.printStackTrace();
            System.out.println(e);
        }

		processInput(command, player);
		processInput(bot.makeMove(), bot);

		if (Arrays.equals(player.position(), bot.position())) {
			System.out.println("You have been caught by the bot!");
			System.out.println("LOSE");
			gameRunning = false;
		}

		System.out.println(map.displayFullMap(player, bot));
		}
	}


	public void processInput(String command, Player user) {
		command = command.toUpperCase();
		String[] input = command.split(" ");
		
		switch (input[0]) {
			case "MOVE":
				if (input.length < 2) {
					System.out.println("Invalid command");
				} else {
					move(input[1].charAt(0), user);
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
		if (map.isWall(x, y)) {
			if (user == player) {
				System.out.println("Fail");
			}
			if (user == bot) {
				bot.node[y][x].isWall = true;
			}
		} else {
			user.updatePosition(x, y);
			if (user == player) {
				System.out.println("Success");
			}
			if (user == bot) {
				bot.node[y][x].isWall = false;
			}
		}
	}

	
	public void pickup(Player user) {
		int[] pos = user.position();
		int x = pos[0];
		int y = pos[1];

		if (map.isGold(x, y)) {
			user.incrementGold();
			map.removeGold(x, y);
			if (user == player) {
				System.out.println("Success. Gold owned: " + player.gold());
			}
			if (user == bot) {
				bot.node[y][x].isGold = false;
			}
		} else {
			if (user == player) {
				System.out.println("Fail");
			}
		}
	}

	public String quit(Player user) {
		int[] pos = user.position();
		int x = pos[0];
		int y = pos[1];
		if (map.isExit(x, y)) {
			gameRunning = false;
			if (user.gold() >= map.goldRequired()) {
				if (user == player) {
					return "WIN";
				}
			return "LOSE";
			}
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
	 * Returns the 5x5 map.
	 *
	 * @return : The map.
	 */
	public void look(Player user) {
		int[] playerPos = player.position();
		int[] botPos = bot.position();

		if (user == player) {
			System.out.print(map.displayMap( playerPos, botPos));
		} else if (user == bot) {
			bot.addToMap(map.displayMapArray(playerPos, botPos));
		}
	}

	// /**
	//  * Returns the full map.
	//  * 
	//  * @return : The map.
	//  */
	// public String lookfull() {
	// 	return map.displayFullMap();
	// }


	
	
	public static void main(String[] args) throws Exception {
		GameLogic logic = new GameLogic();
		logic.play();
    }
}