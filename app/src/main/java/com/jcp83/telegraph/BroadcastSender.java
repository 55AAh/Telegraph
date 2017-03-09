package com.jcp83.telegraph;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BroadcastSender extends Thread
{
    public static final int BUFSIZE = 255;
    public static final int PORT = 7001;
    private byte[] _Buf = new byte[BUFSIZE];
    private DatagramSocket _Socket;
    private DatagramPacket _Packet;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    private boolean _Stop = false;
    public void Stop() { Log("BROADCAST SENDER STOPPED."); _Stopped = true; }
    private void Init()
    {
        try
        {
            _Socket = new DatagramSocket();
            _Socket.setBroadcast(true);
            _Packet = new DatagramPacket(_Buf, _Buf.length, InetAddress.getByName("255.255.255.255"), PORT);
        }
        catch (Exception e) { }
        _Started = true;
    }
    FindRoomActivity _FindRoomActivity;
    protected void Log(String Msg)
    {
        _FindRoomActivity.ShowMessage(Msg);
    }
    public BroadcastSender(FindRoomActivity _FindRoomActivity)
    {
        this._FindRoomActivity = _FindRoomActivity;
    }
    public void Send()
    {
        Log("Searching rooms ...");
        try
        {
            _Socket.send(_Packet);
        }
        catch (Exception e) { }
    }
    public void run()
    {
        Init();
    }
}
