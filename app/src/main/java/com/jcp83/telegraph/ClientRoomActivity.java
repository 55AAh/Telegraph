package com.jcp83.telegraph;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ClientRoomActivity extends AppCompatActivity
{
    private Client _Client = null;
    private TextView _MessagesBox = null;
    private EditText _MessageBox = null;
    private ScrollView _ClientMessagesBoxScrollView = null;
    private TableLayout _ClientMessagesBoxTableLayout = null;
    private TextView _StatusTextView;
    private String ServerIP;
    private String UserName;
    private UUID _UUID;
    private AlertDialog _ExitDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_room);
        _MessagesBox = (TextView)findViewById(R.id.ClientMessagesBox);
        _ClientMessagesBoxScrollView = (ScrollView)findViewById(R.id.ClientMessagesBoxScrollView);
        _ClientMessagesBoxTableLayout = (TableLayout)findViewById(R.id.ClientMessagesBoxTableLayout);
        _MessageBox = (EditText)findViewById(R.id.ClientMessageBox);
        _StatusTextView = (TextView)findViewById(R.id.ClientStatusTextView);
        _StatusesStack.add(Status.CLIENT_IDLE);
        _StatusTextView.setText(GetStringStatus(Status.CLIENT_IDLE));
        AlertDialog.Builder _ExitDialogBuilder = new AlertDialog.Builder(this);
        _ExitDialogBuilder.setTitle("Выход").setMessage("Вы действительно хотите выйти из комнаты ?");
        _ExitDialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) { Exit(); }
        });
        _ExitDialogBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) { }
        });
        _ExitDialog = _ExitDialogBuilder.create();
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
        Start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int ID = item.getItemId();
        switch(ID)
        {
            case R.id.action_settings: OpenSettings(); break;
            case R.id.action_files: OpenFilesMenu(); break;
            case R.id.action_exit: OpenExitDialog(); break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void OpenSettings()
    {

    }
    private void OpenFilesMenu()
    {

    }
    private void OpenExitDialog()
    {
        _ExitDialog.show();
    }
    protected String GetServerIP()
    {
        return ServerIP;
    }
    protected void UploadFile(String Path)
    {
        /*_Client.UploadFile(Path);*/
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
    void ShowMessage(Message Msg, int Color)
    {
        new Thread(new ClientRoomActivity.ShowMessage(Msg, Color, this)).start();
    }
    class ShowMessage implements Runnable
    {
        final Message _Msg;
        final int _Color;
        final ClientRoomActivity _ClientRoomActivity;
        @Override
        public void run()
        {
            _MessagesBox.post(new Runnable()
            {
                @Override
                public void run()
                {
                    TableRow _MessageRow = new TableRow(_ClientRoomActivity);
                    TextView _MessageTimeTextView = new TextView(_ClientRoomActivity);
                    _MessageTimeTextView.setText(_Msg._Time);
                    TextView _MessageSenderTextView = new TextView(_ClientRoomActivity);
                    _MessageSenderTextView.setText(" "+_Msg._Sender+" ");
                    _MessageSenderTextView.setTextColor(Color.BLUE);
                    TextView _MessageTextView = new TextView(_ClientRoomActivity);
                    _MessageTextView.setText(_Msg._Text);
                    _MessageTextView.setTextColor(_Color);
                    _MessageRow.addView(_MessageTimeTextView);
                    _MessageRow.addView(_MessageSenderTextView);
                    _MessageRow.addView(_MessageTextView);
                    _ClientMessagesBoxTableLayout.addView(_MessageRow);
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    ScrollMessagesBoxScrollView();
                }
            });
        }
        public ShowMessage(Message _Msg, int _Color, ClientRoomActivity _ClientRoomActivity)
        {
            this._Msg = _Msg;
            this._Color = _Color;
            this._ClientRoomActivity = _ClientRoomActivity;
        }
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
                }
            });
        }
        public SetStatus(String _Status) { this._Status = _Status; }
    }
}
