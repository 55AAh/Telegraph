package com.jcp83.telegraph;

import java.net.Socket;
import java.util.UUID;

class ServerAccepter implements Runnable
{
    private final int Timeout=1000;
    private final Server _Server;
    private final Socket _Socket;
    private ServerListener _ServerListener;
    private ServerSender _ServerSender;
    public ServerAccepter(Server _Server, Socket _Socket)
    {
        this._Server = _Server;
        this._Socket = _Socket;
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Failed = false;
    private void Fail()
    {
        _Failed = true;
    }
    private String _Name;
    private UUID _UUID;
    private int _TransmittersUID=0;
    private int GetNewTransmitterUID()
    {
        int UID = _TransmittersUID;
        _TransmittersUID++;
        return UID;
    }
    private void Accept()
    {
        try
        {
            _Socket.setSoTimeout(0);
            String Buf;
            Package P_LOGIN=(Package)Package._GetObject(_ServerListener.Get().GetData());
            Buf=(String)P_LOGIN.GetData();
            if(P_LOGIN.GetCommand()!= Command.LOGIN) { Fail(); return; }
            Package P_LOGIN_UUID=(Package)Package._GetObject(_ServerListener.Get().GetData());
            _UUID = (UUID)P_LOGIN_UUID.GetData();
            if(!Buf.equals("#AveJava#"))
            {
                Package P_LOGIN_FAILED = new Package(Command.LOGIN_FAILED, "", "SERVER");
                _ServerSender
                        .Send(P_LOGIN_FAILED.GetTransmitter(GetNewTransmitterUID()));
                Fail();
                return;
            }
            Package P_LOGIN_SUCCESS = new Package(Command.LOGIN_SUCCESS, "", "SERVER");
            _ServerSender.Send(P_LOGIN_SUCCESS.GetTransmitter(GetNewTransmitterUID()));
            _Socket.setSoTimeout(Timeout);
            _Name = P_LOGIN.GetSender();
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void Start()
    {
        _ServerListener = new ServerListener(_Server, _Socket, _UUID);
        Thread _ServerListenerThread = new Thread(_ServerListener);
        _ServerListenerThread.start();
        _ServerSender = new ServerSender(_Server, _Socket, _UUID);
        Thread _ServerSenderThread = new Thread(_ServerSender);
        _ServerSenderThread.start();
        while(!_ServerListener.Started()||!_ServerSender.Started());
        Accept();
        if(!_Failed) _Server.AddClient(_Name, _UUID, _ServerSender, _ServerListener, _ServerSenderThread, _ServerListenerThread);
        _Started = true;
    }
    public void run() { Start(); }
}
