package com.jcp83.telegraph;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

class ServerListener implements Runnable
{
    private final Server _Server;
    private final Socket _Socket;
    private InputStream _Stream;
    private DataInputStream _DStream;
    final ArrayList<PackageTransmitter> _Stack = new ArrayList<>();
    protected UUID _UUID;
    protected Thread _Thread;
    public ServerListener(Server _Server, Socket _Socket, UUID _UUID)
    {
        this._Server = _Server;
        this._Socket = _Socket;
        this._UUID = _UUID;
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private void Fail()
    {
        _Server.Log("\n> SERVERLISTENER FAILED.");
    }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    void Stop() { _Stop = true; }
    boolean IsStopped() { return _Stopped; }
    PackageTransmitter Get()
    {
        while(_Stack.isEmpty());
        PackageTransmitter PT = _Stack.get(0);
        _Stack.remove(0);
        return PT;
    }
    private void Start()
    {
        while(true)
        {
            try
            {
                if(_DStream.available()>0)
                {
                    int S = _DStream.readInt();
                    while(_Stream.available()<S&&!_Stop);
                    if(!_Stop)
                    {
                        byte[] B = new byte[S];
                        _Stream.read(B);
                        PackageTransmitter PT = (PackageTransmitter) Package._GetObject(B);
                        if(PT==null) { Fail(); return; }
                        _Stack.add(PT);
                    }
                }
                if(_Stop) { _Stopped = true; return; }
            }
            catch (Exception e)
            {
                Fail();
                return;
            }
        }
    }
    private void Init()
    {
        try { _Stream = _Socket.getInputStream(); }
        catch (Exception e) { Fail(); return; }
        _DStream = new DataInputStream(_Stream);
        _Started = true;
        Start();
    }
    public void run() { Init(); }
}
