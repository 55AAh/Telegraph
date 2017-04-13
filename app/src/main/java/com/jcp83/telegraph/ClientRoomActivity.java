package com.jcp83.telegraph;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ClientRoomActivity extends AppCompatActivity
{
    private Client _Client = null;
    private TextView _MessagesBox = null;
    private ScrollView _ClientMessagesBoxScrollView = null;
    private EditText _MessageBox = null;
    private TextView _StatusTextView;
    private String ServerIP;
    private String UserName;
    private UUID _UUID;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);
        _MessagesBox = (TextView)findViewById(R.id.ClientMessagesBox);
        _ClientMessagesBoxScrollView = (ScrollView)findViewById(R.id.ClientMessagesBoxScrollView);
        _MessageBox = (EditText)findViewById(R.id.ClientMessageBox);
        _StatusTextView = (TextView)findViewById(R.id.ClientStatusTextView);
        _StatusesStack.add(Status.CLIENT_IDLE);
        _StatusTextView.setText(GetStringStatus(Status.CLIENT_IDLE));
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        new LockOrientation(this);
        ServerIP = getIntent().getStringExtra(FindRoomActivity.ServerIPIntentID);
        UserName = getIntent().getStringExtra(FindRoomActivity.UserNameIntentID);
        String _UUIDString = getIntent().getStringExtra(FindRoomActivity.UserUUIDIntentID);
        _UUID = UUID.fromString(_UUIDString);
        FileOutputStream outputStream = null;
        String D = getFilesDir().getAbsolutePath();Start();
    }
    protected String GetServerIP()
    {
        return ServerIP;
    }
    private void ScrollMessagesBoxScrollView()
    {
        _ClientMessagesBoxScrollView.post(new Runnable() {
            @Override
            public void run()
            {
                _ClientMessagesBoxScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    private void Exit()
    {
        _Client.Stop();
        while(!_Client.Stopped());
        startActivity(new Intent(ClientRoomActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    private void Start()
    {
        int PORT = 7000;
        _Client = new Client(this, PORT, UserName, _UUID);
        Thread _ClientThread = new Thread(_Client);
        _ClientThread.start();
    }
    private void SendMessage(String Msg)
    {
        if(_Client==null||!_Client.Started()) return;
        _Client.SendText(Msg);
        _MessageBox.setText("");
    }
    public void ExitFromClientRoomButtonClick(View view)
    {
        Exit();
    }
    public void ClientSendMessageButtonClick(View view) { SendMessage(_MessageBox.getText().toString()); }
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
                    _MessagesBox.append(Msg);
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    ScrollMessagesBoxScrollView();
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
    private final ArrayList<Status> _StatusesStack = new ArrayList<>();
    void PushStatus(Status _Status)
    {
        _StatusesStack.add(0,_Status);
        _SetStatus(_Status);
    }
    void PopStatus()
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
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    ScrollMessagesBoxScrollView();
                }
            });
        }
        public SetStatus(String _Status) { this._Status = _Status; }
    }
}
