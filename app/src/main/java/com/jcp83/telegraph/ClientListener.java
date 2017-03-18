package com.jcp83.telegraph;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

class ClientListener implements Runnable
{
    private final Client _Client;
    private final Socket _Socket;
    private InputStream _Stream;
    private DataInputStream _DStream;
    private final ArrayList<Package> _Stack = new ArrayList<>();
    private boolean _Started = false;
    boolean Started() { return _Started; }
    public ClientListener(Client _Client, Socket _Socket)
    {
        this._Client = _Client;
        this._Socket = _Socket;
    }
    //private void Log(String Msg) { _Client.Log(Msg); }
    private void Fail()
    {
        _Client.Log("\n> CLIENTLISTENER FAILED.");
    }
    public boolean HasPackages() { return !_Stack.isEmpty(); }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    protected void Stop() { _Stop = true; }
    protected boolean IsStopped() { return _Stopped; }
    /*private class Getter implements Runnable
    {
        private InputStream _Stream;
        //private DataInputStream
        private boolean _Ready = false;
        public boolean Ready() { return _Ready; }
        private Package P;
        public Package Get() { return P; }
        private void _Get()
        {
            try { P = (Package)_Stream.readObject(); }
            catch (Exception e) { e.printStackTrace(); Fail(); }
            _Ready = true;
        }
        public Getter(ObjectInputStream _Stream)
        {
            this._Stream = _Stream;
        }
        public void run()
        {
            _Get();
        }
    }
    protected Package Get()
    {
        Getter _Getter = new Getter(_Stream);
        Thread _Thread = new Thread(_Getter);
        _Thread.start();
        while(!_Getter.Ready());
        Package P = _Getter.Get();
        return P;
    }*/
    Package Get()
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
                if(_DStream.available()>0)
                {
                    int S = _DStream.readInt();
                    while(_Stream.available()<S&&!_Stop);
                    if(!_Stop)
                    {
                        byte[] B = new byte[S];
                        _Stream.read(B);
                        Package P = (Package)Package._GetObject(B);
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
        _Started = true;
    }
    public void run() { Init(); }
}
