package com.jcp83.telegraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ClientRoomActivity extends AppCompatActivity
{
    private final int PORT = 7000;
    private Client _Client = null;
    private Thread _ClientThread = null;
    private TextView _MessagesBox = null;
    private EditText _MessageBox = null;
    private EditText _ServerIPAddress = null;
    private TextView _StatusTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);
        _MessagesBox = (TextView)findViewById(R.id.ClientMessagesBox);
        _MessageBox = (EditText)findViewById(R.id.ClientMessageBox);
        _ServerIPAddress = (EditText)findViewById(R.id.ServerAddressTextBox);
        _StatusTextView = (TextView)findViewById(R.id.ClientStatusTextView);
        _Client = new Client(this, PORT);
        _ClientThread = new Thread(_Client);
        _StatusesStack.add(Status.CLIENT_IDLE);
        _StatusTextView.setText(GetStringStatus(Status.CLIENT_IDLE));
    }
    private void Exit()
    {
        startActivity(new Intent(ClientRoomActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    String GetServerIP() { return _ServerIPAddress.getText().toString(); }
    private void Start()
    {
        _ClientThread.start();
    }
    private void SendMessage(String Msg)
    {
        _Client.Send(Msg);
        _MessageBox.setText("");
    }
    public void ExitFromClientRoomButtonClick(View view)
    {
        Exit();
    }
    public void StartButtonClick(View view) { Start(); }
    public void ClientSendMessageButtonClick(View view) { SendMessage(_MessageBox.getText().toString()); }
    public TextView GetMessagesBox() { return _MessagesBox; }
    public void ShowMessage(String Msg)
    {
        new Thread(new ShowMessage(Msg)).start();
    }
    class ShowMessage implements Runnable
    {
        final String Msg;
        @Override
        public void run()
        {
            _MessagesBox.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _MessagesBox.setText(_MessagesBox.getText()+"\n"+Msg);
                }
            });
        }
        public ShowMessage(String Msg) { this.Msg = Msg; }
    }
    private String GetStringStatus(Status _Status)
    {
        switch(_Status)
        {
            case CLIENT_IDLE:return getString(R.string.Status_CLIENT_IDLE_Text);
            case CLIENT_STARTING:return getString(R.string.Status_CLIENT_STARTING_Text);
            case CLIENT_STOPPING:return getString(R.string.Status_CLIENT_STOPPING_Text);
            case CLIENT_CONNECTOR_STARTING:return getString(R.string.Status_CLIENT_CONNECTOR_STARTING_Text);
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
