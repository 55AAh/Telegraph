package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class BroadcastSenderAccepter extends Thread
{
    private final int WAIT=100;
    protected void SetTimeout(int Timeout)
    {
        if(!_Started) return;
        _Sender.SetTimeout(Timeout);
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    private boolean _Stop = false;
    public void Stop()
    {
        _Sender.Stop();
        while(!_Sender.Stopped());
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
    protected String Join(int ID)
    {
        if(_Sockets.size()<=ID) return null;
        return _Sockets.get(ID).getInetAddress().toString().substring(1);
    }
    private int _Sent = 0;
    protected void NotifySent()
    {
        _Sent++;
        _FindRoomActivity.NotifySent(_Sent);
        if(_Sent==256) { _FindRoomActivity.NotifyRoomsAdded(); _Finding=false; }
    }
    FindRoomActivity _FindRoomActivity;
    public BroadcastSenderAccepter(FindRoomActivity _FindRoomActivity)
    {
        this._FindRoomActivity = _FindRoomActivity;
    }
    private ArrayList<Socket> _Sockets = new ArrayList<>();
    private ArrayList<String> _KnownRooms = new ArrayList<>();
    protected void AddSocket(Socket _Socket)
    {
        // TODO: 17.03.2017 Add room verification
        _Sockets.add(_Socket);
        String Room = _Socket.getInetAddress().toString().substring(1);
        boolean Found = false;
        for(String KnownRoom: _KnownRooms) if(KnownRoom.equals(Room)) { Found=true; break; }
        if(!Found)
        {
            _FindRoomActivity.AddRoom(Room);
            _KnownRooms.add(Room);
        }
    }
    private boolean _Finding=false;
    private boolean _Send=false;
    protected void Send()
    {
        _Send = true;
    }
    private BroadcastSender _Sender;
    private Thread _SenderThread;
    private void Start()
    {
        _Sender=new BroadcastSender(this);
        _SenderThread=new Thread(_Sender);
        _SenderThread.start();
        while(!_Sender.Started());
        _Started = true;
        if(!_Started) return;
        while(!_Stop)
        {
            try
            {
                Thread.sleep(WAIT);
            }
            catch (Exception e) { }
            if(!_Send) continue;
            _Finding = true;
            _Sent=0;
            _Sockets.clear();
            try
            {
                InetAddress MyIP = InetAddress.getByName(GetIP());
                _Sender.SetIP(MyIP);
                _Sender.Send();
            }
            catch (Exception e) { }
            while(_Finding);
        }
    }
    public void run()
    {
        Start();
    }
}
