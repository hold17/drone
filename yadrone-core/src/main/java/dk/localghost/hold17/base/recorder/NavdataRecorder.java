package dk.localghost.hold17.base.recorder;

import dk.localghost.hold17.base.navdata.common.CommonNavdata;
import dk.localghost.hold17.base.navdata.common.CommonNavdataListener;
import dk.localghost.hold17.base.navdata.common.NavdataCollector;

import java.io.PrintStream;

/**
 * @author Formicarufa (Tomas Prochazka)
 * 12. 3. 2016
 */
class NavdataRecorder implements CommonNavdataListener {

    PrintStream stream;
    private char separator;

    /**
     * @param stream
     */
    public NavdataRecorder(PrintStream stream, char separator) {
        super();
        this.stream = stream;
        this.separator = separator;

    }

    /* (non-Javadoc)
     * @see dk.localghost.hold17.base.navdata.common.CommonNavdataListener#navdataReceived(dk.localghost.hold17.base.navdata.common.CommonNavdata)
     */
    @Override
    public void navdataReceived(CommonNavdata data, int missingNavdata) {
        if (missingNavdata == NavdataCollector.LISTENERS_COUNT) {
            System.err.println("Recorder error: required navdata not present in the message from the drone.");
            return;
        }
        if (missingNavdata > 0) {
            System.err.println("Recorder error: not all navdata present in the message from the drone.");
        }
        stream.println(data.toString(separator));
    }

}
