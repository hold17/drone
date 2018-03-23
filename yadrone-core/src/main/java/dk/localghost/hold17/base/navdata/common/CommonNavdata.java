/**
 *
 */
package dk.localghost.hold17.base.navdata.common;

/**
 * I have used a similar format of navdata to that which is used by the ardone_autonomy
 * project.
 *
 * @author Formicarufa (Tomas Prochazka) 11. 3. 2016
 * @see <a href =
 * "http://docs.ros.org/indigo/api/ardrone_autonomy/html/msg/Navdata.html">
 * Specification of the ROS navdata message.</a>
 */
public class CommonNavdata {
    /**
     * Time at the moment the message has been recorded. In milliseconds since
     * 1970.
     */
    public long time; // added later
    public int state; // DEMO OPTION
    public int battery; // DEMO OPTION
    /**
     * Magnitude
     */
    public int magX, magY, magZ; // MAGNETO Option
    public int pressure; // PRESSURE OPTION
    public int temperature; // PRESSURE OPTION
    /**
     * I assume that wind compensation theta corresponds to the drone rotation
     * theta, that is: the drone's pitch (and in the same manner compensation
     * phi gives the roll) (both quantities are in millidegrees)
     */
    public double windSpeed, windAngle, windCompAngleTheta, windCompAnglePhi;

    /**
     * forward/backward tilt,left/right tilt, rotation around the Z axis Remark:
     * the drone's axes are placed according the picture of a plane in the
     * ARDrone Developer Guide (page 6)
     */
    public double rotY, rotX, rotZ; // pitch-roll-yaw, theta,phi,psi
    // !Millidegrees!
    // DEMO OPTION

    public int altitude; // DEMO OPTION
    public double vx, vy, vz; // DEMO OPTION
    public double ax, ay, az; // Phys-measures option
    public int motor1, motor2, motor3, motor4; // PWM Option
    /**
     * Time in microseconds.
     */
    public long boardTime; // Time Option

    public String toString(String separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(time).append(separator).append(state).append(separator)
                .append(battery).append(separator).append(magX).append(separator).append(magY).append(separator)
                .append(magZ).append(separator).append(pressure).append(separator).append(temperature)
                .append(separator).append(windSpeed).append(separator).append(windAngle)
                .append(separator).append(windCompAngleTheta).append(separator)
                .append(windCompAnglePhi).append(separator).append(rotY).append(separator).append(rotX)
                .append(separator).append(rotZ).append(separator).append(altitude).append(separator).append(vx)
                .append(separator).append(vy).append(separator).append(vz).append(separator).append(ax).append(separator)
                .append(ay).append(separator).append(az).append(separator).append(motor1).append(separator)
                .append(motor2).append(separator).append(motor3).append(separator).append(motor4)
                .append(separator).append(boardTime);
        return builder.toString();
    }

    public String toString(char separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(time).append(separator).append(state).append(separator)
                .append(battery).append(separator).append(magX).append(separator).append(magY).append(separator)
                .append(magZ).append(separator).append(pressure).append(separator).append(temperature)
                .append(separator).append(windSpeed).append(separator).append(windAngle)
                .append(separator).append(windCompAngleTheta).append(separator)
                .append(windCompAnglePhi).append(separator).append(rotY).append(separator).append(rotX)
                .append(separator).append(rotZ).append(separator).append(altitude).append(separator).append(vx)
                .append(separator).append(vy).append(separator).append(vz).append(separator).append(ax).append(separator)
                .append(ay).append(separator).append(az).append(separator).append(motor1).append(separator)
                .append(motor2).append(separator).append(motor3).append(separator).append(motor4)
                .append(separator).append(boardTime);
        return builder.toString();
    }


}
