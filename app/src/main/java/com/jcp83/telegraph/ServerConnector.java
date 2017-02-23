package com.jcp83.telegraph;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerConnector extends Thread
{
    public static final int CLIENT_ACCEPT_TIMEOUT = 500;
    protected Server _Server;
    protected Socket _Socket;
    private int PORT;
    private ServerRoomActivity _ServerRoomActivity;
    public ServerConnector(ServerRoomActivity _ServerRoomActivity, Server _Server, int PORT)
    {
        this._ServerRoomActivity = _ServerRoomActivity;
        this._Server = _Server;
        this.PORT = PORT;
    }
    private boolean _Started = false;
    protected boolean Started() { return _Started; }
    private boolean _Stopped = false;
    protected boolean Stopped() { return _Stopped; }
    private void Log(String Msg)
    {
        _ServerRoomActivity.ShowMessage(Msg);
    }
    private boolean StopF = false;
    protected void Stop()
    {
        StopF = true;
    }
    private void Connect()
    {
        Log("PORT : "+PORT);
        Log("Waiting client connections ...");
        try
        {
            _Started = true;
            ServerSocket _ServerSocket = new ServerSocket(PORT);
            int ConnectionID=0;
            while(true)
            {
                boolean Connected = false;
                while(!Connected&&!StopF)
                {
                    Connected = true;
                    _ServerSocket.setSoTimeout(CLIENT_ACCEPT_TIMEOUT);
                    try
                    {
                        _Socket = _ServerSocket.accept();
                    }
                    catch (SocketTimeoutException e){Connected = false;}
                }
                if(StopF) { _ServerSocket.close(); _Stopped = true; return; }
                ServerAccepter _ServerAccepter = new ServerAccepter(_Server, _Socket, _ServerRoomActivity);
                Thread _ServerAccepterThread = new Thread(_ServerAccepter);
                _ServerAccepterThread.start();
                while(!_ServerAccepter.Started());
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
    public void run()
    {
        while(!StopF) Connect();
    }
}