package main.java.videos;

class Move implements Comparable<Move> {
    private final boolean isACapture;
    private final int player;

    public Move(final int player) {
        this.player = player;
        this.isACapture = false;
    }

    @Override
    public int compareTo(final Move o) {
        return 0;
    }

    public int getPlayer() {
        return player;
    }

    public boolean isACapture() {
        return isACapture;
    }
}
