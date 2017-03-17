package com.jcp83.telegraph;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FindRoomActivity extends AppCompatActivity
{
    public static final String ServerIPIntentID = "SERVER_IP";
    public static final int START_TIMEOUT=100;
    private SeekBar _FindRoomTimeoutSeekBar = null;
    private ProgressBar _FindRoomProgressBar = null;
    private TextView _FindRoomTimeoutNTextView = null;
    private TextView _FindRoomProgressTextView = null;
    ListView _FoundedRoomsListView;
    ArrayList<String> _Rooms = new ArrayList<>();
    ArrayAdapter<String> _RoomsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_room);
        _FindRoomTimeoutSeekBar = (SeekBar)findViewById(R.id.FindRoomTimeoutSeekBar);
        _FindRoomProgressBar = (ProgressBar)findViewById(R.id.FindRoomProgressBar);
        _FindRoomTimeoutNTextView = (TextView)findViewById(R.id.FindRoomTimeoutNTextView);
        _FindRoomProgressTextView = (TextView)findViewById(R.id.FindRoomProgressTextView);
        _FoundedRoomsListView = (ListView)findViewById(R.id.FoundedRoomsListView);
        _RoomsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _Rooms);
        _FoundedRoomsListView.setAdapter(_RoomsAdapter);
        _RoomsClickListener = new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            { Join(i); }
        };
        _FoundedRoomsListView.setOnItemClickListener(_RoomsClickListener);
        _TimeoutChangeListener = new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                _FindRoomTimeoutNTextView.setText(String.valueOf(_FindRoomTimeoutSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                _SenderAccepter.SetTimeout(_FindRoomTimeoutSeekBar.getProgress());
            }
        };
        _FindRoomTimeoutSeekBar.setOnSeekBarChangeListener(_TimeoutChangeListener);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        StartBroadcastAccepter();
        _SenderAccepter.SetTimeout(START_TIMEOUT);
        _FindRoomTimeoutNTextView.setText(String.valueOf(START_TIMEOUT));
        _FindRoomTimeoutSeekBar.setProgress(START_TIMEOUT);
        _FindRoomProgressTextView.setText("0");
        FindRoom();
    }
    AdapterView.OnItemClickListener _RoomsClickListener;
    SeekBar.OnSeekBarChangeListener _TimeoutChangeListener;
    private void Join(int ID)
    {
        if(_SenderAccepter==null) return;
        Intent RoomJoinIntent = new Intent(FindRoomActivity.this,ClientRoomActivity.class);
        RoomJoinIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String ServerIP = _SenderAccepter.Join(ID);
        if(ServerIP==null) return;
        RoomJoinIntent.putExtra(ServerIPIntentID, ServerIP);
        StopBroadcastAccepter();
        startActivity(RoomJoinIntent);
    }
    protected void AddRoom(String _IP)
    {
        new Thread(new AddRoom(_IP)).start();
    }
    protected void NotifyRoomsAdded() { new Thread(new NotifyRoomsAdded()).start(); }
    protected void NotifySent(int Progress)
    {
        _FindRoomProgressBar.setProgress(Progress);
        new Thread(new NotifySent(Progress)).start();
    }
    class AddRoom implements Runnable
    {
        final String IP;
        public void run()
        {
            _FoundedRoomsListView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _Rooms.add(IP);
                }
            });
        }
        public AddRoom(String IP) { this.IP = IP; }
    }
    class NotifyRoomsAdded implements Runnable
    {
        public void run()
        {
            _FoundedRoomsListView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _RoomsAdapter.notifyDataSetChanged();
                }
            });
        }
    }
    class NotifySent implements Runnable
    {
        final int Progress;
        public void run()
        {
            _FoundedRoomsListView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _FindRoomProgressTextView.setText(String.valueOf(Progress));
                }
            });
        }
        public NotifySent(int Progress) { this.Progress = Progress; }
    }
    BroadcastSenderAccepter _SenderAccepter;
    Thread _SenderAccepterThread;
    public void ExitFromFindRoomButtonClick(View view)
    {
        Exit();
    }
    private void Exit()
    {
        StopBroadcastAccepter();
        startActivity(new Intent(FindRoomActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    private void FindRoom()
    {
        StartBroadcastAccepter();
        _SenderAccepter.Send();
    }
    private void StartBroadcastAccepter()
    {
        if(_SenderAccepter!=null&&_SenderAccepter.Started()) return;
        _SenderAccepter = new BroadcastSenderAccepter(this);
        _SenderAccepterThread = new Thread(_SenderAccepter);
        _SenderAccepterThread.start();
        while(!_SenderAccepter.Started());
    }
    private void StopBroadcastAccepter()
    {
        if(_SenderAccepter==null) return;
        if(_SenderAccepter.Stopped()) return;
        _SenderAccepter.Stop();
        while(!_SenderAccepter.Stopped());
        _SenderAccepter=null;
    }
}