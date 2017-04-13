package com.jcp83.telegraph;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class Client implements Runnable
{
    public static final int SERVER_CHECK_TIME = 3000;
    private ClientConnector _ClientConnector = null;
    private final int PORT;
    private String _Login;
    private UUID _UUID;
    private final ClientRoomActivity _ClientRoomActivity;
    private Timer _ServerCheckTimer = null;
    private TimerTask _ServerCheckTimerTask = null;
    protected ServerInfo Info;
    public Client(ClientRoomActivity _ClientRoomActivity, int PORT, String _Login, UUID _UUID)
    {
        this._ClientRoomActivity = _ClientRoomActivity;
        this.PORT = PORT;
        this._Login = _Login;
        this._UUID = _UUID;
    }
    void Log(String Msg)
    {
        _ClientRoomActivity.ShowMessage(Msg);
    }
    private void Fail()
    {
        Log("\n> CLIENT FAILED.");
    }
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    public boolean Stopped() { return _Stopped; }
    public void Stop()
    {
        _ClientRoomActivity.PushStatus(Status.CLIENT_STOPPING);
        _ServerCheckTimer.cancel();
        _Stop = true;
        _ClientRoomActivity.PopStatus();
        _Stopped = true;
    }
    public void SendText(String Text)
    {
        if(Text.length()>0)
        {
            if(Text.getBytes()[0]=='#') ParseCommand(Text);
            else BindToSystemTask(new Package(Command.MESSAGE, Text, "SERVER"));
        }
    }
    private int _TasksUID=0;
    protected int GetNewTaskUID()
    {
        int UID = _TasksUID;
        _TasksUID++;
        return UID;
    }
    protected boolean _ServerDisconnected = false;
    protected void SendSystemMessage(Package PACKAGE)
    {
        PackageTransmitter _Transmitter = PACKAGE.GetTransmitter(GetNewTaskUID());
        _Transmitter._IsSystem = true;
        Info.Send(_Transmitter);
        if(Info._Disconnected) _ServerDisconnected = true;
    }
    private int _LastUploadedFile = 0;
    private ArrayList<FileDownloader> _FileDownloaders = new ArrayList<>();
    private int _LastFilDownloader = 0;
    protected void HandleTaskTransmitter(PackageTransmitter _Transmitter)
    {

    }
    protected boolean HandleSystemMessage(PackageTransmitter _Transmitter)
    {
        Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
        Command _Command = PACKAGE.GetCommand();
        switch(_Command)
        {
            case EXIT: Log("\n> ROOM CLOSED."); Stop(); break;
            case INFO_LOGIN: Log("\n> "+PACKAGE.GetData()+" JOINED ROOM."); break;
            case INFO_LOGOUT: Log("\n> "+PACKAGE.GetData()+" LEAVED ROOM."); break;
            case INFO_FILE:
                Log("\nNEW FILE ADDED : '"+PACKAGE.GetData()+"' ("+_LastUploadedFile+").");
                _LastUploadedFile++;
                break;
            case TASK_FILE:
                PackageTask _Task = new PackageTask(Integer.parseInt(PACKAGE.GetData().toString()));
                FileDownloader _Downloader = new FileDownloader("storage/emulated/0/TEMPO/FILE"+Math.abs(new Random().nextInt()%1000000), _Task, _ClientRoomActivity);
                _Downloader._Thread = new Thread(_Downloader);
                _Downloader._Thread.start();
                Info._TasksPopStack.add(_Task);
                break;
            default: break;
        }
        return false;
    }
    protected void HandleSystemTaskTransmitter(PackageTransmitter _Transmitter)
    {
        Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
        switch (PACKAGE.GetCommand())
        {
            case MESSAGE: Log("\n"+PACKAGE.GetSender()+" : "+PACKAGE.GetData().toString()); break;
            default: break;
        }
    }
    protected void BindTask(PackageTask _Task)
    {
        Info._TasksPushStack.add(_Task);
    }
    protected void BindToSystemTask(Package PACKAGE)
    {
        PackageTransmitter _Transmitter = PACKAGE.GetTransmitter(0);
        _Transmitter._IsSystemTask = true;
        Info._TasksPushStack.get(0).Add(_Transmitter);
    }
    private void TT()
    {
        int UID = GetNewTaskUID();
        PackageTask _Task = new PackageTask(GetNewTaskUID());
        for(int c=0;c<10;c++)
        {
            PackageTransmitter Transmitter = new PackageTransmitter(UID, c);
            Transmitter.SetData(Package._GetBytes(new Package(Command.MESSAGE, String.valueOf(c), _Login)));
            _Task.Add(Transmitter);
        }
        BindTask(_Task);
    }
    private void ParseCommand(String Msg)
    {
        int SP=0;for(;SP<Msg.length();SP++) if(Msg.charAt(SP)==' ') break;
        if(SP==0||SP==Msg.length()-1) return;
        String Cmd = Msg.substring(1, SP);
        String Param = ""; if(SP<Msg.length()-1) Param=Msg.substring(SP+1);
        switch(Cmd)
        {
            case "start": Start(); break;
            case "stop": Stop(); break;
            case "tt": TT(); break;
            case "d":
                if(Param.isEmpty()) Log("\nERROR : Must be selected file index !");
                else
                {
                    int _Index = -1;
                    try { _Index = Integer.valueOf(Param); }
                    catch (Exception e) { Log("\nERROR : Invalid index : '"+Param+"' !"); }
                    if(_Index>=0)
                    {
                        if(_Index>=_LastUploadedFile)
                            Log("\nERROR : File "+_Index+" not exist.");
                        else
                        {
                            SendSystemMessage(new Package(Command.DOWNLOAD_FILE, Param, _Login));
                        }
                    }
                }
                break;
            default: Log("\nERROR : No such command : '"+Cmd+"'"); break;
        }
    }
    private void Start()
    {
        _ClientRoomActivity.PushStatus(Status.CLIENT_STARTING);
        _ServerCheckTimer = new Timer();
        _ServerCheckTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                SendSystemMessage(new Package(Command.CHECK, "", _Login));
            }
        };
        StartConnector();
        if(!_ClientConnector.Success()) return;
        final String _Password = "#AveJava#";
        Package P_LOGIN = new Package(Command.LOGIN, _Password, _Login);
        SendSystemMessage(P_LOGIN);
        Package P_LOGIN_UUID = new Package(Command.UUID, _UUID, _Login);
        SendSystemMessage(P_LOGIN_UUID);
        while(Info._Listener._Stack.isEmpty());
        Package P_LOGIN_RESULT = (Package)Package._GetObject(Info._Listener.Get().GetData());
        if(P_LOGIN_RESULT == null) { Fail(); return; }
        _ClientRoomActivity.PopStatus();
        if(P_LOGIN_RESULT.GetCommand()==Command.LOGIN_SUCCESS)
        {
            Log("\n> LOGIN SUCCESS.");
            _ServerCheckTimer.schedule(_ServerCheckTimerTask, SERVER_CHECK_TIME, SERVER_CHECK_TIME);
            _Started = true;
            while(!_Stop)
            {
                Info.Receive();
                if(Info._Disconnected) Stop();
                if(!Info._TasksPushStack.isEmpty()||!Info._TasksPopStack.isEmpty()) Info.HandleTasks();
            }
            if(!_ServerDisconnected) SendSystemMessage(new Package(Command.EXIT,"",_Login));
            else
            {
                if (Info._Disconnected) Log("\nSERVER DISCONNECTED.");
            }
        }
        else
        {
            Log("\n> LOGIN FAILED.");
        }
    }
    public void run() { Start(); }
    private void StartConnector()
    {
        _ClientRoomActivity.PushStatus(Status.CLIENT_CONNECTOR_STARTING);
        if(_ClientConnector!=null) return;
        _ClientConnector = new ClientConnector(_ClientRoomActivity, this, PORT);
        Thread _ClientConnectorThread = new Thread(_ClientConnector);
        _ClientConnectorThread.start();
        while(!_ClientConnector.Started()&&_ClientConnector.Success());
        if(!_ClientConnector.Success()) { Fail(); return; }
        _ClientRoomActivity.PopStatus();
    }
}
