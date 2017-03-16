package com.jcp83.telegraph;

import java.net.Socket;

public class BroadcastListenerAccepter extends Thread
{
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
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    private void Start()
    {
        _Launched = true;
    }
    public void run()
    {
        Start();
    }
}
