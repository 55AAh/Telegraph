package com.jcp83.telegraph;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class Server implements Runnable
{
    public static final int CLIENT_CHECK_TIME = 3000;
    private ServerConnector _ServerConnector = null;
    private Thread _ServerConnectorThread = null;
    private Timer _ClientsCheckTimer = null;
    private TimerTask _ClientsCheckTimerTask = null;
    private final int PORT;
    private int _ClientsCount = 0;
    private final ArrayList<ServerListener> _ServerListeners = new ArrayList<>();
    private final ArrayList<ServerSender> _ServerSenders = new ArrayList<>();
    private final ArrayList<Thread> _ServerListenerThreads = new ArrayList<>();
    private final ArrayList<Thread> _ServerSenderThreads = new ArrayList<>();
    private final ServerRoomActivity _ServerRoomActivity;
    protected String _RoomName;
    protected String GetRoomName() { return _RoomName; }
    Server(ServerRoomActivity _ServerRoomActivity, int PORT, String _RoomName)
    {
        this._RoomName = _RoomName;
        this._ServerRoomActivity = _ServerRoomActivity;
        this.PORT = PORT;
    }
    void Log(String Msg)
    {
        _ServerRoomActivity.ShowMessage(Msg);
    }
    private boolean _Started = false;
    public boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    public void Send(Package P, int ID)
    {
        while(!_Started);
        _ServerSenders.get(ID).Send(P);
    }
    public void SendAll(Package P)
    {
        for(int ID=0;ID<_ServerSenders.size();ID++) Send(P,ID);
    }
    public void SendMessage(String Msg)
    {
        SendAll(new Package(Command.MESSAGE,Msg,"SERVER"));
        Log("\nSERVER : "+Msg);
    }
    class ClientInfo
    {
        private String _Name;
        public ClientInfo(String _Name)
        {
            this._Name = _Name;
        }
        public String GetName() { return _Name; }
    }
    private ArrayList<ClientInfo> _ClientInfos = new ArrayList<>();
    protected void DisconnectClient(int ID, boolean Disconnected)
    {
        String Name = _ClientInfos.get(ID).GetName();
        _ClientInfos.remove(ID);
        _ServerListeners.get(ID).Stop();
        _ServerListeners.remove(ID);
        _ServerSenders.remove(ID);
        _ServerListenerThreads.remove(ID);
        _ServerSenderThreads.remove(ID);
        _ClientsCount--;
        if(!Disconnected)
        {
            Log("\n> " + Name + " LEAVED ROOM.");
            SendAll(new Package(Command.INFO_LOGOUT,Name,"SERVER"));
        }
        else
        {
            Log("\n> " + Name + " DISCONNECTED.");
            SendAll(new Package(Command.INFO_DISCONNECT,Name,"SERVER"));
        }
    }
    private boolean Handle(int ID)
    {
        ServerListener _Listener = _ServerListeners.get(ID);
        if(_Listener._Stack.isEmpty()) return true;
        Package PACKAGE = _Listener.Get();
        Command _Command = PACKAGE.GetCommand();
        switch(_Command)
        {
            case MESSAGE:
                String Msg = (String)PACKAGE.GetData();
                Log("\n"+PACKAGE.GetSender()+" : "+Msg);
                SendAll(PACKAGE);
                break;
            case EXIT:
                DisconnectClient(ID, false);
                break;
            default: break;
        }
        return true;
    }
    private void CheckClients()
    {
        if(!_Started) return;
        for(ServerSender _Sender: _ServerSenders)
            _Sender.Send(new Package(Command.CHECK, "", "SERVER"));
    }
    private void Start()
    {
        if(_Started) return;
        _ClientInfos.clear();
        _ServerRoomActivity.PushStatus(Status.SERVER_STARTING);
        _ClientsCheckTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                CheckClients();
            }
        };
        _ClientsCheckTimer = new Timer();
        _ClientsCheckTimer.schedule(_ClientsCheckTimerTask, CLIENT_CHECK_TIME, CLIENT_CHECK_TIME);
        _Started = true;
        _ServerRoomActivity.PopStatus();
        _ServerRoomActivity.SetRoomNameToStatus();
        while(!_Stop)
            for(int c=0;c<_ClientsCount&&!_Stop;c++)
                if(!Handle(c)) c--;
    }
    public void run() { Start(); }
    void AddClient(String _Name, ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerSender.ID = _ClientsCount;
        ClientInfo _Info = new ClientInfo(_Name);
        Package INFO_LOGIN_P = new Package(Command.INFO_LOGIN, _Name, "SERVER");
        SendAll(INFO_LOGIN_P);
        _ClientInfos.add(_Info);
        Log("\n> "+_Name+" JOINED ROOM.");
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
        _ServerRoomActivity.PopStatus();
    }
    void Stop()
    {
        _ServerRoomActivity.PushStatus(Status.SERVER_STOPPING);
        _Stop = true;
        if(_ServerConnector!=null) StopConnector();
        for (ServerSender _Sender:_ServerSenders) _Sender.Send(new Package(Command.EXIT,"","SERVER"));
        for (ServerListener _Listener:_ServerListeners) _Listener.Stop();
        for (ServerListener _Listener:_ServerListeners) while(!_Listener.IsStopped());
        _ServerRoomActivity.PopStatus();
        _ClientsCheckTimer.cancel();
        _Stopped = true;
    }
}
