package dk.localghost.hold17.base.recorder;

import dk.localghost.hold17.base.command.ATCommand;
import dk.localghost.hold17.base.command.PCMDCommand;
import dk.localghost.hold17.base.command.PCMDMagCommand;
import dk.localghost.hold17.base.command.event.CommandSentListener;

import java.io.PrintStream;

class CommandsRecorder implements CommandSentListener {

    PrintStream stream;
    private char separator;

    /**
     * @param stream
     */
    public CommandsRecorder(PrintStream stream, char separator) {
        super();
        this.stream = stream;
        this.separator = separator;
    }

    /* (non-Javadoc)
     * @see dk.localghost.hold17.base.command.event.CommandSentListener#commandSent(dk.localghost.hold17.base.command.ATCommand)
     */
    @Override
    public void commandSent(ATCommand command) {
        if (command instanceof PCMDCommand) {
            if (command instanceof PCMDMagCommand) {
                throw new UnsupportedOperationException("Recording of absolute-control commands is not supported.");
            }
            PCMDCommand pcmdCommand = (PCMDCommand) command;
            stream.println(pcmdCommand.toString(separator));
        }
    }


}
