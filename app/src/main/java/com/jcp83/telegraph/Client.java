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
    ClientListener _ClientListener;
    ClientSender _ClientSender;
    Thread _ClientListenerThread;
    Thread _ClientSenderThread;
    private final ClientRoomActivity _ClientRoomActivity;
    private Timer _ServerCheckTimer = null;
    private TimerTask _ServerCheckTimerTask = null;
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
    private final ArrayList<String> Messages = new ArrayList<>();
    private boolean HasMessages() { return !Messages.isEmpty(); }
    private String GetMessage()
    {
        if(!HasMessages()) return null;
        String Msg = Messages.get(0);
        Messages.remove(0);
        return Msg;
    }
    public void Send(String Msg)
    {
        while(!_Started);
        Messages.add(Msg);
    }
    protected boolean _ServerStopped = false;
    private int _TransmittersUID=0;
    private int GetNewTransmitterUID()
    {
        int UID = _TransmittersUID;
        _TransmittersUID++;
        return UID;
    }
    private void Handle()
    {
        PackageTransmitter PT = _ClientListener.Get();
        if(!PT._IsSingle) return;
        Package PACKAGE = (Package)Package._GetObject(PT.GetData());
        Command _Command = PACKAGE.GetCommand();
        switch (_Command)
        {
            case MESSAGE: Log("\n"+PACKAGE.GetSender()+" : "+PACKAGE.GetData()); break;
            case EXIT: Log("\n> ROOM CLOSED."); _ServerStopped = true; break;
            case INFO_LOGIN: Log("\n> "+PACKAGE.GetData()+" JOINED ROOM."); break;
            case INFO_LOGOUT: Log("\n> "+PACKAGE.GetData()+" LEAVED ROOM."); break;
            default: break;
        }
    }
    protected boolean _ServerDisconnected = false;
    private void Start()
    {
        _ClientRoomActivity.PushStatus(Status.CLIENT_STARTING);
        _ServerCheckTimer = new Timer();
        _ServerCheckTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                _ClientSender.Send(new Package(Command.CHECK, "", _Login).GetSingleTransmitter(GetNewTransmitterUID()));
            }
        };
        StartConnector();
        if(!_ClientConnector.Success()) return;
        final String _Password = "#AveJava#";
        Package P_LOGIN = new Package(Command.LOGIN, _Password, _Login);
        _ClientSender.Send(P_LOGIN.GetSingleTransmitter(GetNewTransmitterUID()));
        Package P_LOGIN_UUID = new Package(Command.UUID, _UUID, _Login);
        _ClientSender.Send(P_LOGIN_UUID.GetSingleTransmitter(GetNewTransmitterUID()));
        Package P_LOGIN_RESULT = (Package)Package._GetObject(_ClientListener.Get().GetData());
        if(P_LOGIN_RESULT == null) { Fail(); return; }
        _ClientRoomActivity.PopStatus();
        if(P_LOGIN_RESULT.GetCommand()==Command.LOGIN_SUCCESS)
        {
            Log("\n> LOGIN SUCCESS.");
            _ServerCheckTimer.schedule(_ServerCheckTimerTask, SERVER_CHECK_TIME, SERVER_CHECK_TIME);
            _Started = true;
            while(!_Stop&&!_ServerStopped)
            {
                if(HasMessages())
                {
                    Package MESSAGE = new Package(Command.MESSAGE, GetMessage(), _Login);
                    _ClientSender.Send(MESSAGE.GetSingleTransmitter(GetNewTransmitterUID()));
                }
                if(_ClientListener.HasPackages())
                {
                    Handle();
                }
            }
            if(!_ServerStopped) _ClientSender.Send(new Package(Command.EXIT,"",_Login).GetSingleTransmitter(GetNewTransmitterUID()));
            else if (_ServerDisconnected)
            {
                Log("\nSERVER DISCONNECTED.");
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
