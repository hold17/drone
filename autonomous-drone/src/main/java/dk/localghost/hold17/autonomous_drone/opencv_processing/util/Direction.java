package dk.localghost.hold17.autonomous_drone.opencv_processing.util;

public enum Direction {
    LEFT, CENTER, RIGHT, UP, LEFTUP, RIGHTUP, DOWN, LEFTDOWN, RIGHTDOWN, LEFTCENTER, CENTERCENTER, RIGHTCENTER, UNKNOWN;
    public static int CAMERA_WIDTH = 1280;

    /**
     * Methods to find direction from x coordinate
     *
     * @param x the x coordinate
     * @return the direction, if any is applicable
     */
    public static Direction left(double x) {
        if (x > 0 && x <= (CAMERA_WIDTH / 5) * 2) return Direction.LEFT;
        else return null;
    }

    public static Direction center(double x) {
        if (x > (CAMERA_WIDTH / 5) * 2 && x < (CAMERA_WIDTH / 5) * 3)
            return Direction.CENTER; // center segment is 1/5 of CAMERA_WIDTH
        else return null;
    }

    public static Direction right(double x) {
        if (x >= (CAMERA_WIDTH / 5) * 3 && x < CAMERA_WIDTH) return Direction.RIGHT;
        else return null;
    }

    /**
     * Method that finds the direction along the X-axis.
     *
     * @param x the x coordinate of the center of a circle
     * @return the direction to the circle center, if any is found
     */

    public static Direction findXDirection(double x) {
        if (Direction.left(x) != null) return Direction.LEFT;
        else if (Direction.center(x) != null) return Direction.CENTER;
        else if (Direction.right(x) != null) return Direction.RIGHT;
        else {
            System.err.println("Unknown direction");
            return Direction.UNKNOWN;
        }
    }


    /**
     * Method that finds the direction
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the direction if it can be determined.
     */
    public static Direction exactDirection(double x, double y){
        if(Direction.centerUp(x,y) != null) return Direction.UP;
        else if(Direction.centerCenter(x,y) != null) return Direction.CENTER;
        else if(Direction.centerDown(x,y) != null) return Direction.DOWN;
        else if(Direction.leftUp(x,y) != null) return Direction.LEFTUP;
        else if(Direction.leftCenter(x,y) !=null) return Direction.LEFT;
        else if(Direction.leftDown(x,y) != null) return Direction.LEFTDOWN;
        else if(Direction.rightUp(x,y) != null) return Direction.RIGHTUP;
        else if(Direction.rightCenter(x,y) != null) return Direction.RIGHT;
        else if(Direction.rightDown(x,y) != null) return Direction.RIGHTDOWN;

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
    public static Direction leftCenter(double x, double y) {
        if (x > 0 && x < 512 && y < 400 && y > 320) return Direction.LEFTCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction leftDown(double x, double y) {
        if (x > 0 && x < 512 && y < 320) return Direction.LEFTDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction leftUp(double x, double y) {
        if (x > 0 && x < 512 && y > 400) return Direction.LEFTDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction centerCenter(double x, double y) {
        if (x > 512 && x < 768 && y < 400 && y > 320) return Direction.CENTERCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction centerDown(double x, double y) {
        if (x > 512 && x < 768 && y < 320) return Direction.DOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction centerUp(double x, double y) {
        if (x > 512 && x < 768 && y > 400) return Direction.UP; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction rightDown(double x, double y) {
        if (x > 768 && x < 1280 && y < 320) return Direction.RIGHTDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction rightCenter(double x, double y) {
        if (x > 768 && x < 1280 && y < 400 && y > 320)
            return Direction.RIGHTCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction rightUp(double x, double y) {
        if (x > 768 && x < 1280 && y > 400)
            return Direction.RIGHTUP; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
}
