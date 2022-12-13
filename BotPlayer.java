import java.util.ArrayList;
import java.util.HashMap;

public class BotPlayer extends Player {
    
    private char direction = 'E'; // The direction the bot is facing.

    private int stepsUntilLook = 0; // The number of steps until the bot "LOOK"s again.

    private int[] expectedPos = position(); // The position the bot expects itself to be in.

    private int[] searchObjectPosition = {-1, -1}; // The position of the object the bot is searching for.
    private char objectToSearch = '*';
    private boolean changedSearchObject = false; // Used to check if the bot has changed the object it is searching for.
    private HashMap<Character, Integer> objectPrecedence = new HashMap<Character, Integer>();

    // Variables used for the A* search.
    Node[][] node;
    private Node startNode, endNode, currentNode;
    private ArrayList<Node> openObjects = new ArrayList<Node>();
    private ArrayList<Node> visitedObjects = new ArrayList<Node>();
    private int searchIteration = 0; // Used to check if the search has reached its maximum iterations.
    private boolean endReached = false;

    /**
     * Constructor for the bot player.
     * 
     * @param map The map the bot is playing on, used to get the size of the map.
     */
    public BotPlayer(Map map) {
        super(map);
        node = new Node[map.mapHeight()][map.mapWidth()]; // Create a 2D array of nodes.
        setupNodes();

        // Set the precedence of the objects.
        objectPrecedence.put('P', 1);
        objectPrecedence.put('G', 2);
        objectPrecedence.put('E', 3);
        objectPrecedence.put('?', 4);
        objectPrecedence.put('*', 5); // Default value, used when resetting the search.
    }

