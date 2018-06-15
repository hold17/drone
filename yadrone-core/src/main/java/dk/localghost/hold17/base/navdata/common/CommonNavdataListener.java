package dk.localghost.hold17.base.navdata.common;

public interface CommonNavdataListener {
    void navdataReceived(CommonNavdata data, int missingNavdataCount);
}
