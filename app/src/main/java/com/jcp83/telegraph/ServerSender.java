package com.jcp83.telegraph;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

class ServerSender implements Runnable
{
    private final Server _Server;
    private final Socket _Socket;
    private OutputStream _Stream;
    private DataOutputStream _DStream;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    protected UUID _UUID;
    protected Thread _Thread;
    public ServerSender(Server _Server, Socket _Socket, UUID _UUID)
    {
        this._Server = _Server;
        this._Socket = _Socket;
        this._UUID = _UUID;
    }
    private void Fail()
    {
        _Server.DisconnectClient(_UUID, true);
    }
    void Send(PackageTransmitter PT)
    {
        try
        {
            byte[] B = Package._GetBytes(PT);
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
