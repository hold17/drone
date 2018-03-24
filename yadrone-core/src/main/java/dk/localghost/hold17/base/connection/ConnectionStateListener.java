package dk.localghost.hold17.base.connection;

/**
 * * Interface of listeners of the connection state:
 * connected - disconnected - unknown.
 *
 * @author Formicarufa (Tomas Prochazka)
 * 19. 3. 2016
 */
public interface ConnectionStateListener {
    void stateChanged(ConnectionState newState);
}
