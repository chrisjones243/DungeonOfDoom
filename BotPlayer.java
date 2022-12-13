import java.util.ArrayList;
import java.util.HashMap;

public class BotPlayer extends Player {
    
    private char direction = 'E';
    // private int moved = 3;
    // private boolean look;
    private int stepsUntilLook = 0;

    private int[] expectedPos = position();
    private boolean startSearch = false;

    private int[] searchObjectPosition = {-1, -1};
    private char objectToSearch = '*';
    private boolean changedSearchObject = false;
    private HashMap<Character, Integer> precedence = new HashMap<Character, Integer>();


    Node[][] node;
    Node startNode, endNode, currentNode;
    ArrayList<Node> openList = new ArrayList<Node>();
    ArrayList<Node> visitedList = new ArrayList<Node>();

    private int searchIteration = 0;


    boolean endReached = false;


    public BotPlayer(Map map) {
        this.map = map;
        node = new Node[map.mapHeight()][map.mapWidth()];
        setupNodes();

        precedence.put('P', 1);
        precedence.put('G', 2);
        precedence.put('E', 3);
        precedence.put('?', 4);
        precedence.put('*', 5);
    }

    public void updatePosition(int[] pos) {
        position = pos;
        expectedPos = pos;
    }


    /**
     * This method is called by the game engine.
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
     * @param direction The direction the player is moving in.
     */
    private String nextMove() {
        int[] pos = position(); 
        int x = pos[0];
        int y = pos[1];

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
            objectToSearch = '*';
            stepsUntilLook = 5;
            System.out.println("BOT LOOK");
            return "LOOK";
        }

        stepsUntilLook--;
        expectedPos = pos;
        System.out.println("Next move: " + direction);
        return "MOVE " + direction;
    }

    /**
     * This function is used to find the next move the bot should make.
     * It is called by the depthFirstSearch function, when a dead end is reached.
     * 
     */
    // private char findNextMove() {
    //     int[] pos = position();
    //     int x = pos[0];
    //     int y = pos[1];

    //     System.out.println("Find next move");

    //     if (node[y + 1][x].isWall && node[y - 1][x].isWall && node[y][x - 1].isWall) {
    //         return 'E';
    //     }else if (node[y][x + 1].isWall && node[y - 1][x].isWall && node[y][x - 1].isWall) {
    //         return 'S';
    //     } else if (node[y][x + 1].isWall && node[y + 1][x].isWall && node[y][x - 1].isWall) {
    //         return 'N';
    //     } else if (node[y][x + 1].isWall && node[y + 1][x].isWall && node[y - 1][x].isWall) {
    //         return 'W';
    //     } else if (node[y + 1][x].isWall && node[y - 1][x].isWall) {
    //         return 'E';
    //     } else if (node[y][x + 1].isWall && node[y - 1][x].isWall) {
    //         return 'W';
    //     } else if (node[y][x + 1].isWall && node[y + 1][x].isWall) {
    //         return 'N';
    //     } else if (node[y + 1][x].isWall && node[y][x - 1].isWall) {
    //         return 'E';
    //     } else if (node[y - 1][x].isWall && node[y][x - 1].isWall) {
    //         return 'S';
    //     } else if (node[y][x - 1].isWall && node[y + 1][x].isWall) {
    //         return 'N';
    //     } else if (node[y][x + 1].isWall) {
    //         return 'S';
    //     } else if (node[y + 1][x].isWall) {
    //         return 'E';
    //     } else if (node[y - 1][x].isWall) {
    //         return 'W';
    //     } else if (node[y][x - 1].isWall) {
    //         return 'N';
    //     }

    //     return 'N';
    // }

    private void startSearch(int x, int y) {
        int[] pos = position();
        setStartNode(pos[0], pos[1]);
        setEndNode(x, y);

        setCostOnNodes(); // Set the cost of each node.
    }

    private void search() {
        while (endReached == false) {
            int x = currentNode.getX();
            int y = currentNode.getY();

            currentNode.setVisited();
            visitedList.add(currentNode);
            openList.remove(currentNode);

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

            for (int i = 0; i < openList.size(); i++) {
                if (openList.get(i).fCost < lowestCost) {
                    lowestCost = openList.get(i).fCost;
                    nodeIndex = i;
                } else if (openList.get(i).fCost == lowestCost) {
                    if (openList.get(i).gCost < openList.get(nodeIndex).gCost) {
                        nodeIndex = i;
                    }
                }
            }

            currentNode = openList.get(nodeIndex);

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

    private void trackThePath() {
        Node current = endNode;

        while (current != startNode) {
            current = current.parent;

            if (current != startNode) {
                current.setPath();
            }
        }
    }

    private void resetSearch() {
        searchIteration = 0;
        endReached = false;
        for (int i = 0; i < visitedList.size(); i++) {
            visitedList.get(i).isVisited = false;
            visitedList.get(i).isPath = false;
            visitedList.get(i).isOpen = false;
            visitedList.get(i).parent = null;
        }
        for (int i = 0; i < openList.size(); i++) {
            openList.get(i).isVisited = false;
            openList.get(i).isPath = false;
            openList.get(i).isOpen = false;
            openList.get(i).parent = null;
        }
        visitedList.clear();
        openList.clear();
    }

    private void openNode(Node node) {
        if (node.isOpen == false && node.isVisited == false && node.isWall == false) {
            node.setOpen();
            node.parent = currentNode;
            openList.add(node);
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
    
    private void searchForObjects() {
        System.out.println("Searching for objects");
        for (int i = 0; i < map.mapHeight(); i++) {
            for (int j = 0; j < map.mapWidth(); j++) {
                node[i][j].isPlayer = false;
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

    private boolean presedent(char object) {
        if (precedence.get(object) < precedence.get(objectToSearch)) {
            objectToSearch = object;
            changedSearchObject = true;
            return true;
        }
        return false;
    }


}