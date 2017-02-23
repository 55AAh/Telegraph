package com.jcp83.telegraph;

import android.widget.Toast;

import java.util.ArrayList;

public class Server
{
    protected ServerConnector _ServerConnector = null;
    protected Thread _ServerConnectorThread = null;
    private int PORT;
    protected ArrayList<ServerListener> _ServerListeners = new ArrayList<>();
    protected ArrayList<ServerSender> _ServerSenders = new ArrayList<>();
    protected ArrayList<Thread> _ServerListenerThreads = new ArrayList<>();
    protected ArrayList<Thread> _ServerSenderThreads = new ArrayList<>();
    private ServerRoomActivity _ServerRoomActivity;
    public Server(ServerRoomActivity _ServerRoomActivity, int PORT)
    {
        this._ServerRoomActivity = _ServerRoomActivity;
        this._ServerRoomActivity.GetMessagesBox().setText(this._ServerRoomActivity.GetMessagesBox().getText()+"\nSERVER");
        this.PORT = PORT;
    }
    protected void Log(String Msg)
    {
        _ServerRoomActivity.ShowMessage(Msg);
    }
    private void Fail()
    {
        Log("Server failed.");
    }
    private boolean _Started = false;
    private boolean _Stop = false;
    protected void Stop() { _Stop = true; }
    public void Send(String Msg, int ID)
    {
        while(!_Started);
        Log("SENDING : " + Msg);
        _ServerSenders.get(ID).Send(new Package(Command.MESSAGE, Msg));
        _ServerSenders.get(ID).Flush();
    }
    public void Start()
    {

    }
    protected void Add(ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerListeners.add(_ServerListener);
        _ServerSenders.add(_ServerSender);
        _ServerListenerThreads.add(_ServerListenerThread);
        _ServerSenderThreads.add(_ServerSenderThread);
    }
    public void StartConnector()
    {
        if(_ServerConnector!=null) return;
        _ServerConnector = new ServerConnector(_ServerRoomActivity, this, PORT);
        _ServerConnectorThread = new Thread(_ServerConnector);
        _ServerConnectorThread.start();
        while(!_ServerConnector.Started());
        _ServerRoomActivity.ShowMessage("SERVER CONNECTOR STARTED");
    }
    public void StopConnector()
    {
        if(_ServerConnector==null) return;
        _ServerConnector.Stop();
        while(!_ServerConnector.Stopped());
        _ServerConnector = null;
        _ServerConnectorThread = null;
        _ServerRoomActivity.ShowMessage("SERVER CONNECTOR STOPPED");
    }
}
