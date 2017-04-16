package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

class ClientConnector extends Thread
{
    private final Client _Client;
    private int PORT = 0;
    private final ClientRoomActivity _ClientRoomActivity;
    ClientConnector(ClientRoomActivity _ClientRoomActivity, Client _Client, int PORT)
    {
        this._ClientRoomActivity = _ClientRoomActivity;
        this._Client = _Client;
        this.PORT = PORT;
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Success = true;
    boolean Success() { return _Success; }
    private void Log(String Msg)
    {
        _Client.Log(Msg);
    }
    private void Fail() { Log("CLIENTCONNECTOR FAILED."); _Success = false; }
    private void Connect()
    {
        try
        {
            String serverIP = _ClientRoomActivity.GetServerIP();
            Log("CONNECTING ...");
            InetAddress IPAddress=InetAddress.getByName(serverIP);
            Socket _Socket = new Socket(IPAddress, PORT);
            ClientListener _ClientListener = new ClientListener(_Client, _Socket);
            Thread _ClientListenerThread = new Thread(_ClientListener);
            _ClientListener._Thread = _ClientListenerThread;
            ClientSender _ClientSender = new ClientSender(_Client, _Socket);
            Thread _ClientSenderThread = new Thread(_ClientSender);
            _ClientSender._Thread = _ClientSenderThread;
            _Client.Info = new ServerInfo(_Client, _ClientListener, _ClientSender);
            _Client.Info._TasksPushStack.add(new PackageTask(_Client.GetNewTaskUID()));
            _Client.Info._TasksPopStack.add(new PackageTask(_Client.GetNewTaskUID()));
            _ClientListenerThread.start();
            _ClientSenderThread.start();
            while(!_ClientListener.Started()||!_ClientSender.Started());
            _Started = true;
        }
        catch (Exception e) { Fail();
        e.printStackTrace();}
    }
    public void run()
    {
        Connect();
    }
}