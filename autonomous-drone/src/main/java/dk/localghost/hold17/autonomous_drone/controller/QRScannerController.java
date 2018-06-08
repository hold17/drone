package dk.localghost.hold17.autonomous_drone.controller;

import com.google.zxing.Result;

public class QRScannerController implements TagListener {
    private String lastScan = "";

    @Override
    public void onTag(Result result, float orientation) {
        if (result == null) return;

//        if (!lastScan.equals(result.getText()))
            System.out.println("QR Scanned, Result: " + result.getText());
        lastScan = result.getText();
    }
}
