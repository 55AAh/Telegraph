package com.jcp83.telegraph;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class ServerRoomActivity extends AppCompatActivity
{
    private final int PORT = 7000;
    private Server _Server = null;
    private Thread _ServerThread = null;
    private TextView _MessagesBox = null;
    private TextView _StatusTextView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);
        _MessagesBox = (TextView)findViewById(R.id.ServerMessagesBox);
        _StatusTextView = (TextView)findViewById(R.id.ServerStatusTextView);
        _Server = new Server(this, PORT);
        _ServerThread = new Thread(_Server);
        _StatusesStack.add(Status.SERVER_IDLE);
        _StatusTextView.setText(GetStringStatus(Status.SERVER_IDLE));
    }
    private void Exit()
    {
        _Server.Stop();
        while(!_Server.Stopped());
        startActivity(new Intent(ServerRoomActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    private void StartConnector()
    {
        _Server.StartConnector();
    }
    private void StopConnector()
    {
        _Server.StopConnector();
    }
    private void StartServer()
    {
        if(_Server.Started()) return;
        _ServerThread.start();
    }
    public void ExitFromServerRoomButtonClick(View view) { Exit(); }
    public void StartServerConnectorButtonClick(View view) { StartConnector(); }
    public void StopServerConnectorButtonClick(View view) { StopConnector(); }
    public void StartServerButtonClick(View view) { StartServer(); }
    public TextView GetMessagesBox() { return _MessagesBox; }
    protected void ShowMessage(String Msg)
    {
        new Thread(new ShowMessage(Msg)).start();
    }
    class ShowMessage implements Runnable
    {
        final String Msg;
        public void run()
        {
            _MessagesBox.post(new Runnable()
            {
                public void run()
                {
                    _MessagesBox.append("\n"+Msg);
                }
            });
        }
        public ShowMessage(String Msg) { this.Msg = Msg; }
    }
    private String GetStringStatus(Status _Status)
    {
        switch(_Status)
        {
            case SERVER_IDLE:return getString(R.string.Status_SERVER_IDLE_Text);
            case SERVER_STARTING:return getString(R.string.Status_SERVER_STARTING_Text);
            case SERVER_STOPPING:return getString(R.string.Status_SERVER_STOPPING_Text);
            case SERVER_CONNECTOR_STARTING:return getString(R.string.Status_SERVER_CONNECTOR_STARTING_Text);
            case SERVER_CONNECTOR_STOPPING:return getString(R.string.Status_SERVER_CONNECTOR_STOPPING_Text);
            default: return "";
        }
    }
    private ArrayList<Status> _StatusesStack = new ArrayList<>();
    protected void PushStatus(Status _Status)
    {
        _StatusesStack.add(0,_Status);
        _SetStatus(_Status);
    }
    protected void PopStatus()
    {
        if(_StatusesStack.size()>1) _StatusesStack.remove(0);
        _SetStatus(_StatusesStack.get(0));
    }
    private void _SetStatus(Status _Status)
    {
        new Thread(new SetStatus(GetStringStatus(_Status))).start();
    }
    class SetStatus implements Runnable
    {
        final String _Status;
        public void run()
        {
            _MessagesBox.post(new Runnable()
            {
                public void run()
                {
                    _StatusTextView.setText(_Status);
                }
            });
        }
        public SetStatus(String _Status) { this._Status = _Status; }
    }
}
