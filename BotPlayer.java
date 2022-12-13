import java.util.ArrayList;
import java.util.HashMap;

public class BotPlayer extends Player {
    
    private char direction = 'E';

    private int stepsUntilLook = 0;

    private int[] expectedPos = position();

    private boolean startSearch = false;
    private int[] searchObjectPosition = {-1, -1};
    private char objectToSearch = '*';
    private boolean changedSearchObject = false;
    private HashMap<Character, Integer> objectPrecedence = new HashMap<Character, Integer>();


    Node[][] node;
    Node startNode, endNode, currentNode;
    ArrayList<Node> openObjects = new ArrayList<Node>();
    ArrayList<Node> visitedObjects = new ArrayList<Node>();

    private int searchIteration = 0;


    boolean endReached = false;


    public BotPlayer(Map map) {
        this.map = map;
        node = new Node[map.mapHeight()][map.mapWidth()];
        setupNodes();

        objectPrecedence.put('P', 1);
        objectPrecedence.put('G', 2);
        objectPrecedence.put('E', 3);
        objectPrecedence.put('?', 4);
        objectPrecedence.put('*', 5);
    }

    public void updatePosition(int[] pos) {
        position = pos;
        expectedPos = pos;
    }


    /**
     * This method is called by the game engine.
     * 
     * @return The next move of the bot.
     */
    public String makeMove() {
        int pos[] = position();
        int x = pos[0];
        int y = pos[1];
        node[y][x].isPath = false;
        searchForObjects();
        System.out.println("Search object: " + objectToSearch);
        if (node[y][x] == endNode || node[y][x + 1] == endNode || node[y][x - 1] == endNode || node[y + 1][x] == endNode || node[y - 1][x] == endNode) {
            objectToSearch = '*';
        }
        if (node[y][x].isPlayer) {
            node[y][x].isPlayer = false;
            System.out.println("BOT SHOOT");
            return "LOOK";
        } else if (stepsUntilLook <= 0) {

            System.out.println("BOT LOOK");
            if (objectToSearch == 'P') {
                stepsUntilLook = 100;
            } else {
                stepsUntilLook = 5;
            }

            return "LOOK";
        } else if (node[y][x].isGold) {

            objectToSearch = '*';
            System.out.println("BOT PICKUP");
            return "PICKUP";
        } else if (node[y][x].isExit && goldCount >= map.goldRequired()) {
            System.out.println("BOT QUIT");

            return "QUIT";
        }

        if (changedSearchObject) {
            System.out.println("Changed search object to: " + objectToSearch);
            System.out.println("Before search");
            displayNodeMap(); // delete
            startSearch = true;
            int searchX = searchObjectPosition[0];
            int searchY = searchObjectPosition[1];
            startSearch(searchX, searchY);
            changedSearchObject = false;
        }
        if (startSearch) {
            resetSearch();
            System.out.println("Start search");
            startSearch = false;
            search();
            
            System.out.println("After search");
            displayNodeMap(); // delete
        }
        return nextMove();
    }

