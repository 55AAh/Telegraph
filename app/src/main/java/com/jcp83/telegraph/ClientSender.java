package com.jcp83.telegraph;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

class ClientSender implements Runnable
{
    private final Client _Client;
    private final Socket _Socket;
    private OutputStream _Stream;
    private DataOutputStream _DStream;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    ClientSender(Client _Client, Socket _Socket)
    {
        this._Client = _Client;
        this._Socket = _Socket;
    }
    //private void Log(String Msg) { _Client.Log(Msg); }
    private void Fail()
    {
        _Client.Log("\tClientSender failed.");
    }
    void Send(Package P)
    {
        try
        {
            byte[] B = Package._GetBytes(P);
            _DStream.writeInt(B.length);
            _DStream.flush();
            _Stream.write(B);
            _Stream.flush();
        }
        catch (Exception e) { Fail(); }
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
