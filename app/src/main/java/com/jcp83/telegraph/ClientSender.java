package com.jcp83.telegraph;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

class ClientSender implements Runnable
{
    private Client _Client;
    private Socket _Socket;
    private OutputStream _Stream;
    private DataOutputStream _DStream;
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
        try
        {
            byte[] B = Package.GetBytes(P);
            _DStream.writeInt(B.length);
            _DStream.flush();
            _Stream.write(B);
            _Stream.flush();
        }
        catch (Exception e) { Fail(); return false; }
        return true;
    }
    private void Init()
    {
        try { _Stream = _Socket.getOutputStream(); }
        catch (Exception e) { Fail(); return; }
        _DStream = new DataOutputStream(_Stream);
        _Started = true;
    }
    public void run() { Init(); }
}
