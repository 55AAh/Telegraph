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
    public static final int TIMEOUT = 100;
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
        _KnownRooms.clear();
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
        String IPS = Mask.substring(0, Mask.lastIndexOf('.')+1);
        StringBuilder IPSB = new StringBuilder(IPS);
        IPSB = IPSB.append(255);
        return IPSB.toString();
    }
    protected String Join(int ID)
    {
        if(_KnownRooms.size()<=ID) return null;
        return _KnownRooms.get(ID).Address.toString().substring(1);
    }
    FindRoomActivity _FindRoomActivity;
    public BroadcastSenderAccepter(FindRoomActivity _FindRoomActivity)
    {
        this._FindRoomActivity = _FindRoomActivity;
    }
    class RoomTTL
    {
        private InetAddress Address;
        private int TTL = DEFAULT_ROOM_TTL;
        public void Discover() { TTL = DEFAULT_ROOM_TTL; }
        public boolean CheckTTLExpired() { return TTL==0; }
        public void DecreaseTTL() { TTL--; }
        public boolean Compare(InetAddress Address)
        {
            return this.Address.toString().equals(Address.toString());
        }
        RoomTTL(InetAddress Address)
        {
            this.Address = Address;
        }
    }
    protected ArrayList<RoomTTL> _KnownRooms = new ArrayList<>();
    protected void AddRoom(InetAddress _Address, String _RoomName)
    {
        boolean Found = false;
        for(RoomTTL KnownRoom: _KnownRooms) if(KnownRoom.Compare(_Address))
        {
            KnownRoom.Discover();
            Found = true;
            break;
        }
        if(!Found)
        {
            _KnownRooms.add(new RoomTTL(_Address));
            _FindRoomActivity.AddRoom(_RoomName);
        }
    }
    private BroadcastSender _Sender;
    private Thread _SenderThread;
    protected void DecreaseTTL()
    {
        for(int c=0;c<_KnownRooms.size();c++)
        {
            _KnownRooms.get(c).DecreaseTTL();
            if(_KnownRooms.get(c).CheckTTLExpired())
            {
                _KnownRooms.remove(c);
                _FindRoomActivity._Rooms.remove(c);
                _FindRoomActivity.NotifyRoomsChanged();
                c--;
            }
        }
    }
    private void StartListener()
    {
        while(!_Stop)
        {
            try
            {
                DatagramSocket _Socket = new DatagramSocket(PORT);
                _Socket.setSoTimeout(TIMEOUT);
                while (!_Stop)
                {
                    boolean Connected = false;
                    while (!Connected && !_Stop)
                    {
                        Connected = true;
                        try
                        {
                            byte[] Buf = new byte[4096];
                            DatagramPacket _Packet = new DatagramPacket(Buf, Buf.length);
                            _Socket.receive(_Packet);
                            InetAddress _ClientAddress = _Packet.getAddress();
                            if (_Packet.getPort() == PORT)
                            {
                                RoomInfo Info = (RoomInfo) Package._GetObject(Buf);
                                AddRoom(_ClientAddress, Info.GetName());
                            }
                        }
                        catch (Exception e)
                        {
                            Connected = false;
                        }
                    }
                    if (_Stop)
                    {
                        _Socket.close();
                        _Stopped = true;
                        return;
                    }
                }
            }
            catch (Exception e) { }
        }
    }
    private void Start()
    {
        _Sender=new BroadcastSender(this);
        try
        {
            InetAddress MyIP = InetAddress.getByName(GetIP());
            _Sender.SetIP(MyIP);
        }
        catch (Exception e) { }
        _SenderThread=new Thread(_Sender);
        _SenderThread.start();
        while(!_Sender.Started());
        _Started = true;
        StartListener();
    }
    public void run()
    {
        Start();
    }
}
