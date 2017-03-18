package com.jcp83.telegraph;

import java.util.ArrayList;
import java.util.Random;

class Client implements Runnable
{
    private ClientConnector _ClientConnector = null;
    private final int PORT;
    private String _Login;
    ClientListener _ClientListener;
    ClientSender _ClientSender;
    Thread _ClientListenerThread;
    Thread _ClientSenderThread;
    private final ClientRoomActivity _ClientRoomActivity;
    public Client(ClientRoomActivity _ClientRoomActivity, int PORT)
    {
        this._ClientRoomActivity = _ClientRoomActivity;
        this.PORT = PORT;
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
        _Stop = true;
        _ClientRoomActivity.PopStatus();
        _Stopped = true;
    }
    //protected void Stop() { _Stop = true; }
    private final ArrayList<String> Messages = new ArrayList<>();
    //protected void Send()
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
    private boolean _ServerStopped = false;
    private void Handle()
    {
        Package PACKAGE = _ClientListener.Get();
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
    private void Start()
    {
        _ClientRoomActivity.PushStatus(Status.CLIENT_STARTING);
        StartConnector();
        if(!_ClientConnector.Success()) return;
        final String _Password = "#AveJava#";
        Package P_LOGIN = new Package(Command.LOGIN, _Password, _Login);
        _ClientSender.Send(P_LOGIN);
        Package P_LOGIN_RESULT = _ClientListener.Get();
        if(P_LOGIN_RESULT == null) { Fail(); return; }
        _ClientRoomActivity.PopStatus();
        if(P_LOGIN_RESULT.GetCommand()==Command.LOGIN_SUCCESS)
        {
            Log("\n> LOGIN SUCCESS.");
            _Started = true;
            while(!_Stop&&!_ServerStopped)
            {
                if(HasMessages())
                {
                    Package MESSAGE = new Package(Command.MESSAGE, GetMessage(), _Login);
                    _ClientSender.Send(MESSAGE);
                }
                if(_ClientListener.HasPackages())
                {
                    Handle();
                }
            }
            if(!_ServerStopped) _ClientSender.Send(new Package(Command.EXIT,"",_Login));
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
        _Login = "USER" + new Random().nextInt(100);
        if(_ClientConnector!=null) return;
        _ClientConnector = new ClientConnector(_ClientRoomActivity, this, PORT);
        Thread _ClientConnectorThread = new Thread(_ClientConnector);
        _ClientConnectorThread.start();
        while(!_ClientConnector.Started()&&_ClientConnector.Success());
        if(!_ClientConnector.Success()) { Fail(); return; }
        _ClientRoomActivity.PopStatus();
    }
}
