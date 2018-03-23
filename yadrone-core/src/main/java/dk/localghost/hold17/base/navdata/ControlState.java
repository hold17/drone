package dk.localghost.hold17.base.navdata;


public enum ControlState {

    DEFAULT, INIT, LANDED, FLYING, HOVERING, TEST, TRANS_TAKEOFF, TRANS_GOTOFIX, TRANS_LANDING, /* Meaning unknown.*/ TRANS_LOOPING;

    public static ControlState fromInt(int v) {
        ControlState[] values = values();
        if (v < 0 || v > values.length) {
            return null;
        }
        return values[v];
    }
}