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
        if (x > 0 && x <= (CAMERA_WIDTH / 5) * 2) return LEFT;
        else return UNKNOWN;
    }

    public static Direction center(double x) {
        if (x > (CAMERA_WIDTH / 5) * 2 && x < (CAMERA_WIDTH / 5) * 3)
            return CENTER; // center segment is 1/5 of CAMERA_WIDTH
        else return UNKNOWN;
    }

    public static Direction right(double x) {
        if (x >= (CAMERA_WIDTH / 5) * 3 && x < CAMERA_WIDTH) return RIGHT;
        else return UNKNOWN;
    }

    /**
     * Method that finds the direction along the X-axis.
     *
     * @param x the x coordinate of the center of a circle
     * @return the direction to the circle center, if any is found
     */

    public static Direction findXDirection(double x) {
        if (left(x) != null) return LEFT;
        else if (center(x) != null) return CENTER;
        else if (right(x) != null) return RIGHT;
        else {
            System.err.println("Unknown direction");
            return UNKNOWN;
        }
    }


    /**
     * Method that finds the direction
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the direction if it can be determined.
     */
    public static Direction exactDirection(double x, double y){
        if(centerUp(x,y) != null) return UP;
        else if(centerCenter(x,y) != null) return CENTER;
        else if(centerDown(x,y) != null) return DOWN;
        else if(leftUp(x,y) != null) return LEFTUP;
        else if(leftCenter(x,y) !=null) return LEFT;
        else if(leftDown(x,y) != null) return LEFTDOWN;
        else if(rightUp(x,y) != null) return RIGHTUP;
        else if(rightCenter(x,y) != null) return RIGHT;
        else if(rightDown(x,y) != null) return RIGHTDOWN;

        else {
            return UNKNOWN;
        }
    }

    /**
     * Methods to find direction from coordinates
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the direction, if any is applicable
     */
    public static Direction leftCenter(double x, double y) {
        if (x > 0 && x < 512 && y < 400 && y > 320) return LEFTCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction leftDown(double x, double y) {
        if (x > 0 && x < 512 && y < 320) return LEFTDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction leftUp(double x, double y) {
        if (x > 0 && x < 512 && y > 400) return LEFTDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction centerCenter(double x, double y) {
        if (x > 512 && x < 768 && y < 400 && y > 320) return CENTERCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction centerDown(double x, double y) {
        if (x > 512 && x < 768 && y < 320) return DOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction centerUp(double x, double y) {
        if (x > 512 && x < 768 && y > 400) return UP; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction rightDown(double x, double y) {
        if (x > 768 && x < 1280 && y < 320) return RIGHTDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction rightCenter(double x, double y) {
        if (x > 768 && x < 1280 && y < 400 && y > 320)
            return RIGHTCENTER; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
    public static Direction rightUp(double x, double y) {
        if (x > 768 && x < 1280 && y > 400)
            return RIGHTUP; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }
}
