package com.jcp83.telegraph;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class BroadcastListener extends Thread
{
    public static final int PORT = 7001;
    public static final int TIMEOUT = 500;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    private String _RoomName;
    public boolean Stopped() { return _Stopped; }
    public void Stop() { _Stop = true; }
    private ServerConnector _ServerConnector;
    private BroadcastListenerAccepter _ListenerAccepter;
    private Thread _ListenerAccepterThread;
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    public BroadcastListener(ServerConnector _ServerConnector, String _RoomName)
    {
        this._ServerConnector = _ServerConnector;
        this._RoomName = _RoomName;
    }
    private void Start()
    {
        DatagramSocket _Socket = null;
        try
        {
            _ListenerAccepter = new BroadcastListenerAccepter(_ServerConnector, _RoomName);
            _ListenerAccepterThread = new Thread(_ListenerAccepter);
            _ListenerAccepterThread.start();
            while(!_ListenerAccepter.Started());
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
                        if(_Packet.getPort()==PORT)
                        {
                            _ListenerAccepter.Send(_ClientAddress);
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
