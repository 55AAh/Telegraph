package com.jcp83.telegraph;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FindRoomActivity extends AppCompatActivity
{
    public static final String ServerIPIntentID = "SERVER_IP";
    public static final String UserNameIntentID = "USERNAME";
    public static final String UserUUIDIntentID = "UUID";
    private ListView _FoundedRoomsListView;
    protected ArrayList<String> _Rooms;
    protected ArrayAdapter<String> _RoomsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_room);
        _Rooms = new ArrayList<>();
        _FoundedRoomsListView = (ListView)findViewById(R.id.FoundedRoomsListView);
        _RoomsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _Rooms);
        _RoomsAdapter.setNotifyOnChange(true);
        _FoundedRoomsListView.setAdapter(_RoomsAdapter);
        _RoomsClickListener = new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            { Join(i); }
        };
        _FoundedRoomsListView.setOnItemClickListener(_RoomsClickListener);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        new LockOrientation(this);
        FindRoom();
    }
    AdapterView.OnItemClickListener _RoomsClickListener;
    private void Join(int ID)
    {
        if(_SenderAccepter==null) return;
        _Joining = true;
        String ServerIP = _SenderAccepter.Join(ID);
        Intent RoomJoinIntent = new Intent(FindRoomActivity.this,ClientRoomActivity.class);
        RoomJoinIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(ServerIP==null) return;
        RoomJoinIntent.putExtra(ServerIPIntentID, ServerIP);
        Settings _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, MODE_PRIVATE));
        _Settings.Load();
        RoomJoinIntent.putExtra(UserNameIntentID, _Settings.GetUserName());
        RoomJoinIntent.putExtra(UserUUIDIntentID, _Settings.GetUUID().toString());
        StopBroadcastAccepter();
        startActivity(RoomJoinIntent);
    }
    protected void AddRoom(final String _Name)
    {
        _FoundedRoomsListView.post(new Runnable()
        {
            @Override
            public void run()
            {
                _RoomsAdapter.add(_Name);
            }
        });
    }
    private boolean _Joining = false;
    protected void NotifyRoomsChanged()
    {
        if(_RoomsAdapter==null||_FoundedRoomsListView==null||_Joining) return;
        _FoundedRoomsListView.post(new Runnable()
        {
            @Override
            public void run()
            {
                _RoomsAdapter.notifyDataSetChanged();
            }
        });
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
    }
    private void StartBroadcastAccepter()
    {
        _SenderAccepter = new BroadcastSenderAccepter(this);
        _SenderAccepterThread = new Thread(_SenderAccepter);
        _SenderAccepterThread.start();
        while(!_SenderAccepter.Started());
        _SenderAccepter._KnownRooms.clear();
    }
    private void StopBroadcastAccepter()
    {
        if (_SenderAccepter == null) return;
        if (_SenderAccepter.Stopped()) return;
        _SenderAccepter.Stop();
        _SenderAccepter = null;
    }
}