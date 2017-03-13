package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class BroadcastSenderAccepter extends Thread
{
    public static final int PORT = 7001;
    private boolean _Starting = false;
    boolean Starting() { return _Starting; }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    private boolean _Stop = false;
    public void Stop()
    {
        Log("\nSTOPPING ...");
        Log("\nStopping : ");
        for(int S=0;S<16;S++)
            _Senders[S].Stop();
        for(int S=0;S<16;S++)
        {
            while(!_Senders[S].Stopped());
            Log(".");
        }
        Log("\nSTOPPED.");
        _Stopped = true;
    }
    private String GetIP()
    {
        String Mask = "127.0.0.1";
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface Interface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (Interface.isLoopback() || !Interface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = Interface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress Address = addresses.nextElement();
                    Mask = Address.getHostAddress();
                }
            }
        }
        catch (SocketException e) { }
        return Mask;
    }
    protected void Join(int ID)
    {
        if(_Sockets.size()<=ID) return;
        Log("JOINING TO "+_Addresses.get(ID)+" ...");
    }
    FindRoomActivity _FindRoomActivity;
    public BroadcastSenderAccepter(FindRoomActivity _FindRoomActivity)
    {
        this._FindRoomActivity = _FindRoomActivity;
    }
    protected void Log(String Msg)
    {
        _FindRoomActivity.ShowMessage(Msg);
    }
    private ArrayList<Socket> _Sockets = new ArrayList<>();
    private ArrayList<String> _Addresses = new ArrayList<>();
    protected void AddSocket(Socket _Socket, InetAddress _Address)
    {
        _Sockets.add(_Socket);
        _Addresses.add(_Address.toString().substring(1));
        _FindRoomActivity.AddRoom(_Address.toString());
    }
    protected void Send()
    {
        _FindRoomActivity.ClearRooms();
        Log("\nSEARCHING ...");
        _Sockets.clear();
        try
        {
            InetAddress MyIP = InetAddress.getByName(GetIP());
            for(int S = 0; S < 16; S++)
            {
                _Senders[S].SetIP(MyIP);
                _Senders[S].Send();
            }
        }
        catch (Exception e) { }
    }
    private BroadcastSender[] _Senders = new BroadcastSender[16];
    private Thread[] _SenderThreads = new Thread[16];
    private void Start()
    {
        Log("\nSTARTING ...");
        _Starting = true;
        Log("\nStarting : ");
        for(int S=0;S<16;S++)
        {
            _Senders[S]=new BroadcastSender(this, S*16);
            _SenderThreads[S]=new Thread(_Senders[S]);
            _SenderThreads[S].start();
        }
        for(int S=0;S<16;S++)
        {
            while(!_Senders[S].Started());
            Log(".");
        }
        Log(" Ready");
        _Started = true;
        Log("\nSTARTED.");
    }
    public void run()
    {
        Start();
    }
}
