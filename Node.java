public class Node {
    
    Node parent;
    int x;
    int y;
    int gCost;
    int hCost;
    int fCost;
    boolean isVisited;
    boolean isStart;
    boolean isEnd;
    boolean isOpen;
    boolean isPath;

    boolean isEmpty;
    boolean isGold;
    boolean isPlayer;
    boolean isExit;
    boolean isWall;
    boolean isUnknown = true;


    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setStart() {
        isStart = true;
    }

    public void setEnd() {
        isEnd = true;
    }

    public void setOpen() {
        isOpen = true;
    }

    public void setVisited() { 
        isVisited = true;
    }

    public void setPath() {
        isPath = true;
    }

    public void setWall() {
        isWall = true;
        isUnknown = false;
    }

    public void setEmpty() {
        isEmpty = true;
        isUnknown = false;
    }

    public void setGold() {
        isGold = true;
        isUnknown = false;
    }

    public void setPlayer() {
        isPlayer = true;
        isUnknown = false;
    }

    public void setExit() {
        isExit = true;
        isUnknown = false;
    }
}
