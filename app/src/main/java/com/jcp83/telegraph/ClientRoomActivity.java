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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

public class ClientRoomActivity extends AppCompatActivity
{
    protected Client _Client = null;
    protected Settings _Settings;
    private TextView _MessagesBox = null;
    private EditText _MessageBox = null;
    private ScrollView _ClientMessagesBoxScrollView = null;
    private TableLayout _ClientMessagesBoxTableLayout = null;
    private TextView _StatusTextView;
    private String ServerIP;
    private String UserName;
    private UUID _UUID;
    private AlertDialog _ExitDialog;
    private AlertDialog.Builder _FilesDialogBuilder;
    private AlertDialog.Builder _FilesMessageDialogBuilder;
    private AlertDialog _FilesDialog;
    private AlertDialog _FilesMessageDialog;
    private AlertDialog.OnClickListener _FilesListAdapterOnClickListener;
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
        CreateDialogs();
    }
    protected void DownloadFile(int ID)
    {
        Message _Msg = new Message(_Client._Login, "DOWNLOADING FILE '"+_FileNames.get(ID)+"' ...", _Client.GetTime());
        _Msg._FileTaskUID = _Client.GetNewTaskUID();
        ShowMessage(_Msg, Color.BLUE);
        _Client.SendSystemMessage(new Package(Command.DOWNLOAD_FILE, _Msg._FileTaskUID+":"+ID, _Client._Login));
    }
    private int _FileMessageDialogFileID;
    private void CreateDialogs()
    {
        AlertDialog.Builder _ExitDialogBuilder = new AlertDialog.Builder(this);
        _ExitDialogBuilder.setTitle("Выход").setMessage("Вы действительно хотите удалить комнату ?");
        _ExitDialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) { Exit(); }
        });
        _ExitDialogBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) { }
        });
        _ExitDialog = _ExitDialogBuilder.create();

        _FilesDialogBuilder = new AlertDialog.Builder(this);
        _FilesDialogBuilder.setTitle("Файлы");
        _FilesDialog = _FilesDialogBuilder.create();
        _FilesMessageDialogBuilder = new AlertDialog.Builder(this);
        _FilesMessageDialogBuilder.setTitle("Подтверждение");
        _FilesMessageDialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                DownloadFile(_FileMessageDialogFileID);
            }
        });
        _FilesMessageDialogBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        new LockOrientation(this);
        _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, MODE_PRIVATE));
        _Settings.Load();
        ServerIP = getIntent().getStringExtra(FindRoomActivity.ServerIPIntentID);
        UserName = _Settings.GetUserName();
        _UUID = _Settings.GetUUID();
        _FilesListAdapterOnClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                DownloadFile(which);
            }
        };
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
        CharSequence[] _FileNamesC = new CharSequence[_FileNames.size()];
        for(int c=0;c<_FileNames.size(); c++) _FileNamesC[c]=_FileNames.get(c);
        _FilesDialogBuilder.setItems(_FileNamesC, _FilesListAdapterOnClickListener)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });
        _FilesDialog = _FilesDialogBuilder.create();
        _FilesDialog.show();
    }
    private void OpenExitDialog()
    {
        _ExitDialog.show();
    }
    protected String GetServerIP()
    {
        return ServerIP;
    }
    protected ArrayList<String> _FileNames = new ArrayList<>();
    protected void NotifyFileUploaded(String Name, int ID)
    {
        Message _Msg = new Message("SERVER", "NEW FILE ADDED : '"+Name+"' ("+ID+").", _Client.GetTime());
        _Msg._FileID = ID;
        ShowMessage(_Msg, Color.BLUE);
    }
    protected void UploadFile(String _Path)
    {
        _Client.UploadFile(_Path);
        _FileNames.add(_Path.substring(_Path.lastIndexOf('/')));
    }
    protected void F()
    {
        OpenFileDialog _OpenFileDialog = new OpenFileDialog(this);
        _OpenFileDialog._ClientRoomActivity = this;
        _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, MODE_PRIVATE));
        _Settings.Load();
        _OpenFileDialog._CurrentPath = _Settings.GetLastUploadDir();
        _OpenFileDialog.show();
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
    private void DownloadFileMessageClick(int ID)
    {
        String FileName = _FileNames.get(ID);
        _FilesMessageDialogBuilder.setMessage("Скачать файл '"+FileName+"' ?");
        _FilesMessageDialog = _FilesMessageDialogBuilder.create();
        _FileMessageDialogFileID = ID;
        _FilesMessageDialog.show();
    }
    public void ExitFromClientRoomButtonClick(View view)
    {
        Exit();
    }
    public void ClientSendMessageButtonClick(View view) { SendMessage(_MessageBox.getText().toString()); }
    protected ArrayList<Message> _Messages = new ArrayList<>();
    void ShowMessage(Message Msg, int Color)
    {
        _Messages.add(Msg);
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
                    _Msg._SenderTextView = _MessageSenderTextView;
                    _Msg._TextView = _MessageTextView;
                    _MessageRow.addView(_MessageTimeTextView);
                    _MessageRow.addView(_MessageSenderTextView);
                    _MessageRow.addView(_MessageTextView);
                    _MessageTextView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            DownloadFileMessageClick(_Msg._FileID);
                        }
                    });
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
