package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.Socket;

public class BroadcastListenerAccepter extends Thread
{
    private InetAddress _Address;
    private boolean _Launched = false;
    boolean Launched() { return _Started; }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    private Socket _Socket;
    public boolean Stopped() { return _Stopped; }
    public void Stop() { _Stop = true; }
    ServerConnector _ServerConnector;
    protected void Handle()
    {
        Log("\n> BROADCAST REQUEST FROM "+_Address.getHostAddress().toString());
    }
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    BroadcastListenerAccepter(ServerConnector _ServerConnector, InetAddress _Address)
    {
        this._ServerConnector = _ServerConnector;
        this._Address=_Address;
    }
    public void run()
    {
        Handle();
    }
}