    /**
     * This method is used to append the nodes to the 2D array.
     */
    private void setupNodes() {
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                node[i][j] = new Node(j, i);
            }
        }
    }

    /**
     * This method is used to update the position of the bot.
     * 
     * @param pos The position the bot is in.
     */
    public void updatePosition(int[] pos) {
        position = pos;
        expectedPos = pos;
    }


    /**
     * This method is called by the game engine.
     * 
     * @return String The move the bot wants to make.
     */
    public String makeMove() {
        int pos[] = position();
        int x = pos[0];
        int y = pos[1];

        // remove the path from the current position, so the bot doesn't go back to the same position.
        node[y][x].isPath = false;

        searchForObjects(); // Search for objects within the map.

        if ( node[y][x] == endNode || node[y][x + 1] == endNode || node[y][x - 1] == endNode || node[y + 1][x] == endNode || node[y - 1][x] == endNode ) {
                // If the bot is on or next to the end node, reset the search.
                objectToSearch = '*';
        }

        if (node[y][x].isPlayer) {
            // If the bot is on a player, remove the player from the map, 
            // and LOOK again, since the player might have moved.
            node[y][x].isPlayer = false;

            return "LOOK";
        } else if (stepsUntilLook <= 0) {
            // If the bot has reached the number of steps until it should LOOK again.
            // and reset the steps until it should LOOK again.
            if (objectToSearch == 'P') {
                stepsUntilLook = 100;
            } else {
                stepsUntilLook = 5;
            }
            return "LOOK";
        } else if (node[y][x].isGold) {
            // If the bot is on gold, pick it up, and reset the search.
            objectToSearch = '*';

            return "PICKUP";
        } else if (node[y][x].isExit && goldCount >= map.goldRequired()) {
            // If the bot is on the exit, and has enough gold, exit the map.
            return "QUIT";
        }

        if (changedSearchObject) {
            // If the bot has changed the object it is searching for, start the search again.
            changedSearchObject = false;
            setupSearch();
            resetSearch();
            search();
        }

        return nextMove();
    }

    /**
     * This method is  used to find the next move the bot should make.
     * 
     * @return String containing the next move the bot should make.
     */
    private String nextMove() {
        int[] pos = position(); 
        int x = pos[0];
        int y = pos[1];

        // If the node's around the bot is a path, gold or exit (if enough gold), move to it.
        if (node[y][x + 1].isPath || node[y][x + 1].isGold || (node[y][x + 1].isExit && goldCount == map.goldRequired()) || node[y][x + 1].isPlayer) {
            x++;
            direction = 'E';
        } else if (node[y][x - 1].isPath || node[y][x - 1].isGold || (node[y][x - 1].isExit && goldCount == map.goldRequired()) || node[y][x - 1].isPlayer) {
            x--;
            direction = 'W';
        } else if (node[y + 1][x].isPath || node[y + 1][x].isGold || (node[y + 1][x].isExit && goldCount == map.goldRequired()) || node[y + 1][x].isPlayer) {
            y++;
            direction = 'S';
        } else if (node[y - 1][x].isPath || node[y - 1][x].isGold || (node[y - 1][x].isExit && goldCount == map.goldRequired()) || node[y - 1][x].isPlayer) {
            y--;
            direction = 'N';
        }

        if (node[y][x].isWall || expectedPos != pos) {
            // If the bot is on a wall or it's not where it should be, look and reset search object.
            objectToSearch = '*';
            stepsUntilLook = 5;

            return "LOOK";
        }

        stepsUntilLook--;
        expectedPos = pos;

        return "MOVE " + direction;
    }

    /**
     * This method is used to setup the search.
     * 
     * @param x The x coordinate of the node.
     * @param y The y coordinate of the node.
     */
    private void setupSearch() {
        int[] pos = position();
        int x = searchObjectPosition[0];
        int y = searchObjectPosition[1];
        setStartNode(pos[0], pos[1]);
        setEndNode(x, y);

        setCostOnNodes(); // Set the cost of each node.
    }

    /**
     * This method is an implementation of the A* search algorithm.
     * 
     * The search will start from the current position of the player.
     * 
     * The search will be done using the following rules:
     * 1. The search will stop when the object is found.
     * 2. The search will stop when the search has reached the end of the map.
     * 3. The search will stop when the search has reached the maximum number of steps.
     * 
     * The search will use the Manhattan distance as a heuristic.
     * 
     * Followed https://youtu.be/2JNEme00ZFA as a guide.
     */
    private void search() {
        while (endReached == false) {
            int x = currentNode.getX();
            int y = currentNode.getY();

            currentNode.setVisited();
            visitedObjects.add(currentNode);
            openObjects.remove(currentNode);

            // Check the nodes around the current node, 
            // and set them as open if they are within the map bounds.
            if (x - 1 >= 0) {
                openNode(node[y][x - 1]);
            }
            if (x + 1 < map.mapWidth()) {
                openNode(node[y][x + 1]);
            }
            if (y - 1 >= 0) {
                openNode(node[y - 1][x]);
            }
            if (y + 1 < map.mapHeight()) {
                openNode(node[y + 1][x]);
            }

            // Find the node with the lowest cost.
            int lowestNodeIndex = 0;
            int lowestCost = 9999;

            // Loop through all the open nodes.
            // And if the node has a lower cost than the current lowest cost,
            // set the node as the node with the lowest cost.
            for (int i = 0; i < openObjects.size(); i++) {
                if (openObjects.get(i).fCost < lowestCost) {
                    lowestCost = openObjects.get(i).fCost; 
                    lowestNodeIndex = i;
                } else if (openObjects.get(i).fCost == lowestCost) { // If the costs are equal, use the gCost.
                    if (openObjects.get(i).gCost < openObjects.get(lowestNodeIndex).gCost) {
                        lowestNodeIndex = i;
                    }
                }
            }

            currentNode = openObjects.get(lowestNodeIndex);

            if (currentNode == endNode) {
                // If the current node is the end node, the search is done.
                endReached = true;
                trackThePath();

            } else if (searchIteration >= 300) {
                // If the search has reached the maximum number of steps, the search is done.
                endReached = true;
                endNode.setUnreachable();
                objectToSearch = '*';
                searchForObjects();

            } else {
                // If the search is not done, continue the search.
                searchIteration++;
                search();
            }
        }
    }

    /**
     * This method is used to find the shortest path from the start node to the end node.
     */
    private void trackThePath() {
        Node current = endNode;

        while (current != startNode) {
            current = current.parent;

            if (current != startNode) {
                current.setPath();
            }
        }
    }

    /**
     * This method is used to reset the visited and open nodes.
     * So that a new search can be done again, without the old search interfering.
     */
    private void resetSearch() {
        searchIteration = 0;
        endReached = false;
        for (int i = 0; i < visitedObjects.size(); i++) {
            visitedObjects.get(i).isVisited = false;
            visitedObjects.get(i).isPath = false;
            visitedObjects.get(i).isOpen = false;
            visitedObjects.get(i).parent = null;
        }
        for (int i = 0; i < openObjects.size(); i++) {
            openObjects.get(i).isVisited = false;
            openObjects.get(i).isPath = false;
            openObjects.get(i).isOpen = false;
            openObjects.get(i).parent = null;
        }
        visitedObjects.clear();
        openObjects.clear();
    }

    /**
     * This method is used to open a node.
     * this means that the node will be added to the open list.
     *
     * @param node The node to open.
     */
    private void openNode(Node node) {
        // If the node is not visited, not open and not a wall.
        if (node.isOpen == false && node.isVisited == false && node.isWall == false) {
            node.setOpen();
            node.parent = currentNode;
            openObjects.add(node);
        }
    }

    /**
     * This method is used to set the start node.
     * 
     * @param x The x position of the start node.
     * @param y The y position of the start node.
     */
    private void setStartNode(int x, int y) {
        node[y][x].setStart();
        startNode = node[y][x];
        currentNode = startNode;
    }

    /**
     * This method is used to set the end node.
     * 
     * @param x The x position of the end node.
     * @param y The y position of the end node.
     */
    private void setEndNode(int x, int y) {
        node[y][x].setEnd();
        endNode = node[y][x];
    }

    /**
     * This method is used to set the cost of all the nodes.
     * The cost is used to find the shortest path.
     */
    private void setCostOnNodes() {
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                getCost(node[i][j]);
            }
        }
    }

    /**
     * This method is used to get the cost of a node.
     * The cost is used to find the shortest path.
     * 
     * @param node The node to get the cost of.
     */
    private void getCost(Node node) {
        int weight = 0;
        if (!(node.isUnknown) && endNode.isUnknown) {
            // If the node is not unknown and the end node is unknown, add a weight to the node.
            // This is done to make the search go to the unknown nodes first, instead of revisiting known nodes.
            weight = 1;
        }
        // Get the distance between the current node and the start node.
        int xDist = Math.abs(node.getX() - startNode.getX());
        int yDist = Math.abs(node.getY() - startNode.getY());
        node.gCost = xDist + yDist;

        // Get the distance between the current node and the end node.
        xDist = Math.abs(node.getX() - endNode.getX());
        yDist = Math.abs(node.getY() - endNode.getY());
        node.hCost = xDist + yDist;

        // Add the two distances together to get the total cost.
        node.fCost = node.gCost + node.hCost + weight;
    }

    /**
     * This method is called in the GameLogic class.
     * It is used recieve the view of the bot from the GameLogic class,
     * and add it to the map.
     * 
     * @param view
     */
    public void addToMap(char[][] view) {

        int[] pos = position();
        int x = pos[0];
        int y = pos[1];

        int viewX = 0;
        int viewY = 0;

        // The view is 5x5, so the bot is in the middle of the view.   
        // The view is added to the map, starting from the top left corner of the view.
        for (int i = y - 2; i < y + 3; i++ ) {
            viewX = 0;
			for (int j = x - 2; j < x + 3; j++ ) {
				if ((i >= 0 && j >= 0 && i < (map.mapHeight()) && j < (map.mapWidth()))) {
                    // If the bot is within the map boundaries, set the node to the correct object type.
                    if (view[viewY][viewX] == '#') {
                        node[i][j].setWall();
                    } else if (view[viewY][viewX] == '.') {
                        node[i][j].setEmpty();
                    } else if (view[viewY][viewX] == 'E') {
                        node[i][j].setExit();
                    } else if (view[viewY][viewX] == 'G') {
                        node[i][j].setGold();
                    } else if (view[viewY][viewX] == 'P') {
                        node[i][j].setPlayer();
                    }
                }
                viewX++;
			}
            viewY++;
		}
    }
    
    /**
     * This method is used to search for objects on the map.
     * It will search for the object with the highest priority.
     */
    private void searchForObjects() {

        // Iterate through the map, and set the search for the object with the highest priority.
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                if (node[i][j].isUnreachable) {
                    continue;
                }
                if (node[i][j] == endNode && node[i][j].isWall) {
                    objectToSearch = '*';
                }
                if (node[i][j].isGold) {
                    if (presedent('G')) {
                        searchObjectPosition = new int[] {j, i};
                    }
                } else if (node[i][j].isPlayer) {
                    if (presedent('P')) {
                        searchObjectPosition = new int[] {j, i};
                        stepsUntilLook = 100;
                    }
                } else if (node[i][j].isExit && goldCount >= map.goldRequired()) {
                    if (presedent('E')) {
                        searchObjectPosition = new int[] {j, i};
                    }
                } else if (node[i][j].isUnknown) {
                    if (presedent('?')) {
                        searchObjectPosition = new int[] {j, i};
                    }
                }
                
            }
        }
    }

    /**
     * Checks the precedence of the object, against the current object.
     * 
     * @param object The object to check the precedence of
     * @return boolean true if the object has higher precedence than the current object to search for
     */
    private boolean presedent(char object) {
        if (objectPrecedence.get(object) < objectPrecedence.get(objectToSearch)) {
            objectToSearch = object;
            changedSearchObject = true;
            return true;
        }
        return false;
    }

}