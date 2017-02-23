package com.jcp83.telegraph;

import java.util.List;
import java.util.Random;

public class Client
{
    protected ClientConnector _ClientConnector = null;
    protected Thread _ClientConnectorThread = null;
    private int PORT;
    private String _Login;
    private String _Password = "1234";
    protected ClientListener _ClientListener;
    protected ClientSender _ClientSender;
    protected Thread _ClientListenerThread;
    protected Thread _ClientSenderThread;
    private ClientRoomActivity _ClientRoomActivity;
    public Client(ClientRoomActivity _ClientRoomActivity, int PORT)
    {
        this._ClientRoomActivity = _ClientRoomActivity;
        this._ClientRoomActivity.GetMessagesBox().setText(this._ClientRoomActivity.GetMessagesBox().getText()+"\nCLIENT - PASCAL");
        this.PORT = PORT;
    }
    protected void Log(String Msg)
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
    private List<String> Messages;
    //protected void Send()
    public void Send(String Msg)
    {
        while(!_Started);
        Log("SENDING : " + Msg);
        _ClientSender.Send(new Package(Command.MESSAGE, Msg));
        _ClientSender.Flush();
    }
    public void Start()
    {
        StartConnector();
        while(_ClientListener==null||_ClientSender==null);
        Log("Login : "+_Login);
        Log("Password : "+_Password);
        Log("Connecting ...");
        Package P_LOGIN = new Package(Command.LOGIN, _Login);
        Package P_LOGIN_PASSWORD = new Package(Command.LOGIN_PASSWORD, _Password);
        _ClientSender.Send(P_LOGIN);
        _ClientSender.Send(P_LOGIN_PASSWORD);
        _ClientSender.Flush();
        Package P_LOGIN_RESULT = _ClientListener.Get();
        if(P_LOGIN_RESULT == null) { Fail(); return; }
        if(P_LOGIN_RESULT._Command==Command.LOGIN_SUCCESS)
        {
            Log("Login success.");
            _Started = true;
            while(!_Stop)
            {
                Package P = _ClientListener.Get();
                if(P==null) { Fail(); return; }
                if(P._Command == Command.MESSAGE)
                    Log("ANSWER : " + P._Data);
            }
        }
        else
        {
            Log("Login failed !");
        }
    }
    private void StartConnector()
    {
        _Login = "USER" + new Random().nextInt(100);
        if(_ClientConnector!=null) return;
        _ClientConnector = new ClientConnector(_ClientRoomActivity, this, PORT);
        _ClientConnectorThread = new Thread(_ClientConnector);
        _ClientConnectorThread.start();
        while(!_ClientConnector.Started());
        Log("CLIENT CONNECTOR STARTED.");
    }
}
