/**
 *
 */
package dk.localghost.hold17.base.navdata.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Formicarufa (Tomas Prochazka)
 * 12. 3. 2016
 */
public final class CommonNavdataEvent {
    private boolean enabled = false;
    List<CommonNavdataListener> listeners = new ArrayList<CommonNavdataListener>();

    public void addListener(CommonNavdataListener l) {
        enabled = true;
        listeners.add(l);
    }

    public boolean removeListener(CommonNavdataListener l) {
        boolean remove = listeners.remove(l);
        if (listeners.isEmpty()) {
            enabled = false;
        }
        return remove;
    }

    public void invoke(CommonNavdata data, int missing) {
        List<CommonNavdataListener> temp = new ArrayList<CommonNavdataListener>(listeners);
        for (CommonNavdataListener listener : temp) {
            listener.navdataReceived(data, missing);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
