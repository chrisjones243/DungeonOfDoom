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

	/* Representation of the map */
	private char[][] map;
	
	/* Map name */
	private String mapName;
	
	/* Gold required for the human player to win */
	private int goldRequired;
	
	/**
	 * Default constructor, creates the default map "Very small Labyrinth of doom".
	 */
	public Map() {
		mapName = "Very small Labyrinth of Doom";
		goldRequired = 2;
		map = new char[][]{
		{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','G','.','.','.','.','.','.','.','.','.','E','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','E','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','G','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'}
		};
	}
	
	/**
	 * Constructor that accepts a map to read in from.
	 *
	 * @param : The filename of the map file.
	 */
	public Map(String fileName) throws IOException {
		readMap(fileName);
	}

	public String displayFullMap(Player player, Player bot) {
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
	 * Displays the map around the player.
	 *
	 * @param : The x coordinate of the player.
	 * @param : The y coordinate of the player.
	 * @return : The map around the player.
	 */
	public String displayMap(int x, int y, char symbol) {
		String mapString = "";

		for (int i = y - 2; i < y + 3; i++ ) {
			for (int j = x - 2; j < x + 3; j++ ) {
				if (i < 0 || j < 0 || j > map[0].length - 1 || i > map.length - 1) {
					mapString += "#";
				} else if (x == j && y == i) {
					mapString += symbol;
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
	 * Returns a 2D array of the map around the player.
	 *
	 * @param : The x coordinate of the player.
	 * @param : The y coordinate of the player.
	 * @return : The map around the player.
	 */
	public char[][] displayMapArray(int x, int y) {
		char[][] mapArray = new char[5][5];

		for (int i = y - 2; i < y + 3; i++ ) {
			for (int j = x - 2; j < x + 3; j++ ) {
				if (i < 0 || j < 0 || j > map[0].length - 1 || i > map.length - 1) {
					mapArray[i - y + 2][j - x + 2] = '#';
				} else {
					mapArray[i - y + 2][j - x + 2] = map[i][j];
				}
			}
		}
		return mapArray;
	}

	public String mapName() {
		return mapName;
	}

	public int goldRequired() {
		return goldRequired;
	}

	public char getTile(int x, int y) {
		return map[y][x];
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

    /**
     * Reads the map from file.
     *
     * @param : Name of the map's file.
     */
    public void readMap(String fileName) throws IOException {
		String line;
		BufferedReader br =  new BufferedReader(new FileReader(fileName));
		try {
			mapName = br.readLine().substring(0, 5);
			goldRequired = Integer.parseInt(br.readLine().substring("win ".length(), 5));
			ArrayList<char[]> mapList = new ArrayList<char[]>();
			// Read the map into a list of char arrays
			while ((line = br.readLine()) != null) {
				mapList.add(line.toCharArray());
			}
			// Convert the list of char arrays into a 2D array
			map = new char[mapList.size()][];
			map = mapList.toArray(map);
		
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error reading map file.");
		} finally {
			br.close();
		}
	}

}
