package dk.localghost.hold17.base.command.event;

import dk.localghost.hold17.base.command.ATCommand;

/**
 * Callback executed after a command is sent to the drone.
 */
public interface CommandSentListener {
    /**
     * Called on the thread which sends commands to the drone.
     * Do not perform any time-consuming action!
     *
     * @param command
     */
    void commandSent(ATCommand command);
}
