package com.jcp83.telegraph;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class FindRoomActivity extends AppCompatActivity
{
    public static final String ServerIPIntentID = "SERVER_IP";
    public static final String UserNameIntentID = "USERNAME";
    private ListView _FoundedRoomsListView;
    protected ArrayList<String> _Rooms = new ArrayList<>();
    private ArrayAdapter<String> _RoomsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_room);
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
        Intent RoomJoinIntent = new Intent(FindRoomActivity.this,ClientRoomActivity.class);
        RoomJoinIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String ServerIP = _SenderAccepter.Join(ID);
        if(ServerIP==null) return;
        RoomJoinIntent.putExtra(ServerIPIntentID, ServerIP);
        Settings _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, MODE_PRIVATE));
        _Settings.Load();
        RoomJoinIntent.putExtra(UserNameIntentID, _Settings.GetUserName());
        StopBroadcastAccepter();
        startActivity(RoomJoinIntent);
    }
    protected void AddRoom(String _Name)
    {
        new Thread(new AddRoom(_Name)).start();
    }
    protected void NotifyDataSetChanged() { new Thread(new NotifyDataSetChanged()).start(); }
    class AddRoom implements Runnable
    {
        final String Name;
        public void run()
        {
            _FoundedRoomsListView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _Rooms.add(Name);
                    /*try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) { }
                    NotifyDataSetChanged();*/
                }
            });
        }
        public AddRoom(String Name) { this.Name = Name; }
    }
    class NotifyDataSetChanged implements Runnable
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
        _Rooms.clear();
        NotifyDataSetChanged();
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
        _SenderAccepter._KnownRooms.clear();
    }
    private void StopBroadcastAccepter()
    {
        if(_SenderAccepter==null) return;
        if(_SenderAccepter.Stopped()) return;
        _SenderAccepter.Stop();
        _SenderAccepter=null;
    }
}