package com.jcp83.telegraph;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientSender implements Runnable
{
    private Client _Client;
    private Socket _Socket;
    private ObjectOutputStream _Stream;
    private boolean _Started = false;
    protected boolean Started() { return _Started; }
    ClientSender(Client _Client, Socket _Socket)
    {
        this._Client = _Client;
        this._Socket = _Socket;
    }
    private void Log(String Msg) { _Client.Log(Msg); }
    private void Fail()
    {
        Log("\tClientSender failed.");
    }
    protected boolean Send(Package P)
    {
        try { _Stream.writeObject(P); }
        catch (Exception e) { Fail(); return false; }
        return true;
    }
    protected void Flush()
    {
        try { _Stream.flush(); }
        catch (Exception e) { Fail(); }
    }
    private void Init()
    {
        try { _Stream = new ObjectOutputStream(_Socket.getOutputStream()); }
        catch (Exception e) { Fail(); return; }
        _Started = true;
    }
    public void run() { Init(); }
}
