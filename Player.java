public class Player {
    private int[] position = {7,3};
    protected int goldCount = 0;

    protected Map map;


    public Player() {

    }

    public int[] position() {
        return position;
    }

    public void updatePosition(int x, int y) {
        position[0] = x;
        position[1] = y;
    }

    public void incrementGold() {
        goldCount++;
    }

    public int gold() {
        return goldCount;
    }
}
