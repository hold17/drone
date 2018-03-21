package dk.localghost.hold17.base.command;

public class ResetControlAckCommand extends ControlCommand {

	public ResetControlAckCommand() {
		super(ControlMode.ACK, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.localghost.hold17.base.command.ATCommand#getPriority()
	 */
	@Override
	public Priority getPriority() {
		return Priority.MAX_PRIORITY;
	}

}
