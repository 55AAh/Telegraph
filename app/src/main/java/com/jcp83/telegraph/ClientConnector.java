package com.jcp83.telegraph;

import java.net.InetAddress;
import java.net.Socket;

public class ClientConnector extends Thread
{
    private Socket _Socket;
    protected Client _Client;
    private int PORT = 0;
    private ClientRoomActivity _ClientRoomActivity;
    private String ServerIP;
    public ClientConnector(ClientRoomActivity _ClientRoomActivity, Client _Client, int PORT)
    {
        this._ClientRoomActivity = _ClientRoomActivity;
        this._Client = _Client;
        this.PORT = PORT;
    }
    private boolean _Started = false;
    protected boolean Started() { return _Started; }
    private void Log(String Msg)
    {
        _ClientRoomActivity.ShowMessage(Msg);
    }
    private void Fail() { Log("ClientConnector failed."); }
    private void Connect()
    {
        try
        {
            ServerIP = _ClientRoomActivity.GetServerIP();
            Log("Connecting to server at PORT "+PORT+" and address "+ServerIP+" ...");
            InetAddress IPAddress=InetAddress.getByName(ServerIP);
            _Socket = new Socket(IPAddress,PORT);
            Log("Connected.");
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
        catch (Exception e) { Fail(); }
    }
    public void run()
    {
        Connect();
    }
}