package com.jcp83.telegraph;

import java.io.ObjectInputStream;
import java.net.Socket;

class ClientListener implements Runnable
{
    private Client _Client;
    private Socket _Socket;
    private ObjectInputStream _Stream;
    private boolean _Started = false;
    protected boolean Started() { return _Started; }
    public ClientListener(Client _Client, Socket _Socket)
    {
        this._Client = _Client;
        this._Socket = _Socket;
    }
    private void Log(String Msg) { _Client.Log(Msg); }
    private void Fail()
    {
        Log("\tClientListener failed.");
    }
    private class Getter implements Runnable
    {
        private ObjectInputStream _Stream;
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
    }
    private void Init()
    {
        try { _Stream = new ObjectInputStream(_Socket.getInputStream()); }
        catch (Exception e) { Fail(); return; }
        _Started = true;
    }
    public void run() { Init(); }
}
