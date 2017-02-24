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
        this._ClientRoomActivity.GetMessagesBox().append("\nCLIENT - PASCAL");
        this.PORT = PORT;
    }
    void Log(String Msg)
    {
        _ClientRoomActivity.ShowMessage(Msg);
    }
    private void Fail()
    {
        Log("Client failed.");
    }
    private boolean _Started = false;
    private boolean _Stop = false;
    protected void Stop() { _Stop = true; }
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
        //Log("SENDING : " + Msg);
        //_ClientSender.Send(new Package(Command.MESSAGE, Msg));
        Messages.add(Msg);
    }
    private void Start()
    {
        StartConnector();
        while(_ClientListener==null||_ClientSender==null);
        Log("Login : "+_Login);
        final String _Password = "1234";
        Log("Password : "+ _Password);
        Log("Connecting ...");
        Package P_LOGIN = new Package(Command.LOGIN, _Login);
        Package P_LOGIN_PASSWORD = new Package(Command.LOGIN_PASSWORD, _Password);
        _ClientSender.Send(P_LOGIN);
        _ClientSender.Send(P_LOGIN_PASSWORD);
        Package P_LOGIN_RESULT = _ClientListener.Get();
        if(P_LOGIN_RESULT == null) { Fail(); return; }
        if(P_LOGIN_RESULT._Command==Command.LOGIN_SUCCESS)
        {
            Log("Login success.");
            _Started = true;
            while(!_Stop)
            {
                if(HasMessages())
                {
                    Package MESSAGE = new Package(Command.MESSAGE, GetMessage());
                    _ClientSender.Send(MESSAGE);
                }
                if(_ClientListener.HasPackages())
                {
                    Package P = _ClientListener.Get();
                    if(P._Command==Command.MESSAGE)
                        Log("SERVER : "+P._Data);
                }
                /*Package P = _ClientListener.Get();
                if(P==null) { Fail(); return; }
                if(P._Command == Command.MESSAGE)
                    Log("ANSWER : " + P._Data);*/
            }
        }
        else
        {
            Log("Login failed !");
        }
    }
    public void run() { Start(); }
    private void StartConnector()
    {
        _Login = "USER" + new Random().nextInt(100);
        if(_ClientConnector!=null) return;
        _ClientConnector = new ClientConnector(_ClientRoomActivity, this, PORT);
        Thread _ClientConnectorThread = new Thread(_ClientConnector);
        _ClientConnectorThread.start();
        while(!_ClientConnector.Started());
        Log("CLIENT CONNECTOR STARTED.");
    }
}
