package com.jcp83.telegraph;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

class ServerListener implements Runnable
{
    private Server _Server;
    private Socket _Socket;
    private ObjectInputStream _Stream;
    private ArrayList<Package> _Stack = new ArrayList<>();
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
    public boolean HasPackages() { return !_Stack.isEmpty(); }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    protected void Stop() { _Stop = true; }
    protected boolean IsStopped() { return _Stopped; }
    public Package Get()
    {
        while(!HasPackages());
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
                Log(".");
                if(_Stream.available()>0) { _Stack.add((Package) _Stream.readObject()); Log("AVAIL"); }
                Thread.sleep(2000);
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
        try { _Stream = new ObjectInputStream(_Socket.getInputStream()); }
        catch (Exception e) { Fail(); return; }
        _Started = true;
        Start();
    }
    public void run() { Init(); }
}
