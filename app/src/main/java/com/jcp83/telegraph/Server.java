package com.jcp83.telegraph;

import java.util.ArrayList;

class Server implements Runnable
{
    private ServerConnector _ServerConnector = null;
    private Thread _ServerConnectorThread = null;
    private final int PORT;
    private int _ClientsCount = 0;
    private final ArrayList<ServerListener> _ServerListeners = new ArrayList<>();
    private final ArrayList<ServerSender> _ServerSenders = new ArrayList<>();
    private final ArrayList<Thread> _ServerListenerThreads = new ArrayList<>();
    private final ArrayList<Thread> _ServerSenderThreads = new ArrayList<>();
    private final ServerRoomActivity _ServerRoomActivity;
    Server(ServerRoomActivity _ServerRoomActivity, int PORT)
    {
        this._ServerRoomActivity = _ServerRoomActivity;
        this._ServerRoomActivity.GetMessagesBox().append("\nSERVER");
        this.PORT = PORT;
    }
    void Log(String Msg)
    {
        _ServerRoomActivity.ShowMessage(Msg);
    }
    private void Fail()
    {
        Log("Server failed.");
    }
    private boolean _Started = false;
    public boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
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
        if(_Listener._Stack.isEmpty()) return;
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
        if(_Started) return;
        _ServerRoomActivity.PushStatus(Status.SERVER_STARTING);
        _Started = true;
        Log("SERVER STARTED.");
        _ServerRoomActivity.PopStatus();
        while(!_Stop)
            for(int c=0;c<_ClientsCount&&!_Stop;c++)
                Handle(c);
    }
    public void run() { Start(); }
    void Add(ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerListeners.add(_ServerListener);
        _ServerSenders.add(_ServerSender);
        _ServerListenerThreads.add(_ServerListenerThread);
        _ServerSenderThreads.add(_ServerSenderThread);
        _ClientsCount++;
    }
    void StartConnector()
    {
        if(_ServerConnector!=null) return;
        _ServerRoomActivity.PushStatus(Status.SERVER_CONNECTOR_STARTING);
        _ServerConnector = new ServerConnector(_ServerRoomActivity, this, PORT);
        _ServerConnectorThread = new Thread(_ServerConnector);
        _ServerConnectorThread.start();
        while(!_ServerConnector.Started());
        _ServerRoomActivity.ShowMessage("SERVER CONNECTOR STARTED");
        _ServerRoomActivity.PopStatus();
    }
    void StopConnector()
    {
        if(_ServerConnector==null) return;
        _ServerRoomActivity.PushStatus(Status.SERVER_CONNECTOR_STOPPING);
        _ServerConnector.Stop();
        while(!_ServerConnector.Stopped());
        _ServerConnector = null;
        _ServerConnectorThread = null;
        _ServerRoomActivity.ShowMessage("SERVER CONNECTOR STOPPED");
        _ServerRoomActivity.PopStatus();
    }
    void Stop()
    {
        _ServerRoomActivity.PushStatus(Status.SERVER_STOPPING);
        _Stop = true;
        if(_ServerConnector!=null) StopConnector();
        for (ServerListener _Listener:_ServerListeners) _Listener.Stop();
        for (ServerListener _Listener:_ServerListeners) while(!_Listener.IsStopped());
        Log("SERVER STOPPED.");
        _ServerRoomActivity.PopStatus();
        _Stopped = true;
    }
}
