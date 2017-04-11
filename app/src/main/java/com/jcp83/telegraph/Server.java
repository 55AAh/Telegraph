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
    private int _TasksUID=0;
    private int GetNewTaskUID()
    {
        int UID = _TasksUID;
        _TasksUID++;
        return UID;
    }
    protected boolean HandleSystemMessage(PackageTransmitter _Transmitter, UUID _UUID)
    {
        Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
        Command _Command = PACKAGE.GetCommand();
        switch(_Command)
        {
            case MESSAGE:
                String Msg = (String)PACKAGE.GetData();
                Log("\n"+PACKAGE.GetSender()+" : "+Msg);
                PackageTask _Task = new PackageTask(GetNewTaskUID());
                _Task.Add(PACKAGE.GetTransmitter(GetNewTaskUID()));
                BindTaskToAll(_Task);
                break;
            case EXIT:
                DisconnectClient(_UUID);
                return true;
            default: break;
        }
        return false;
    }
    protected void HandleTransmitter(PackageTransmitter _Transmitter, UUID _UUID)
    {
        Log("\nTRANSMITTER FROM "+_UUID.toString());
    }
    protected void BindTask(PackageTask _Task, UUID _UUID)
    {
        for(int c=0;c<_ClientInfos.size();c++)
        {
            ClientInfo _Info = _ClientInfos.get(c);
            if(_Info.GetUUID()==_UUID)
            {
                _Info._TasksPushStack.add(_Task);
                break;
            }
        }
    }
    protected void BindTaskToAll(PackageTask _Task)
    {
        for(int c=0;c<_ClientInfos.size();c++)
            _ClientInfos.get(c)._TasksPushStack.add(_Task);
    }
    private void TT()
    {
        int UID = GetNewTaskUID();
        PackageTask _Task = new PackageTask(GetNewTaskUID());
        for(int c=0;c<10;c++)
        {
            PackageTransmitter Transmitter = new PackageTransmitter(UID, c);
            Transmitter.SetData(Package._GetBytes(new Package(Command.MESSAGE, String.valueOf(c), "SERVER")));
            _Task.Add(Transmitter);
        }
        BindTaskToAll(_Task);
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
            case "tt": TT(); break;
            default: Log("\nNo such command : '"+Cmd+"'"); break;
        }
    }
    public void SendMessage(String Msg)
    {
        if(Msg.length()>0)
        {
            if(Msg.getBytes()[0]=='#') ParseCommand(Msg);
            else
            {
                PackageTask _Task = new PackageTask(GetNewTaskUID());
                _Task.Add(new Package(Command.MESSAGE, Msg, "SERVER").GetTransmitter(GetNewTaskUID()));
                BindTaskToAll(_Task);
            }
            Log("\nSERVER : "+Msg);
        }
    }
    protected void SendSystemMessage(ClientInfo _Info, Package PACKAGE)
    {
        PackageTransmitter _Transmitter = PACKAGE.GetTransmitter(GetNewTaskUID());
        _Transmitter._IsSystem = true;
        _Info.Send(_Transmitter);
    }
    protected void SendSystemMessageToAll(Package PACKAGE)
    {
        for(int c=0;c<_ClientInfos.size();c++)
        {
            PackageTransmitter _Transmitter = PACKAGE.GetTransmitter(GetNewTaskUID());
            _Transmitter._IsSystem = true;
            _ClientInfos.get(c).Send(_Transmitter);
        }
    }
    private ArrayList<ClientInfo> _ClientInfos = new ArrayList<>();
    protected void DisconnectClient(UUID _UUID)
    {
        for(int c=0;c<_ClientInfos.size();c++)
        {
            ClientInfo Info = _ClientInfos.get(c);
            if(Info.GetUUID() == _UUID)
            {
                Info._Listener.Stop();
                while (!Info._Listener.IsStopped()) ;
                Info._Listener._Thread = null;
                Info._Sender._Thread = null;
                String Name = Info.GetName();
                boolean _Disconnected = Info._Disconnected;
                _ClientInfos.remove(c);
                if (_Disconnected)
                {
                    Log("\n> " + Name + " LEAVED ROOM.");
                    SendSystemMessageToAll(new Package(Command.INFO_LOGOUT, Name, "SERVER"));
                }
                else
                {
                    Log("\n> " + Name + " DISCONNECTED.");
                    SendSystemMessageToAll(new Package(Command.INFO_DISCONNECT, Name, "SERVER"));
                }
                break;
            }
        }
    }
    private void CheckClients()
    {
        if(!_Started) return;
        SendSystemMessageToAll(new Package(Command.CHECK, "", "SERVER"));
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
                //CheckClients();
            }
        };
        _ClientsCheckTimer = new Timer();
        _ClientsCheckTimer.schedule(_ClientsCheckTimerTask, CLIENT_CHECK_TIME, CLIENT_CHECK_TIME);
        _Started = true;
        _ServerRoomActivity.PopStatus();
        _ServerRoomActivity.SetRoomNameToStatus();
        while(!_Stop)
            for(int c=0;c<_ClientInfos.size()&&!_Stop;c++)
            {
                ClientInfo _Info = _ClientInfos.get(c);
                _Info.Receive();
                _Info.HandleTasks();
                if(_Info._Disconnected) c--;
            }
    }
    public void run() { Start(); }
    void AddClient(String _Name, UUID _UUID, ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerSender._UUID = _UUID;
        Package P_INFO_LOGIN = new Package(Command.INFO_LOGIN, _Name, "SERVER");
        SendSystemMessageToAll(P_INFO_LOGIN);
        Log("\n> "+_Name+" JOINED ROOM.");
        _ServerListener._UUID = _UUID;
        _ServerListener._Thread = _ServerListenerThread;
        _ServerSender._UUID = _UUID;
        _ServerSender._Thread = _ServerSenderThread;
        _ClientInfos.add(new ClientInfo(this, _ServerListener, _ServerSender, _Name, _UUID));
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
        SendSystemMessageToAll(new Package(Command.EXIT,"","SERVER"));
        for(int c=0;c<_ClientInfos.size();c++)
            DisconnectClient(_ClientInfos.get(c).GetUUID());
        _ServerRoomActivity.PopStatus();
        _ClientsCheckTimer.cancel();
        _Stopped = true;
    }
}
