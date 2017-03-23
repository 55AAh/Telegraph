package com.jcp83.telegraph;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class BroadcastSenderAccepter extends Thread
{
    public static final int PORT = 7002;
    public static final int DEFAULT_ROOM_TTL = 3;
    private final int WAIT=100;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    private boolean _Stop = false;
    public void Stop()
    {
        _Sender.Stop();
        while(!_Sender.Stopped());
        _Sender=null;
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
        if(_KnownRooms.size()<=ID) return null;
        return "";
    }
    FindRoomActivity _FindRoomActivity;
    public BroadcastSenderAccepter(FindRoomActivity _FindRoomActivity)
    {
        this._FindRoomActivity = _FindRoomActivity;
    }
    class RoomTTL
    {
        private String RoomName;
        private InetAddress Address;
        private int TTL = DEFAULT_ROOM_TTL;
        public void Discover() { TTL = DEFAULT_ROOM_TTL; }
        public boolean CheckTTLExpired() { return TTL==0; }
        public void DecreaseTTL() { TTL--; }
        RoomTTL(String RoomName, InetAddress Address)
        {
            this.RoomName = RoomName;
            this.Address = Address;
        }
    }
    private ArrayList<String> _KnownRooms = new ArrayList<>();
    protected void AddRoom(InetAddress _Address, String _RoomName)
    {
        boolean Found = false;
        for(String KnownRoom: _KnownRooms) if(KnownRoom.equals(_RoomName)) { Found=true; break; }
        if(!Found)
        {
            _FindRoomActivity.AddRoom(_RoomName);
            _KnownRooms.add(_RoomName);
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
    private void StartListener()
    {
        try
        {
            DatagramSocket _Socket = new DatagramSocket(PORT);
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
                            String Msg = (String)Package._GetObject(Buf);
                            AddRoom(_ClientAddress, Msg);
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
    }
    private void Start()
    {
        _Sender=new BroadcastSender(this);
        _SenderThread=new Thread(_Sender);
        _SenderThread.start();
        while(!_Sender.Started());
        _Started = true;
        if(!_Started) return;
        while(!_Stop&&!_Send)
        {
            try
            {
                Thread.sleep(WAIT);
            }
            catch (Exception e) { }
        }
        if(!_Send)
        {
            _Finding = true;
            try
            {
                InetAddress MyIP = InetAddress.getByName(GetIP());
                _Sender.SetIP(MyIP);
                _Sender.Start();
            }
            catch (Exception e) { }
            while(!_Stop)
            {

            }
        }
    }
    public void run()
    {
        Start();
    }
}
