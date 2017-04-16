package com.jcp83.telegraph;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

class ServerConnector extends Thread
{
    private static final int CLIENT_ACCEPT_TIMEOUT = 500;
    private final Server _Server;
    private Socket _Socket;
    private final int PORT;
    private final ServerRoomActivity _ServerRoomActivity;
    public ServerConnector(ServerRoomActivity _ServerRoomActivity, Server _Server, int PORT)
    {
        this._ServerRoomActivity = _ServerRoomActivity;
        this._Server = _Server;
        this.PORT = PORT;
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stopped = false;
    boolean Stopped() { return _Stopped; }
    protected void Log(String Msg)
    {
        _Server.Log(Msg);
    }
    private boolean StopF = false;
    void Stop()
    {
        StopF = true;
    }
    private void Connect()
    {
        try
        {
            _Started = true;
            ServerSocket _ServerSocket = new ServerSocket(PORT);
            _ServerSocket.setSoTimeout(CLIENT_ACCEPT_TIMEOUT);
            while(true)
            {
                boolean Connected = false;
                while(!Connected&&!StopF)
                {
                    Connected = true;
                    try
                    {
                        _Socket = _ServerSocket.accept();
                    }
                    catch (SocketTimeoutException e) { Connected = false; }
                }
                if(StopF) { _ServerSocket.close(); return; }
                ServerAccepter _ServerAccepter = new ServerAccepter(_Server, _Socket);
                Thread _ServerAccepterThread = new Thread(_ServerAccepter);
                _ServerAccepterThread.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try {
                _Socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    BroadcastListener _Listener;
    Thread _ListenerThread;
    private void StartBroadcastReceiver()
    {
        _Listener = new BroadcastListener(this, _Server.GetRoomName());
        _ListenerThread = new Thread(_Listener);
        _ListenerThread.start();
        while(!_Listener.Started());
    }
    private void StopBroadcastReceiver()
    {
        _Listener.Stop();
        while(!_Listener.Stopped());
        _Stopped = true;
    }
    public void run()
    {
        StartBroadcastReceiver();
        while(!StopF) Connect();
        StopBroadcastReceiver();
    }
}