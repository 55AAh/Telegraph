package com.jcp83.telegraph;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class BroadcastListener extends Thread
{
    public static final int PORT = 7001;
    public static final int TIMEOUT = 500;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    private Socket _Socket;
    public boolean Stopped() { return _Stopped; }
    public void Stop() { _Stop = true; }
    private ServerConnector _ServerConnector;
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    public BroadcastListener(ServerConnector _ServerConnector)
    {
        this._ServerConnector = _ServerConnector;
    }
    private void Start()
    {
        DatagramSocket _Socket = null;
        try
        {
            _Socket = new DatagramSocket(PORT);
            _Socket.setSoTimeout(TIMEOUT);
        }
        catch (SocketException e) { }
        _Started = true;
        try
        {
            while(!_Stop)
            {
                boolean Connected = false;
                while(!Connected&&!_Stop)
                {
                    Connected = true;
                    try
                    {
                        byte[] Buf = new byte[256];
                        DatagramPacket _Packet = new DatagramPacket(Buf,Buf.length);
                        _Socket.receive(_Packet);
                        InetAddress _ClientAddress = _Packet.getAddress();
                        int P = _Packet.getPort();
                        if(_Packet.getPort()==PORT)
                        {
                            BroadcastListenerAccepter _ListenerAccepter = new BroadcastListenerAccepter(_ServerConnector, _ClientAddress);
                            Thread _ListenerAccepterThread = new Thread(_ListenerAccepter);
                            _ListenerAccepterThread.start();
                        }
                    }
                    catch (Exception e) { Connected = false; }
                }
                if(_Stop)
                {
                    _Socket.close();
                    _Stopped = true;
                    return;
                }
            }
        }
        catch (Exception e) { }
        _Stopped = true;
    }
    public void run()
    {
        Start();
    }
}
