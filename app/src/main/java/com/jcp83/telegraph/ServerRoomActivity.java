package com.jcp83.telegraph;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class ServerRoomActivity extends AppCompatActivity
{
    private final int PORT = 7000;
    private Server _Server = null;
    private Thread _ServerThread = null;
    protected Settings _Settings;
    private TextView _MessagesBox = null;
    private ScrollView _ServerMessagesBoxScrollView = null;
    private TableLayout _ServerMessagesBoxTableLayout = null;
    private TextView _StatusTextView = null;
    private EditText _MessageBox = null;
    private ToggleButton _VisibilityToggleButton = null;
    private AlertDialog _ExitDialog;
    private AlertDialog.Builder _FilesDialogBuilder;
    private AlertDialog _FilesDialog;
    private ListAdapter _FilesListAdapter;
    private AlertDialog.OnClickListener _FilesListAdapterOnClickListener;
    protected ArrayList<String> _DownloadedFilesNotifyList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);
        _MessagesBox = (TextView)findViewById(R.id.ServerMessagesBox);
        _ServerMessagesBoxScrollView = (ScrollView)findViewById(R.id.ServerMessagesBoxScrollView);
        _ServerMessagesBoxTableLayout = (TableLayout)findViewById(R.id.ServerMessagesBoxTableLayout);
        _StatusTextView = (TextView)findViewById(R.id.ServerStatusTextView);
        _MessageBox = (EditText)findViewById(R.id.ServerMessageBox);
        _StatusesStack.add(Status.SERVER_IDLE);
        _StatusTextView.setText(GetStringStatus(Status.SERVER_IDLE));
        _VisibilityToggleButton = (ToggleButton)findViewById(R.id.ServerVisibilityToggleButton);
        _VisibilityToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(b) StartConnector(); else StopConnector();
            }
        });
        CreateDialogs();
    }
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
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        new LockOrientation(this);
        Intent _Intent = getIntent();
        _Server = new Server(this, PORT, _Intent.getStringExtra(RoomPresetActivity.RoomNameIntentID));
        _ServerThread = new Thread(_Server);
        StartServer();
        boolean _Visibility = _Intent.getBooleanExtra(RoomPresetActivity.RoomStartVisibilityIntentID, true);
        if(_Visibility) StartConnector();
        _VisibilityToggleButton.setChecked(_Visibility);
        _FilesListAdapterOnClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        };
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.server_menu, menu);
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
                .setNeutralButton("Добавить", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        F();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
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
    private void ScrollMessagesBoxScrollView()
    {
        _ServerMessagesBoxScrollView.post(new Runnable() {
            @Override
            public void run() {
                _ServerMessagesBoxScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    protected void Exit()
    {
        _Server.Stop();
        while(!_Server.Stopped());
        startActivity(new Intent(ServerRoomActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    protected ArrayList<String> _FileNames = new ArrayList<>();
    protected void UploadFile(String _Path)
    {
        _Server.UploadFile(_Path);
        _FileNames.add(_Path.substring(_Path.lastIndexOf('/')+1));
    }
    protected void F()
    {
        OpenFileDialog _OpenFileDialog = new OpenFileDialog(this);
        _OpenFileDialog._ServerRoomActivity = this;
        _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, MODE_PRIVATE));
        _Settings.Load();
        _OpenFileDialog._CurrentPath = _Settings.GetLastUploadDir();
        _OpenFileDialog.show();
    }
    protected void SetRoomNameToStatus()
    {
        _StatusTextView.setText(_Server._RoomName);
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
        while(!_Server.Started());
    }
    public void ExitFromServerRoomButtonClick(View view) { Exit(); }
    void ShowMessage(Message Msg, int _Color)
    {
        new Thread(new ShowMessage(Msg, _Color, this)).start();
    }
    public void ServerSendMessageButtonClick(View view)
    {
        if(!_Server.Started()) return;
        _Server.SendText(_MessageBox.getText().toString());
        _MessageBox.setText("");
    }
    class ShowMessage implements Runnable
    {
        final Message _Msg;
        final int _Color;
        final ServerRoomActivity _ServerRoomActivity;
        @Override
        public void run()
        {
            _MessagesBox.post(new Runnable()
            {
                public void run()
                {
                    TableRow _MessageRow = new TableRow(_ServerRoomActivity);
                    TextView _MessageTimeTextView = new TextView(_ServerRoomActivity);
                    _MessageTimeTextView.setText(_Msg._Time);
                    TextView _MessageSenderTextView = new TextView(_ServerRoomActivity);
                    _MessageSenderTextView.setText(" "+_Msg._Sender+" ");
                    _MessageSenderTextView.setTextColor(Color.BLUE);
                    TextView _MessageTextView = new TextView(_ServerRoomActivity);
                    _MessageTextView.setText(_Msg._Text);
                    _MessageTextView.setTextColor(_Color);
                    _MessageRow.addView(_MessageTimeTextView);
                    _MessageRow.addView(_MessageSenderTextView);
                    _MessageRow.addView(_MessageTextView);
                    _ServerMessagesBoxTableLayout.addView(_MessageRow);
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    ScrollMessagesBoxScrollView();
                }
            });
        }
        public ShowMessage(Message _Msg, int _Color, ServerRoomActivity _ServerRoomActivity)
        {
            this._Msg = _Msg;
            this._Color = _Color;
            this._ServerRoomActivity = _ServerRoomActivity;
        }
    }
    private String GetStringStatus(Status _Status)
    {
        switch(_Status)
        {
            case SERVER_IDLE:if(_Server==null||_Server._RoomName==null)
                return getString(R.string.Status_SERVER_IDLE_Text);
                else return _Server._RoomName;
            case SERVER_STARTING:return getString(R.string.Status_SERVER_STARTING_Text);
            case SERVER_STOPPING:return getString(R.string.Status_SERVER_STOPPING_Text);
            case SERVER_CONNECTOR_STARTING:return getString(R.string.Status_SERVER_CONNECTOR_STARTING_Text);
            case SERVER_CONNECTOR_STOPPING:return getString(R.string.Status_SERVER_CONNECTOR_STOPPING_Text);
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
