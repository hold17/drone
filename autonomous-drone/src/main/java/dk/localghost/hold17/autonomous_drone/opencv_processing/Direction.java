package dk.localghost.hold17.autonomous_drone.opencv_processing;

public enum Direction {
    LEFT, RIGHT, CENTER,UP, DOWNCENTER, UPCENTER, UPLEFT, UPRIGHT, DOWN, DOWNLEFT,DOWNRIGHT, UNKNOWN;

    /**
     * Method that finds the direction
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the direction if it can be determined.
     */
    public static Direction exactDirection(double x, double y){
        if(Direction.upCenter(x,y) != null) return Direction.UPCENTER;
        else if(Direction.center(x,y) != null) return Direction.CENTER;
        else if(Direction.downCenter(x,y) != null) return Direction.DOWNCENTER;
        else if(Direction.upLeft(x,y) != null) return Direction.UPLEFT;
        else if(Direction.left(x,y) !=null) return Direction.LEFT;
        else if(Direction.downLeft(x,y) != null) return Direction.DOWNLEFT;
        else if(Direction.upRight(x,y) != null) return Direction.UPRIGHT;
        else if(Direction.right(x,y) != null) return Direction.RIGHT;
        else if(Direction.downRight(x,y) != null) return Direction.DOWNRIGHT;

        else {
            return Direction.UNKNOWN;
        }
    }

    /**
     * Methods to find direction from coordinates
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the direction, if any is applicable
     */
    public static Direction left(double x, double y) {
        if (x > 0 && x < 512 && y < 400 && y > 320) return Direction.LEFT; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction downLeft(double x, double y) {
        if (x > 0 && x < 512 && y < 320) return Direction.DOWNLEFT; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction upLeft(double x, double y) {
        if (x > 0 && x < 512 && y > 400) return Direction.DOWNLEFT; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction center(double x, double y) {
        if (x > 512 && x < 768 && y < 400 && y > 320) return Direction.CENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction downCenter(double x, double y) {
        if (x > 512 && x < 768 && y < 320) return Direction.DOWNCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction upCenter(double x, double y) {
        if (x > 512 && x < 768 && y > 400) return Direction.UPCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction downRight(double x, double y) {
        if (x > 768 && x < 1280 && y < 320) return Direction.DOWNRIGHT; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction right(double x, double y) {
        if (x > 768 && x < 1280 && y < 400 && y > 320)
            return Direction.RIGHT; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction upRight(double x, double y) {
        if (x > 768 && x < 1280 && y > 400)
            return Direction.UPRIGHT; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
}
