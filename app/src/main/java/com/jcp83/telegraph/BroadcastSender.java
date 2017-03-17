package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BroadcastSender extends Thread
{
    public static final int PORT = 7001;
    private int TIMEOUT=0;
    protected void SetTimeout(int Timeout)
    {
        this.TIMEOUT = Timeout;
    }
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
    public BroadcastSender(BroadcastSenderAccepter _BroadcastSenderAccepter)
    {
        this._BroadcastSenderAccepter = _BroadcastSenderAccepter;
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
            byte[] BAddress = _IP.getAddress();
            for (int D = 0; D < 256&&!_Stop; D++)
            {
                _BroadcastSenderAccepter.NotifySent();
                try
                {
                    BAddress[3] = (byte) D;
                    InetSocketAddress ISA = new InetSocketAddress(InetAddress.getByAddress(BAddress), PORT);
                    Socket _Socket = new Socket();
                    _Socket.connect(ISA, TIMEOUT);
                    if (_Socket.isConnected())
                    {
                        _BroadcastSenderAccepter.AddSocket(_Socket);
                    }
                }
                catch (Exception e) { if(D==182) e.printStackTrace(); }
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
