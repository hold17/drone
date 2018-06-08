package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.Result;

public interface TagListener {

    public void onTag(Result result, float orientation);

}
