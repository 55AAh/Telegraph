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
    protected final ArrayList<PackageTransmitter> _Stack = new ArrayList<>();
    protected Thread _Thread;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    public ClientListener(Client _Client, Socket _Socket)
    {
        this._Client = _Client;
        this._Socket = _Socket;
    }
    private void Fail()
    {
        _Client.Log("CLIENTLISTENER FAILED.");
    }
    public boolean HasPackages() { return !_Stack.isEmpty(); }
    private boolean _Stop = false;
    protected void Stop() { _Stop = true; }
    PackageTransmitter Get()
    {
        while(!HasPackages());
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
                if(_Stop) return;
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
