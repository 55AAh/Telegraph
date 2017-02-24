package com.jcp83.telegraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ServerRoomActivity extends AppCompatActivity
{
    final int PORT = 7000;
    private Server _Server = null;
    private Thread _ServerThread = null;
    private TextView _MessagesBox = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);
        _MessagesBox = (TextView)findViewById(R.id.ServerMessagesBox);
        _Server = new Server(this, PORT);
        _ServerThread = new Thread(_Server);
    }
    private void Exit()
    {
        _Server.Exit();
        //startActivity(new Intent(ServerRoomActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
        _ServerThread.start();
    }
    protected void ExitFromServerRoomButtonClick(View view) { Exit(); }
    protected void StartServerConnectorButtonClick(View view) { StartConnector(); }
    protected void StopServerConnectorButtonClick(View view) { StopConnector(); }
    protected void StartServerButtonClick(View view) { StartServer(); }
    public TextView GetMessagesBox() { return _MessagesBox; }
    public void ShowMessage(String Msg)
    {
        new Thread(new ShowMessage(Msg)).start();
    }
    class ShowMessage implements Runnable
    {
        String Msg;
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
