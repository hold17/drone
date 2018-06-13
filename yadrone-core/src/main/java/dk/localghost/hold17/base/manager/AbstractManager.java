/*
 *
  Copyright (c) <2011>, <Shigeo Yoshida>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
The names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dk.localghost.hold17.base.manager;

import dk.localghost.hold17.base.connection.ConnectionStateEvent;
import dk.localghost.hold17.base.connection.ConnectionStateListener;

import java.net.InetAddress;

public abstract class AbstractManager implements Runnable {

    protected InetAddress inetaddr;
    protected Thread thread = null;
    protected boolean connected = false;
    protected ConnectionStateEvent connectionStateEvent = new ConnectionStateEvent();

    public AbstractManager(InetAddress inetaddr) {
        this.inetaddr = inetaddr;
    }

    public boolean isConnected() {
        return connected;
    }

    public void addConnectionStateListener(ConnectionStateListener l) {
        connectionStateEvent.addListener(l);
    }

    public void removeConnectionStateListeners(ConnectionStateListener l) {
        connectionStateEvent.removeListener(l);
    }

}
