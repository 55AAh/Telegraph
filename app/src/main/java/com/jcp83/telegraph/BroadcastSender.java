package com.jcp83.telegraph;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class BroadcastSender extends Thread
{
    public static final int PORT = 7001;
    private DatagramSocket _Socket;
    private final int WAIT=500;
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
            _BroadcastSenderAccepter.DecreaseTTL();
            try
            {
                final byte Buf[] = new byte[0];
                DatagramPacket _Packet = new DatagramPacket(Buf, Buf.length, _BroadcastIP, PORT);
                _Socket.send(_Packet);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                Thread.sleep(WAIT);
            }
            catch (InterruptedException e) {}
        }
        _Socket.close();
        _Stopped = true;
    }
    public void run()
    {
        Init();
    }
}
