package com.jcp83.telegraph;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;

public class BroadcastSender extends Thread
{
    public static final int PORT = 7001;
    private DatagramSocket _Socket;
    private final int WAIT=2500;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    private boolean _Stop = false;
    public void Stop()
    {
        _Stop = true;
    }
    BroadcastSenderAccepter _BroadcastSenderAccepter;
    public BroadcastSender(BroadcastSenderAccepter _BroadcastSenderAccepter)
    {
        this._BroadcastSenderAccepter = _BroadcastSenderAccepter;
    }
    private InetAddress _BroadcastIP;
    protected void SetIP(InetAddress _IP)
    {
        this._BroadcastIP = _IP;
    }
    private boolean _Send = false;
    private void Init()
    {
        try
        {
            _Socket = new DatagramSocket(PORT);
        }
        catch (SocketException e) { }
        _Started = true;
        while (!_Stop)
        {
            try
            {
                Thread.sleep(WAIT);
            }
            catch (InterruptedException e) {}
            if (!_Send) continue;
            try
            {
                _BroadcastIP = InetAddress.getByName("192.168.43.255");
                final byte Buf[] = new byte[0];
                DatagramPacket _Packet = new DatagramPacket(Buf, Buf.length, _BroadcastIP, PORT);
                _Socket.send(_Packet);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        _Socket.close();
        _Stopped = true;
    }
    public void Start()
    {
        _Send = true;
    }
    public void run()
    {
        Init();
    }
}
