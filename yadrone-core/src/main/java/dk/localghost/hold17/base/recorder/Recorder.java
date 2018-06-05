package dk.localghost.hold17.base.recorder;

import dk.localghost.hold17.base.IARDrone;

import java.io.PrintStream;

/**
 * Records navdata or commands to a given stream.
 */
public class Recorder {
    IARDrone drone;

    /**
     * @param drone
     */
    public Recorder(IARDrone drone) {
        super();
        this.drone = drone;
    }

    private NavdataRecorder navdataRecorder;

    public void startRecordingNavdata(PrintStream stream, char separator) {
        if (navdataRecorder != null) return;
        navdataRecorder = new NavdataRecorder(stream, separator);
        drone.getNavDataManager().addCommonNavdataListener(navdataRecorder);
    }

    public void stopRecordingNavdata() {
        if (navdataRecorder == null) return;
        drone.getNavDataManager().removeCommonNavdataListener(navdataRecorder);
        navdataRecorder = null;
    }

    CommandsRecorder commandsRecorder = null;

    public void startRecordingCommands(PrintStream stream, char separator) {
        if (commandsRecorder != null) return;
        commandsRecorder = new CommandsRecorder(stream, separator);
        drone.getCommandManager().addCommandSentListener(commandsRecorder);

    }

    public void stopRecordingCommands() {
        if (commandsRecorder == null) return;
        drone.getCommandManager().removeCommandSentListener(commandsRecorder);
        commandsRecorder = null;
    }


}
