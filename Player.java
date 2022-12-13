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

    /**
     * Constructor for the Player class.
     * 
     * @param map - The map that the player is on.
     */
    public Player(Map map) {
        this.map = map;
    }

    // Setters

    /**
     * Updates the position of the player.
     * 
     * @param x - The new x position of the player.
     * @param y - The new y position of the player.
     */
    public void updatePosition(int x, int y) {
        position[0] = x;
        position[1] = y;
    }

    /**
     * Updates the position of the player.
     * 
     * @param pos - The new position of the player.
     */
    public void updatePosition(int[] pos) {
        position = pos;
    }

    /**
     * Increments the gold count by 1.
     */
    public void incrementGold() {
        goldCount++;
    }

    // Getters

    public int[] position() {
        return position;
    }

    public int gold() {
        return goldCount;
    }
}
