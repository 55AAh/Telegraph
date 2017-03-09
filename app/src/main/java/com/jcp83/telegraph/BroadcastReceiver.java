package com.jcp83.telegraph;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class BroadcastReceiver extends Thread
{
    public static final int BUFSIZE = 255;
    public static final int PORT = 7001;
    public static final int TIMEOUT = 500;
    private byte[] _Buf = new byte[BUFSIZE];
    private DatagramSocket _Socket;
    private DatagramPacket _Packet;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    public void Stop() { _Stop = true; }
    private void Init()
    {
        try
        {
            _Socket = new DatagramSocket();
            _Socket.setBroadcast(true);
            _Packet = new DatagramPacket(_Buf, _Buf.length,InetAddress.getByName("255.255.255.255"), PORT);
        }
        catch (Exception e) { }
    }
    ServerConnector _ServerConnector;
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    public BroadcastReceiver(ServerConnector _ServerConnector)
    {
        this._ServerConnector = _ServerConnector;
    }
    private void Start()
    {
        Log("BROADCAST RECEIVER STARTED.");
        _Started = true;
        try
        {
            _Socket.setSoTimeout(TIMEOUT);
        }
        catch (SocketException e) { e.printStackTrace(); }
        while(!_Stop)
        {
            try
            {
                _Socket.receive(_Packet);
                Log("Received.");
                //String a = _Packet.getData().toString();
            }
            catch (Exception e) { }
        }
        Log("BROADCAST RECEIVER STOPPED");
        _Stopped = true;
    }
    public void run()
    {
        Init();
        Start();
    }
}
