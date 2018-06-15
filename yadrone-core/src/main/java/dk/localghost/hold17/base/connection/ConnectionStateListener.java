package dk.localghost.hold17.base.connection;

/**
 * * Interface of listeners of the connection state:
 * connected - disconnected - unknown.
 */
public interface ConnectionStateListener {
    void stateChanged(ConnectionState newState);
}
