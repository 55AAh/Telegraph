package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BroadcastSender extends Thread
{
    public static final int PORT = 7001;
    private final int TIMEOUT=10;
    private final int WAIT=100;
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
    protected void Log(String Msg)
    {
        _BroadcastSenderAccepter.Log(Msg);
    }
    private int _StartDevice = 0;
    public BroadcastSender(BroadcastSenderAccepter _BroadcastSenderAccepter)
    {
        this._BroadcastSenderAccepter = _BroadcastSenderAccepter;
        this._StartDevice = _StartDevice;
    }
    private int _Sent = 0;
    protected void NotifySent()
    {
        _Sent++;
        if(_Sent%16==0) Log(".");
        if(_Sent==256) Log(" OK");
    }
    private InetAddress _IP;
    protected void SetIP(InetAddress _IP)
    {
        this._IP = _IP;
    }
    private boolean _Send = false;
    private void Start()
    {
        _Started = true;
        while (!_Stop)
        {
            try
            {
                Thread.sleep(WAIT);
            }
            catch (InterruptedException e) {}
            if (!_Send) continue;
            _Sent=0;
            byte[] BAddress = _IP.getAddress();
            for (int D = _StartDevice; D < 256; D++)
            {
                NotifySent();
                try
                {
                    BAddress[3] = (byte) D;
                    InetSocketAddress ISA = new InetSocketAddress(InetAddress.getByAddress(BAddress), PORT);
                    Socket _Socket = new Socket();
                    _Socket.connect(ISA, TIMEOUT);
                    if (_Socket.isConnected())
                    {
                        Log("\nFOUND " + D + ".\n");
                        _BroadcastSenderAccepter.AddSocket(_Socket, InetAddress.getByAddress(BAddress));
                    }
                }
                catch (Exception e) { }
            }
            _Send = false;
        }
        _Stopped = true;
    }
    public void Send()
    {
        _Send = true;
    }
    public void run()
    {
        Start();
    }
}
