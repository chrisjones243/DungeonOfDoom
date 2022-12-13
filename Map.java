import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Reads and contains in memory the map of the game.
 *
 */
public class Map {

	private char[][] map;
	private String mapName;
	private int goldRequired;
	
	/**
	 * Constructor that accepts a map to read in from.
	 *
	 * @param filename - The filename of the map file.
	 * @throws IOException
	 */
	public Map(String fileName) throws IOException {
		readMap(fileName);
	}

	public String displayFullMap(Player player, Player bot) { // delete this
		String mapString = "";
		int x = player.position()[0];
		int y = player.position()[1];
		int xBot = bot.position()[0];
		int yBot = bot.position()[1];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				if (j == x && i == y) {
					mapString += "P";
				} else if (j == xBot && i == yBot) {
					mapString += "B";
				} else {
					mapString += map[i][j];
				}
				mapString += " ";
			}
			mapString += "\n";
		}
		return mapString;
	}

	/**
     * Reads the map from file.
     *
     * @param filename - Name of the map's file.
	 * @throws IOException
     */
    public void readMap(String fileName) throws IOException {
		String line;
		try {
			// Read the map file
			BufferedReader br =  new BufferedReader(new FileReader(fileName));

			ArrayList<char[]> mapList = new ArrayList<char[]>(); // List of char arrays

			mapName = br.readLine().substring(0, 5);
			// This is the number of gold required to win the game.
			goldRequired = Integer.parseInt(br.readLine().substring("win ".length(), 5));
			
			// Read the map into a list of char arrays
			while ((line = br.readLine()) != null) {
				mapList.add(line.toCharArray());
			}
			// Convert the list of char arrays into a 2D array
			map = new char[mapList.size()][];
			map = mapList.toArray(map);

			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Error reading map file.");
			System.exit(0);
		}
	}

	/**
	 * Displays the 5x5 map around the player.
	 * Used only for the human player.
	 *
	 * @param playerPos - The x, y coordinates of the player.
	 * @param botPos - The x, y coordinates of the bot.
	 * @return A string of the view.
	 */
	public String displayMap(int[] playerPos, int[] botPos) {
		String mapString = "";
		int x = playerPos[0];
		int y = playerPos[1];

		// Display the map around the player
		for (int i = y - 2; i < y + 3; i++ ) {
			for (int j = x - 2; j < x + 3; j++ ) {
				if (i < 0 || j < 0 || j > map[0].length - 1 || i > map.length - 1) {
					// If the x, y is out of bounds, display a wall
					mapString += "#";
				} else if (x == j && y == i) {
					// If the x, y is the player's position, display a P
					mapString += 'P';
				} else if (botPos[0] == j && botPos[1] == i) {
					// If the x, y is the bot's position, display a B
					mapString += 'B';
				} else {
					mapString += map[i][j];
				}
				mapString += " "; // Add a space between each character for readability
			}
			mapString += "\n"; // Add a new line after each row
		}
		return mapString;
	}

	/**
	 * Returns a 2D array of the map around the player.
	 * Used only for Bot.
	 *
	 * @param playerPos - The x, y coordinates of the player.
	 * @param botPos - The x, y coordinates of the bot.
	 * @return A 2D array of the view.
	 */
	public char[][] displayMapArray(int[] playerPos, int[] botPos) {
		char[][] mapArray = new char[5][5];
		int x = botPos[0];
		int y = botPos[1];

		for (int i = y - 2; i < y + 3; i++ ) {
			for (int j = x - 2; j < x + 3; j++ ) {
				if (i < 0 || j < 0 || j > map[0].length - 1 || i > map.length - 1) {
					mapArray[i - y + 2][j - x + 2] = '#';
				} else if (playerPos[0] == j && playerPos[1] == i) {
					mapArray[i - y + 2][j - x + 2] = 'P';
				} else {
					mapArray[i - y + 2][j - x + 2] = map[i][j];
				}
			}
		}
		return mapArray;
	}

	// Getters
	
	public String mapName() {
		return mapName;
	}

	public int goldRequired() {
		return goldRequired;
	}

	public boolean isWall(int x, int y) {
		return map[y][x] == '#';
	}

	public boolean isGold(int x, int y) {
		return map[y][x] == 'G';
	}

	public boolean isExit(int x, int y) {
		return map[y][x] == 'E';
	}

	public void removeGold(int x, int y) {
		map[y][x] = '.';
	}

	public int mapWidth() {
		return map[0].length;
	}

	public int mapHeight() {
		return map.length;
	}

}
