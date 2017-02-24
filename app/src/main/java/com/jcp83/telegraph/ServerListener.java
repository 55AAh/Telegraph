package com.jcp83.telegraph;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

class ServerListener implements Runnable
{
    private final Server _Server;
    private final Socket _Socket;
    private InputStream _Stream;
    private DataInputStream _DStream;
    final ArrayList<Package> _Stack = new ArrayList<>();
    public ServerListener(Server _Server, Socket _Socket)
    {
        this._Server = _Server;
        this._Socket = _Socket;
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    //private void Log(String Msg) { _Server.Log(Msg); }
    private void Fail()
    {
        _Server.Log("\tServerListener failed.");
    }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    void Stop() { _Stop = true; }
    boolean IsStopped() { return _Stopped; }
    Package Get()
    {
        while(_Stack.isEmpty());
        Package P = _Stack.get(0);
        _Stack.remove(0);
        return P;
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
                        Package P = Package.GetPackage(B);
                        if(P==null) { Fail(); return; }
                        _Stack.add(P);
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
