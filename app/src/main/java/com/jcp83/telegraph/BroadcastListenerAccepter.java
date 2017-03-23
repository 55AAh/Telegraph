package com.jcp83.telegraph;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

public class BroadcastListenerAccepter extends Thread
{
    public static final int PORT = 7002;
    private String _RoomName;
    private InetAddress _Address;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private DatagramSocket _Socket;
    ServerConnector _ServerConnector;
    protected void Handle()
    {
        RoomInfo Info = new RoomInfo(_RoomName);
        final byte[] Buf = Package._GetBytes(Info);
        DatagramPacket _Packet = new DatagramPacket(Buf, Buf.length, _Address, PORT);
        try
        {
            _Socket = new DatagramSocket(PORT);
            _Socket.send(_Packet);
        }
        catch (Exception e) {}
    }
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    BroadcastListenerAccepter(ServerConnector _ServerConnector, InetAddress _Address, String _RoomName)
    {
        this._ServerConnector = _ServerConnector;
        this._Address=_Address;
        this._RoomName = _RoomName;
    }
    public void run()
    {
        Handle();
    }
}