package com.jcp83.telegraph;

import java.io.ObjectInputStream;
import java.net.Socket;

class ServerListener implements Runnable
{
    private Server _Server;
    private Socket _Socket;
    private ObjectInputStream _Stream;
    public ServerListener(Server _Server, Socket _Socket)
    {
        this._Server = _Server;
        this._Socket = _Socket;
    }
    private boolean _Started = false;
    protected boolean Started() { return _Started; }
    private void Log(String Msg) { _Server.Log(Msg); }
    private void Fail()
    {
        Log("\tServerListener failed.");
    }
    protected Package Get()
    {
        Package P;
        try { P = (Package)_Stream.readObject(); }
        catch (Exception e) { Fail(); return null; }
        return P;
    }
    private void Init()
    {
        try { _Stream = new ObjectInputStream(_Socket.getInputStream()); }
        catch (Exception e) { Fail(); return; }
        _Started = true;
    }
    public void run() { Init(); }
}