    private void setupNodes() {
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                node[i][j] = new Node(j, i);
            }
        }
    }

    /**
     * This method is called by the makeMove function.
     * and is used to find the next move the bot should make.
     * 
     * @return The next move the bot should make.
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
    private void startSearch(int x, int y) {
        int[] pos = position();
        setStartNode(pos[0], pos[1]);
        setEndNode(x, y);

        setCostOnNodes(); // Set the cost of each node.
    }

    // * The search will start from the current position of the player.
    //  * 
    //  * The search will be done using the A* algorithm.
    //  * 
    //  * The search will be done using the following rules:
    //  * 1. The search will stop when the object is found.
    //  * 2. The search will stop when the search has reached the end of the map.
    //  * 3. The search will stop when the search has reached the maximum number of steps.
    //  * 
    //  * The search will be done using the following heuristics:
    //  * 1. The search will use the Euclidean distance.

    private void search() {
        while (endReached == false) {
            int x = currentNode.getX();
            int y = currentNode.getY();

            currentNode.setVisited();
            visitedObjects.add(currentNode);
            openObjects.remove(currentNode);

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
            int nodeIndex = 0;
            int lowestCost = 9999;

            for (int i = 0; i < openObjects.size(); i++) {
                if (openObjects.get(i).fCost < lowestCost) {
                    lowestCost = openObjects.get(i).fCost;
                    nodeIndex = i;
                } else if (openObjects.get(i).fCost == lowestCost) {
                    if (openObjects.get(i).gCost < openObjects.get(nodeIndex).gCost) {
                        nodeIndex = i;
                    }
                }
            }

            currentNode = openObjects.get(nodeIndex);

            if (currentNode == endNode) {
                endReached = true;
                trackThePath();
                System.out.println("End reached!");
            } else if (searchIteration >= 300) {
                endReached = true;
                endNode.setUnreachable(); // Set the end node to unreachable.
                objectToSearch = '*';
                searchForObjects();
                System.out.println("Search iteration limit reached!");
            } else {
                searchIteration++;
                search();
            }
        }
    }

    /**
     * This method is used to find the shortest path from the start node to the end node.
     * 
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
     * So that a new search can be done again.
     * 
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
     * @param node
     */
    private void openNode(Node node) {
        // If the node is not visited, not open and not a wall.
        if (node.isOpen == false && node.isVisited == false && node.isWall == false) {
            node.setOpen();
            node.parent = currentNode;
            openObjects.add(node);
        }
    }


    private void setStartNode(int x, int y) {
        node[y][x].setStart();
        startNode = node[y][x];
        currentNode = startNode;
    }

    private void setEndNode(int x, int y) {
        node[y][x].setEnd();
        endNode = node[y][x];
    }

    private void setCostOnNodes() {
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                getCost(node[i][j]);
            }
        }
    }

    private void getCost(Node node) {
        int weight = 0;
        if (!(node.isUnknown) && endNode.isUnknown) {
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

    public void look(char[][] view) {
        int[] pos = position();
        int x = pos[0];
        int y = pos[1];
        int viewX = 0;
        int viewY = 0;

        for (int i = y - 2; i < y + 3; i++ ) {
            viewX = 0;
			for (int j = x - 2; j < x + 3; j++ ) {
				if ((i >= 0 && j >= 0 && i < (map.mapHeight()) && j < (map.mapWidth()))) {
                    if (view[viewY][viewX] == '#') {
                        node[i][j].setWall();
                    } else if (view[viewY][viewX] == '.') {
                        node[i][j].setEmpty();
                    } else if (view[viewY][viewX] == 'E') {
                        node[i][j].setExit();
                        // if (goldCount >= map.goldRequired()) {
                        //     startSearch = true;
                        //     startSearch(j, i);
                        // }
                    } else if (view[viewY][viewX] == 'G') {
                        node[i][j].setGold();
                        // startSearch = true;
                        // startSearch(j, i);
                    } else if (view[viewY][viewX] == 'P') {
                        node[i][j].setPlayer();
                        // startSearch(j, i);
                        // startSearch = true;
                    }
                }
                viewX++;
			}
            viewY++;
		}
        displayNodeMap();
    }

    private void displayNodeMap() {
        for (int i = 0; i < node.length; i++) {
            for (int j = 0; j < node[i].length; j++) {
                if (node[i][j].isPath) {
                    System.out.print("~");
                } else if (node[i][j].isWall) {
                    System.out.print("#");
                } else if (node[i][j].isGold) {
                    System.out.print("G");
                } else if (node[i][j].isExit) {
                    System.out.print("E");
                } else if (node[i][j].isPlayer) {
                    System.out.print("P");
                } else if (node[i][j].isUnknown) {
                    System.out.print("?");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * This method is used to search for objects on the map.
     * It will search for the object with the highest priority.
     * 
     * The priority is as follows:
     * 1. Gold
     * 2. Exit
     * 3. Player
     * 4. Unknown
     * 
     * 
     */
    private void searchForObjects() {
        System.out.println("Searching for objects");
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                if (node[i][j].isUnreachable) {
                    continue;
                }
                if (node[i][j] == endNode && node[i][j].isWall) {
                    objectToSearch = '*';
                }
                if (!(node[i][j].isUnreachable)) {
                    if (node[i][j].isGold) {
                        System.out.println("Gold found");
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
    }

    /**
     * Returns true if the object has higher precedence than the current object to search for
     * @param object
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