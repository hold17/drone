package dk.localghost.hold17.base.navdata.common;

/**
 * @author Formicarufa (Tomas Prochazka)
 * 11. 3. 2016
 */
public interface CommonNavdataListener {
    void navdataReceived(CommonNavdata data, int missingNavdataCount);
}
