/**
 *
 */
package dk.localghost.hold17.base.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Formicarufa (Tomas Prochazka) 19. 3. 2016
 */
public class ConnectionStateEvent {
    private ConnectionState currentState = ConnectionState.Disconnected;
    private List<ConnectionStateListener> listeners = Collections
            .synchronizedList(new ArrayList<ConnectionStateListener>());

    /**
     * Checks if a change has occurred and notifies listeners.
     *
     * @param state
     */
    public void setCurrentState(ConnectionState state) {
        if (state != currentState) {
            notifyListeners(state);
            this.currentState = state;
        }
    }

    /**
     * Sets the current state to ConnectionState.Connected
     */
    public void stateConnected() {
        setCurrentState(ConnectionState.Connected);
    }

    /**
     * Sets the current state to ConnectionState.Disconnected
     */
    public void stateDisconnected() {
        setCurrentState(ConnectionState.Disconnected);
    }

    /**
     * Sets the current state to ConnectionState.Unknown
     */
    public void stateUnknown() {
        setCurrentState(ConnectionState.Unknown);
    }


    public ConnectionState getCurrentState() {
        return currentState;
    }

    public boolean addListener(ConnectionStateListener listener) {
        return listeners.add(listener);
    }

    private void notifyListeners(ConnectionState state) {
        List<ConnectionStateListener> copy;
        synchronized (listeners) {
            copy = new ArrayList<>(listeners);
        }
        for (ConnectionStateListener connectionStateListener : copy) {
            connectionStateListener.stateChanged(state);
        }
    }

    public boolean removeListener(ConnectionStateListener l) {
        return listeners.remove(l);

    }

}
