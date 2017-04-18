package com.jcp83.telegraph;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class Client implements Runnable
{
    public static final int SERVER_CHECK_TIME = 3000;
    private ClientConnector _ClientConnector = null;
    private final int PORT;
    protected String _Login;
    protected UUID _UUID;
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
    private String GetTime()
    {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    void Log(String _Text)
    {
        Log(new Message("", _Text, GetTime()));
    }
    void Log(Message Msg)
    {
        _ClientRoomActivity.ShowMessage(Msg, Color.RED);
    }
    private void ShowMessage(String _Sender, String _Text)
    {
        _ClientRoomActivity.ShowMessage(new Message(_Sender, _Text, GetTime()), Color.BLACK);
    }
    private void Fail()
    {
        Log("CLIENT FAILED.");
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
            else BindToSystemTask(new Package(Command.MESSAGE, Text, _Login));
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
    private ArrayList<String> _Files = new ArrayList<>();
    private ArrayList<FileDownloader> _FileDownloaders = new ArrayList<>();
    protected boolean HandleSystemMessage(PackageTransmitter _Transmitter)
    {
        Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
        Command _Command = PACKAGE.GetCommand();
        switch(_Command)
        {
            case EXIT: Log("ROOM CLOSED."); Stop(); break;
            case INFO_LOGIN: Log(PACKAGE.GetData()+" JOINED ROOM."); break;
            case INFO_LOGOUT: Log(PACKAGE.GetData()+" LEFT ROOM."); break;
            case INFO_FILE:
                Log("NEW FILE ADDED : '"+PACKAGE.GetData()+"' ("+_LastUploadedFile+").");
                _ClientRoomActivity._FileNames.add(PACKAGE.GetData().toString());
                _Files.add(PACKAGE.GetData().toString());
                _LastUploadedFile++;
                break;
            case TASK_FILE:
                PackageTask _Task = new PackageTask(Integer.parseInt(PACKAGE.GetData().toString()));
                String _Path = _ClientRoomActivity._Settings.GetDownloadDir()+"/"+PACKAGE.GetSender();
                FileDownloader _Downloader = new FileDownloader(_Path, _Task, _ClientRoomActivity);
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
            case MESSAGE: ShowMessage(PACKAGE.GetSender(), PACKAGE.GetData().toString()); break;
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
    private void F() { _ClientRoomActivity.F(); }
    private ArrayList<String> _UploadedFiles = new ArrayList<>();
    protected void UploadFile(String _Path)
    {
        _UploadedFiles.add(_Path);
        String _FileName = _Path.substring(_Path.lastIndexOf('/')+1);
        int _UID = GetNewTaskUID();
        PackageTask Task = new PackageTask(_UID);
        Info._TasksPushStack.add(Task);
        SendSystemMessage(new Package(Command.TASK_FILE, _UID, _FileName));
        FileUploader _Uploader = new FileUploader(Task, _Path, "SERVER");
        _Uploader._Thread = new Thread(_Uploader);
        _Uploader._Thread.start();
        Log("UPLOADING FILE '"+_Path+"' ...");
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
            case "f": F(); break;
            case "d":
                if(Param.isEmpty()) Log("ERROR : Must be selected file index !");
                else
                {
                    int _Index = -1;
                    try { _Index = Integer.valueOf(Param); }
                    catch (Exception e) { Log("ERROR : Invalid index : '"+Param+"' !"); }
                    if(_Index>=0)
                    {
                        if(_Index>=_LastUploadedFile)
                            Log("ERROR : File "+_Index+" not exist.");
                        else
                        {
                            SendSystemMessage(new Package(Command.DOWNLOAD_FILE, Param, _Login));
                        }
                    }
                }
                break;
            default: Log("ERROR: NO SUCH COMMAND : '"+Cmd+"'"); break;
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
            Log("LOGIN SUCCESS.");
            _ServerCheckTimer.schedule(_ServerCheckTimerTask, SERVER_CHECK_TIME, SERVER_CHECK_TIME);
            _Started = true;
            while(!_Stop)
            {
                Info.Receive();
                if(Info._Disconnected) Stop();
                if(!Info._TasksPushStack.isEmpty()||!Info._TasksPopStack.isEmpty()) Info.HandleTasks();
                if(!_ClientRoomActivity._DownloadedFilesNotifyList.isEmpty())
                {
                    Log("FILE '"+_ClientRoomActivity._DownloadedFilesNotifyList.get(0)+"' DOWNLOADED");
                    _ClientRoomActivity._DownloadedFilesNotifyList.remove(0);
                }
            }
            if(!_ServerDisconnected) SendSystemMessage(new Package(Command.EXIT,"",_Login));
            else
            {
                if (Info._Disconnected) Log("SERVER DISCONNECTED.");
            }
        }
        else
        {
            Log("LOGIN FAILED.");
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
