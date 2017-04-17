package com.jcp83.telegraph;

import android.content.Intent;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class Server implements Runnable
{
    public static final int CLIENT_CHECK_TIME = 1000;
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
    void Log(Message Msg)
    {
        _ServerRoomActivity.ShowMessage(Msg, Color.RED);
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
    private ArrayList<String> _UploadedFiles = new ArrayList<>();
    private String GetTime()
    {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    private void ShowMessage(String _Sender, String _Text)
    {
        _ServerRoomActivity.ShowMessage(new Message(_Sender, _Text, GetTime()), Color.BLACK);
    }
    protected void Log(String _Text)
    {
        Log(new Message("", _Text, GetTime()));
    }
    protected boolean HandleSystemMessage(PackageTransmitter _Transmitter, UUID _UUID)
    {
        Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
        Command _Command = PACKAGE.GetCommand();
        switch(_Command)
        {
            case EXIT:
                DisconnectClient(_UUID);
                return true;
            case DOWNLOAD_FILE:
                Log("CLIENT "+PACKAGE.GetSender()+" ("+_UUID.toString()+") ASKING FOR DOWNLOAD FILE "+PACKAGE.GetData());
                for(int c=0;c<_ClientInfos.size();c++)
                {
                    ClientInfo Info = _ClientInfos.get(c);
                    if(Info.GetUUID()==_UUID)
                    {
                        int _UID = GetNewTaskUID();
                        PackageTask Task = new PackageTask(_UID);
                        Info._TasksPushStack.add(Task);
                        SendSystemMessage(Info, new Package(Command.TASK_FILE, _UID, "SERVER"));
                        int _Index = Integer.valueOf(PACKAGE.GetData().toString());
                        FileUploader _Uploader = new FileUploader(Task, _UploadedFiles.get(_Index), "SERVER");
                        _Uploader._Thread = new Thread(_Uploader);
                        _Uploader._Thread.start();
                        break;
                    }
                }
                break;
            default: break;
        }
        return false;
    }
    protected void HandleSystemTaskTransmitter(PackageTransmitter _Transmitter, UUID _UUID)
    {
        Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
        switch (PACKAGE.GetCommand())
        {
            case MESSAGE:
                String Msg = PACKAGE.GetData().toString();
                ShowMessage(PACKAGE.GetSender(), PACKAGE.GetData().toString());
                BindToAllSystemTasks(PACKAGE);
                break;
            default: break;
        }
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
    protected void BindToAllSystemTasks(Package PACKAGE)
    {
        for(int c=0;c<_ClientInfos.size();c++)
        {
            ClientInfo _Info = _ClientInfos.get(c);
            PackageTransmitter _Transmitter = PACKAGE.GetTransmitter(0);
            _Transmitter._IsSystemTask = true;
            _Info._TasksPushStack.get(0).Add(_Transmitter);
        }
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
    private void F()
    {
        _ServerRoomActivity.F();
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
            case "f": F(); break;
            case "tt": TT(); break;
            default: Log("ERROR: NO SUCH COMMAND : '"+Cmd+"'"); break;
        }
    }
    public void SendText(String Text)
    {
        if(Text.length()>0)
        {
            if(Text.getBytes()[0]=='#') ParseCommand(Text);
            else BindToAllSystemTasks(new Package(Command.MESSAGE, Text, "SERVER"));
            ShowMessage("SERVER", Text);
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
            ClientInfo _Info = _ClientInfos.get(c);
            _Info.Send(_Transmitter);
            if(_Info._Disconnected) DisconnectClient(_Info.GetUUID());
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
                while (!Info._Listener.IsStopped());
                Info._TasksPopStack.clear();
                Info._TasksPushStack.clear();
                String Name = Info.GetName();
                boolean _Disconnected = Info._Disconnected;
                _ClientInfos.remove(c);
                if (!_Disconnected)
                {
                    Log(Name + " LEAVED ROOM.");
                    SendSystemMessageToAll(new Package(Command.INFO_LOGOUT, Name, "SERVER"));
                }
                else
                {
                    Log(Name + " DISCONNECTED.");
                    SendSystemMessageToAll(new Package(Command.INFO_DISCONNECT, Name, "SERVER"));
                }
                break;
            }
        }
    }
    private boolean _Checking = false;
    private void CheckClients()
    {
        if(!_Started||_Checking) return;
        _Checking = true;
        SendSystemMessageToAll(new Package(Command.CHECK, "", "SERVER"));
        _Checking = false;
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
            for(int c=0;c<_ClientInfos.size()&&!_Stop;c++)
            {
                if(!_Checking)
                {
                    ClientInfo _Info = _ClientInfos.get(c);
                    _Info.Receive();
                    _Info.HandleTasks();
                    if (_Info._Disconnected) c--;
                }
            }
    }
    protected void UploadFile(String _Path)
    {
        _UploadedFiles.add(_Path);
        String _FileName = _Path.substring(_Path.lastIndexOf('/')+1);
        SendSystemMessageToAll(new Package(Command.INFO_FILE, _FileName, "SERVER"));
        Log("ADDED FILE '"+_Path+"'.");
    }
    public void run() { Start(); }
    void AddClient(String _Name, UUID _UUID, ServerSender _ServerSender, ServerListener _ServerListener, Thread _ServerSenderThread, Thread _ServerListenerThread)
    {
        _ServerSender._UUID = _UUID;
        Package P_INFO_LOGIN = new Package(Command.INFO_LOGIN, _Name, "SERVER");
        SendSystemMessageToAll(P_INFO_LOGIN);
        Log(_Name+" JOINED ROOM.");
        _ServerListener._UUID = _UUID;
        _ServerListener._Thread = _ServerListenerThread;
        _ServerSender._UUID = _UUID;
        _ServerSender._Thread = _ServerSenderThread;
        ClientInfo _Info = new ClientInfo(this, _ServerListener, _ServerSender, _Name, _UUID);
        _Info._TasksPushStack.add(new PackageTask(GetNewTaskUID()));
        _Info._TasksPopStack.add(new PackageTask(GetNewTaskUID()));
        _ClientInfos.add(_Info);
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
