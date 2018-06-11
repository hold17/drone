package dk.localghost.hold17.autonomous_drone.opencv_processing;

public enum Direction {
    LEFTCENTER, RIGHTCENTER, CENTERCENTER,UP, CENTERDOWN, CENTERUP, LEFTUP, RIGHTUP, DOWN, LEFTDOWN, RIGHTDOWN, UNKNOWN;

    /**
     * Method that finds the direction
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the direction if it can be determined.
     */
    public static Direction exactDirection(double x, double y){
        if(Direction.centerUp(x,y) != null) return Direction.CENTERUP;
        else if(Direction.centerCenter(x,y) != null) return Direction.CENTERCENTER;
        else if(Direction.centerDown(x,y) != null) return Direction.CENTERDOWN;
        else if(Direction.leftUp(x,y) != null) return Direction.LEFTUP;
        else if(Direction.leftCenter(x,y) !=null) return Direction.LEFTCENTER;
        else if(Direction.leftDown(x,y) != null) return Direction.LEFTDOWN;
        else if(Direction.rightUp(x,y) != null) return Direction.RIGHTUP;
        else if(Direction.rightCenter(x,y) != null) return Direction.RIGHTCENTER;
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
        if (x > 512 && x < 768 && y < 320) return Direction.CENTERDOWN; // 256px (1/5 af billedeopløsningen på 1280)
        else return null;
    }

    public static Direction centerUp(double x, double y) {
        if (x > 512 && x < 768 && y > 400) return Direction.CENTERUP; // 256px (1/5 af billedeopløsningen på 1280)
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
