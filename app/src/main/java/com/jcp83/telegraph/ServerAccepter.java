package com.jcp83.telegraph;

import java.net.Socket;

class ServerAccepter implements Runnable
{
    private final int Timeout=1000;
    private final Server _Server;
    private final Socket _Socket;
    private final ServerRoomActivity _ServerRoomActivity;
    private ServerListener _ServerListener;
    private ServerSender _ServerSender;

    public ServerAccepter(Server _Server, Socket _Socket, ServerRoomActivity _ServerRoomActivity)
    {
        this._Server = _Server;
        this._Socket = _Socket;
        this._ServerRoomActivity = _ServerRoomActivity;
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private void Log(String Msg)
    {
        _ServerRoomActivity.ShowMessage(Msg);
    }
    private boolean _Failed = false;
    private void Fail()
    {
        _Failed = true;
    }
    private void Accept()
    {
        try
        {
            _Socket.setSoTimeout(0);
            String Buf;
            Package LOGIN_P= _ServerListener.Get();
            Buf=(String)LOGIN_P.GetData();
            if(LOGIN_P.GetCommand()!= Command.LOGIN) { Fail(); return; }
            if(!Buf.equals("#AveJava#"))
            {
                Package LOGIN_FAILED_P = new Package(Command.LOGIN_FAILED, "", "SERVER");
                _ServerSender.Send(LOGIN_FAILED_P);
                return;
            }
            Package INFO_LOGIN_P = new Package(Command.INFO_LOGIN, LOGIN_P.GetSender(), "SERVER");
            _Server.SendAll(INFO_LOGIN_P);
            Log("\n> "+LOGIN_P.GetSender()+" JOINED ROOM.");
            Package LOGIN_SUCCESS_P = new Package(Command.LOGIN_SUCCESS, "", "SERVER");
            _ServerSender.Send(LOGIN_SUCCESS_P);
            _Socket.setSoTimeout(Timeout);
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void Start()
    {
        _ServerListener = new ServerListener(_Server, _Socket);
        Thread _ServerListenerThread = new Thread(_ServerListener);
        _ServerListenerThread.start();
        _ServerSender = new ServerSender(_Server, _Socket);
        Thread _ServerSenderThread = new Thread(_ServerSender);
        _ServerSenderThread.start();
        while(!_ServerListener.Started()||!_ServerSender.Started());
        Accept();
        if(!_Failed) _Server.Add(_ServerSender, _ServerListener, _ServerSenderThread, _ServerListenerThread);
        _Started = true;
    }
    public void run() { Start(); }
}
