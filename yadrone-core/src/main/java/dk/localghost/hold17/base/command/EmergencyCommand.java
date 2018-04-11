package dk.localghost.hold17.base.command;

public class EmergencyCommand extends RefCommand {
    public EmergencyCommand() {
        super(false, true);
    }

    @Override
    public Priority getPriority() {
        return Priority.MAX_PRIORITY;
    }
}
