public class Player {
    protected int[] position = {0, 0};
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
