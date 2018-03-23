/**
 *
 */
package dk.localghost.hold17.base.navdata.common;

import dk.localghost.hold17.base.navdata.*;

/**
 * @author Formicarufa (Tomas Prochazka)
 * 12. 3. 2016
 */
public class NavdataCollector implements TimeListener, PWMlistener, AcceleroListener, VelocityListener, AltitudeListener, AttitudeListener, WindListener, TemperatureListener, PressureListener, MagnetoListener, BatteryListener, StateListener {

    CommonNavdata result;
    private boolean activated = false;
    public static final int LISTENERS_COUNT = 13;
    private int unsetNavdata = LISTENERS_COUNT;

    /**
     * Navdata collector listens to changes of individual
     * values in the navdata and fills an instance of
     * CommonNavdata with the values
     */
    public NavdataCollector() {
        result = new CommonNavdata();
    }

    public void activate(NavDataManager manager) {
        if (activated) return;
        activated = true;
        manager.addStateListener(this);
        manager.addBatteryListener(this);
        manager.addMagnetoListener(this);
        manager.addPressureListener(this);
        manager.addTemperatureListener(this);
        manager.addWindListener(this);
        manager.addAttitudeListener(this);
        manager.addAltitudeListener(this);
        manager.addVelocityListener(this);
        manager.addAcceleroListener(this);
        manager.addPWMlistener(this);
        manager.addTimeListener(this);
    }

    public void deactivate(NavDataManager manager) {
        if (!activated) return;
        activated = false;
        manager.removeStateListener(this);
        manager.removeBatteryListener(this);
        manager.removeMagnetoListener(this);
        manager.removePressureListener(this);
        manager.removeTemperatureListener(this);
        manager.removeWindListener(this);
        manager.removeAttitudeListener(this);
        manager.removeAltitudeListener(this);
        manager.removeVelocityListener(this);
        manager.removeAcceleroListener(this);
        manager.removePWMlistener(this);
        manager.removeTimeListener(this);
    }

    /**
     * New CommonNavdata object is fully initialized iff unsetNavdataCount == 0.
     * if unsetNavdataCount>0 some navdata events haven't been invoked yet.
     *
     * @return
     */
    public int getUnsetNavdataCount() {
        return unsetNavdata;
    }

    /**
     * Gets the filled CommonNavdata objects
     * and starts filling the values to a new one.
     * To be called after all navdata are parsed.
     *
     * @return
     */
    public CommonNavdata getNavdata() {
        result.time = System.currentTimeMillis();
        CommonNavdata ret = result;
        result = new CommonNavdata();
        unsetNavdata = LISTENERS_COUNT;
        return ret;
    }

    @Override
    public void stateChanged(DroneState state) {
        // Skipping.

    }

    @Override
    public void controlStateChanged(ControlState state) {
        result.state = state.ordinal();
        unsetNavdata--;
    }

    @Override
    public void batteryLevelChanged(int percentage) {
        result.battery = percentage;
        unsetNavdata--;

    }

    @Override
    public void voltageChanged(int vbat_raw) {
        //nothing

    }

    @Override
    public void received(MagnetoData d) {
        short[] mag = d.getM();
        result.magX = mag[0];
        result.magY = mag[1];
        result.magZ = mag[2];
        unsetNavdata--;

    }

    @Override
    public void receivedKalmanPressure(KalmanPressureData d) {
    }

    @Override
    public void receivedPressure(Pressure d) {
        result.pressure = d.getMeasurement();
        unsetNavdata--;

    }

    @Override
    public void receivedTemperature(Temperature d) {
        result.temperature = d.getMeasurement();
        unsetNavdata--;

    }

    @Override
    public void receivedEstimation(WindEstimationData d) {
        result.windSpeed = d.getEstimatedSpeed();
        result.windAngle = d.getEstimatedAngle();
        unsetNavdata--;
    }

    @Override
    public void attitudeUpdated(float pitch, float roll, float yaw) {
        result.rotY = pitch;
        result.rotX = roll;
        result.rotZ = yaw;
        unsetNavdata--;

    }

    @Override
    public void attitudeUpdated(float pitch, float roll) {
        //Already captured.
    }

    @Override
    public void windCompensation(float pitch, float roll) {
        result.windCompAngleTheta = pitch;
        result.windCompAnglePhi = roll;
        unsetNavdata--;
    }

    @Override
    public void receivedAltitude(int altitude) {
        result.altitude = altitude;
        unsetNavdata--;

    }

    @Override
    public void receivedExtendedAltitude(Altitude d) {
        // Lets skip this.

    }

    @Override
    public void velocityChanged(float vx, float vy, float vz) {
        result.vx = vx;
        result.vy = vy;
        result.vz = vz;
        unsetNavdata--;
    }

    @Override
    public void receivedRawData(AcceleroRawData d) {
        int[] rawAccs = d.getRawAccs();
        result.ax = rawAccs[0];
        result.ay = rawAccs[1];
        result.az = rawAccs[2];
        unsetNavdata--;
    }

    @Override
    public void receivedPhysData(AcceleroPhysData d) {
        //No, thanks.

    }

    @Override
    public void received(PWMData d) {
        short[] motor = d.getMotor();
        result.motor1 = motor[0];
        result.motor2 = motor[1];
        result.motor3 = motor[2];
        result.motor4 = motor[3];
        unsetNavdata--;

    }

    /* (non-Javadoc)
     * @see dk.localghost.hold17.base.navdata.TimeListener#timeReceived(int, int)
     */
    @Override
    public void timeReceived(int seconds, int useconds) {
        result.boardTime = seconds * 1000L * 1000 + useconds;
        unsetNavdata--;
    }
}
