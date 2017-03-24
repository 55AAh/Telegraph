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
        _ClientRoomActivity.ShowMessage(Msg);
    }
    private void Fail() { Log("\n> CLIENTCONNECTOR FAILED."); _Success = false; }
    private void Connect()
    {
        try
        {
            String serverIP = _ClientRoomActivity.GetServerIP();
            Log("\n> CONNECTING ...");
            InetAddress IPAddress=InetAddress.getByName(serverIP);
            Socket _Socket = new Socket(IPAddress, PORT);
            ClientListener _ClientListener = new ClientListener(_Client, _Socket);
            //_ClientListener._ClientRoomActivity = _ClientRoomActivity;
            _Client._ClientListener=_ClientListener;
            Thread _ClientListenerThread = new Thread(_ClientListener);
            _Client._ClientListenerThread = _ClientListenerThread;
            _ClientListenerThread.start();
            ClientSender _ClientSender = new ClientSender(_Client, _Socket);
            //_ClientSender._ClientRoomActivity = _ClientRoomActivity;
            _Client._ClientSender = _ClientSender;
            Thread _ClientSenderThread = new Thread(_ClientSender);
            _Client._ClientSenderThread = _ClientSenderThread;
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