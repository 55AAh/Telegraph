package com.jcp83.telegraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class FindRoomActivity extends AppCompatActivity
{
    Button _FindRoomButton;
    private TextView _MessagesBox = null;
    private ScrollView _FindRoomMessagesBoxScrollView = null;
    ListView _FoundedRoomsListView;
    ArrayList<String> _Rooms = new ArrayList<>();
    ArrayAdapter<String> _RoomsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_room);
        _MessagesBox = (TextView)findViewById(R.id.FindRoomMessagesBox);
        _FindRoomMessagesBoxScrollView = (ScrollView)findViewById(R.id.FindRoomMessagesBoxScrollView);
        _FindRoomButton = (Button)findViewById(R.id.FindRoomButton);
        _FoundedRoomsListView = (ListView)findViewById(R.id.FoundedRoomsListView);
        _RoomsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _Rooms);
        _FoundedRoomsListView.setAdapter(_RoomsAdapter);
        _RoomsClickListener = new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Join(i);
            }
        };
        _FoundedRoomsListView.setOnItemClickListener(_RoomsClickListener);
    }
    AdapterView.OnItemClickListener _RoomsClickListener;
    private void Join(int ID)
    {
        _SenderAccepter.Join(ID);
        StopBroadcastAccepter();
        startActivity(new Intent(FindRoomActivity.this,ClientRoomActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    protected void ClearRooms()
    {
        _Rooms.clear();
        _RoomsAdapter.notifyDataSetChanged();
    }
    protected void AddRoom(String _IP)
    {
        _Rooms.add(_IP);
        _RoomsAdapter.notifyDataSetChanged();
    }
    public void ShowMessage(String Msg)
    {
        new Thread(new ShowMessage(Msg)).start();
    }
    class ShowMessage implements Runnable
    {
        final String Msg;
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
    private void ScrollMessagesBoxScrollView()
    {
        _FindRoomMessagesBoxScrollView.post(new Runnable() {
            @Override
            public void run()
            {
                _FindRoomMessagesBoxScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    public void StartBroadcastSenderAccepterButtonClick(View view)
    {
        StartBroadcastAccepter();
    }
    public void StopBroadcastSenderAccepterButtonClick(View view)
    {
        StopBroadcastAccepter();
    }
    public void FindRoomButtonClick(View view)
    {
        _SenderAccepter.Send();
        //_Rooms.add("JAVA");
        //_RoomsAdapter.notifyDataSetChanged();
    }
    BroadcastSenderAccepter _SenderAccepter;
    Thread _SenderAccepterThread;
    private void StartBroadcastAccepter()
    {
        _SenderAccepter = new BroadcastSenderAccepter(this);
        _SenderAccepterThread = new Thread(_SenderAccepter);
        _SenderAccepterThread.start();
        while(!_SenderAccepter.Starting());
    }
    private void StopBroadcastAccepter()
    {
        _SenderAccepter.Stop();
        while(!_SenderAccepter.Stopped());
    }
}