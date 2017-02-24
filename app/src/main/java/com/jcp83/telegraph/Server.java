package com.jcp83.telegraph;

import java.util.ArrayList;

public class Server implements Runnable
{
    protected ServerConnector _ServerConnector = null;
    protected Thread _ServerConnectorThread = null;
    private int PORT;
    protected int _ClientsCount = 0;
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
    }
    private void Handle(int ID)
    {
        ServerListener _Listener = _ServerListeners.get(ID);
        ServerSender _Sender = _ServerSenders.get(ID);
        if(!_Listener.HasPackages()) return;
        Package MESSAGE = _Listener.Get();
        if(MESSAGE._Command==Command.MESSAGE)
        {
            String Msg = MESSAGE._Data;
            Log("Client ["+ID+"] : "+Msg);
            Msg="Thanks for message \""+Msg+"\" !";
            Package ANSWER = new Package(Command.MESSAGE, Msg);
            _Sender.Send(ANSWER);
        }
    }
    private void Start()
    {
        Log("SERVER STARTED.");
        while(!_Stop)
            for(int c=0;c<_ClientsCount&&!_Stop;c++)
                Handle(c);
    }
    public void run() { Start(); }
    protected void Add(ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerListeners.add(_ServerListener);
        _ServerSenders.add(_ServerSender);
        _ServerListenerThreads.add(_ServerListenerThread);
        _ServerSenderThreads.add(_ServerSenderThread);
        _ClientsCount++;
    }
    protected void StartConnector()
    {
        if(_ServerConnector!=null) return;
        _ServerConnector = new ServerConnector(_ServerRoomActivity, this, PORT);
        _ServerConnectorThread = new Thread(_ServerConnector);
        _ServerConnectorThread.start();
        while(!_ServerConnector.Started());
        _ServerRoomActivity.ShowMessage("SERVER CONNECTOR STARTED");
    }
    protected void StopConnector()
    {
        if(_ServerConnector==null) return;
        _ServerConnector.Stop();
        while(!_ServerConnector.Stopped());
        _ServerConnector = null;
        _ServerConnectorThread = null;
        _ServerRoomActivity.ShowMessage("SERVER CONNECTOR STOPPED");
    }
    protected void Exit()
    {
        if(_ServerConnector!=null) StopConnector();
        for (ServerListener _Listener:_ServerListeners) _Listener.Stop();
        for (ServerListener _Listener:_ServerListeners) while(!_Listener.IsStopped());
        Log("Server stopped.");
    }
}
