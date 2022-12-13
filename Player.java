/**
 * Player.java
 * 
 * This class represents the player in the game. It keeps track of the player's
 * position and the amount of gold the player has collected.
 * 
 */

public class Player {
    protected int[] position = {0, 0};
    protected int goldCount = 0;

    protected Map map;


    public Player(Map map) {
        this.map = map;
    }

    public int[] position() {
        return position;
    }

    public void updatePosition(int x, int y) {
        position[0] = x;
        position[1] = y;
    }

    public void updatePosition(int[] pos) {
        position = pos;
    }

    public void incrementGold() {
        goldCount++;
    }

    public int gold() {
        return goldCount;
    }
}
