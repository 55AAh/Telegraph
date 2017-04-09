package com.jcp83.telegraph;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
    private ArrayList<PackageTask> _TasksPushStack = new ArrayList<>();
    private ArrayList<PackageTask> _TasksPopStack = new ArrayList<>();
    private int _LastPushTask=0;
    private int _LastPopTask=0;
    private int _TasksUID=0;
    private int GetNewTaskUID()
    {
        int UID = _TasksUID;
        _TasksUID++;
        return UID;
    }
    public void Send(PackageTransmitter PT, int ID)
    {
        while(!_Started);
        _ServerSenders.get(ID).Send(PT);
    }
    public void SendAll(PackageTransmitter PT)
    {
        for(int ID=0;ID<_ServerSenders.size();ID++) Send(PT,ID);
    }
    private void TTS()
    {
        int UID = GetNewTaskUID();
        PackageTask Task = new PackageTask();
        for(int c=0;c<10;c++)
        {
            PackageTransmitter Transmitter = new PackageTransmitter(UID, c);
            Transmitter.SetData(Package._GetBytes(new Package(Command.MESSAGE, String.valueOf(c), "SERVER")));
            Transmitter._IsSingle = true;
            Task.Add(Transmitter);
        }
        _TasksPushStack.add(Task);
    }
    private void ParseCommand(String Msg)
    {
        int SP=0;for(;SP<Msg.length();SP++) if(Msg.charAt(SP)==' ') break;
        if(SP==0||SP==Msg.length()-1) return;
        String Cmd = Msg.substring(1, SP);
        String Param = ""; if(SP<Msg.length()-1) Msg.substring(SP+1);
        switch(Cmd)
        {
            case "start": Start(); break;
            case "stop": Stop(); break;
            case "tts": TTS(); break;
        }
    }
    public void SendMessage(String Msg)
    {
        if(Msg.length()>0)
        {
            if(Msg.getBytes()[0]=='#') ParseCommand(Msg);
            else SendAll(new Package(Command.MESSAGE, Msg, "SERVER").GetSingleTransmitter(GetNewTaskUID()));
            Log("\nSERVER : "+Msg);
        }
    }
    private ArrayList<ClientInfo> _ClientInfos = new ArrayList<>();
    protected void DisconnectClient(UUID _UUID, boolean Disconnected)
    {
        for(int c=0;c<_ClientInfos.size();c++)
        {
            ClientInfo Info = _ClientInfos.get(c);
            if(Info.GetUUID() == _UUID)
            {
                _ClientInfos.remove(c);
                for (int sc = 0; sc < _ServerListeners.size(); sc++)
                {
                    ServerListener Listener = _ServerListeners.get(sc);
                    if (Listener._UUID == _UUID)
                    {
                        Listener.Stop();
                        while (!Listener.IsStopped()) ;
                        Listener._Thread = null;
                        _ServerListeners.remove(sc);
                        break;
                    }
                }
                for (int sc = 0; sc < _ServerSenders.size(); sc++)
                {
                    ServerSender Sender = _ServerSenders.get(sc);
                    if (Sender._UUID == _UUID)
                    {
                        Sender._Thread = null;
                        _ServerSenders.remove(sc);
                        break;
                    }
                }
                _ClientInfos.remove(c);
                _ClientsCount--;
                String Name = Info.GetName();
                if (!Disconnected)
                {
                    Log("\n> " + Name + " LEAVED ROOM.");
                    SendAll(new Package(Command.INFO_LOGOUT, Name, "SERVER").GetSingleTransmitter(GetNewTaskUID()));
                }
                else
                {
                    Log("\n> " + Name + " DISCONNECTED.");
                    SendAll(new Package(Command.INFO_DISCONNECT, Name, "SERVER").GetSingleTransmitter(GetNewTaskUID()));
                }
                break;
            }
        }
    }
    private boolean HandleClient(UUID _UUID)
    {
        for (int c = 0; c < _ServerListeners.size(); c++)
        {
            ServerListener Listener = _ServerListeners.get(c);
            if(Listener._UUID == _UUID)
            {
                if(Listener._Stack.isEmpty()) return true;
                PackageTransmitter PT = Listener.Get();
                if(!PT._IsSingle) return true;
                Package PACKAGE = (Package)Package._GetObject(PT.GetData());
                Command _Command = PACKAGE.GetCommand();
                switch(_Command)
                {
                    case MESSAGE:
                        String Msg = (String)PACKAGE.GetData();
                        Log("\n"+PACKAGE.GetSender()+" : "+Msg);
                        SendAll(PACKAGE.GetSingleTransmitter(GetNewTaskUID()));
                        break;
                    case EXIT:
                        DisconnectClient(_UUID, false);
                        return false;
                    default: break;
                }
            }
        }
        return true;
    }
    private void HandlePushTasks()
    {
        if(_LastPushTask>=_TasksPushStack.size()) _LastPushTask=0;
        PackageTask Task = _TasksPushStack.get(_LastPushTask);
        if(Task.IsCompleted())
        {
            _TasksPushStack.remove(_LastPushTask);
            return;
        }
        PackageTransmitter Transmitter = Task.Handle();
        if(Task.IsToAll()) SendAll(Transmitter); else Send(Transmitter, Task.GetReceiverID());
    }
    public void HandlePopTasks()
    {
        if(_LastPopTask>=_TasksPopStack.size()) _LastPopTask=0;
        PackageTask Task = _TasksPopStack.get(_LastPopTask);
        if(Task.IsCompleted())
        {
            _TasksPopStack.remove(_LastPopTask);
            return;
        }
        PackageTransmitter Transmitter = Task.Handle();
    }
    private void CheckClients()
    {
        if(!_Started) return;
        for(ServerSender _Sender: _ServerSenders)
            _Sender.Send(new Package(Command.CHECK, "", "SERVER").GetSingleTransmitter(GetNewTaskUID()));
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
            {
                if(!HandleClient(_ClientInfos.get(c).GetUUID())) c--;
                HandlePushTasks();
                HandlePopTasks();
            }
    }
    public void run() { Start(); }
    void AddClient(String _Name, UUID _UUID, ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerSender._UUID = _UUID;
        ClientInfo _Info = new ClientInfo(_Name, _UUID);
        Package P_INFO_LOGIN = new Package(Command.INFO_LOGIN, _Name, "SERVER");
        SendAll(P_INFO_LOGIN.GetSingleTransmitter(GetNewTaskUID()));
        _ClientInfos.add(_Info);
        Log("\n> "+_Name+" JOINED ROOM.");
        _ServerListener._Thread = _ServerListenerThread;
        _ServerListeners.add(_ServerListener);
        _ServerSender._Thread = _ServerSenderThread;
        _ServerSenders.add(_ServerSender);
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
        for (ServerSender _Sender:_ServerSenders) _Sender.Send(new Package(Command.EXIT,"","SERVER").GetSingleTransmitter(GetNewTaskUID()));
        for (ServerListener _Listener:_ServerListeners) _Listener.Stop();
        for (ServerListener _Listener:_ServerListeners) while(!_Listener.IsStopped());
        _ServerRoomActivity.PopStatus();
        _ClientsCheckTimer.cancel();
        _Stopped = true;
    }
}
