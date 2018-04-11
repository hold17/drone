package dk.localghost.hold17.base.navdata;

import java.util.EventListener;


public interface CounterListener extends EventListener {

    public void update(Counters d);

}
