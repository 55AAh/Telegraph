package com.jcp83.telegraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ClientRoomActivity extends AppCompatActivity
{
    private final int PORT = 7000;
    private Client _Client = null;
    private Thread _ClientThread = null;
    private TextView _MessagesBox = null;
    private EditText _MessageBox = null;
    private EditText _ServerIPAddress = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);
        _MessagesBox = (TextView)findViewById(R.id.ClientMessagesBox);
        _MessageBox = (EditText)findViewById(R.id.ClientMessageBox);
        _ServerIPAddress = (EditText)findViewById(R.id.ServerAddressTextBox);
        _Client = new Client(this, PORT);
        _ClientThread = new Thread(_Client);
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
}
