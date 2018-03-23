package dk.localghost.hold17.base.command;

public class QuitCommand extends DroneCommand {
    @Override
    public Priority getPriority() {
        return Priority.MAX_PRIORITY;
    }
}
