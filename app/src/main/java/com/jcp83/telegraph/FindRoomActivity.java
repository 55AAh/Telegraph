package com.jcp83.telegraph;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FindRoomActivity extends AppCompatActivity
{
    Button _FindRoomButton;
    private TextView _MessagesBox = null;
    private ScrollView _FindRoomMessagesBoxScrollView = null;
    ListView _FoundedRoomListView;
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
        _FoundedRoomListView = (ListView)findViewById(R.id.FoundedRoomsListView);
        _RoomsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _Rooms);
        _FoundedRoomListView.setAdapter(_RoomsAdapter);
        StartBroadcastSender();
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
                    _MessagesBox.append("\n"+Msg);
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
    public void FindRoomButtonClick(View view)
    {
        _Sender.Send();
        //_Rooms.add("JAVA");
        //_RoomsAdapter.notifyDataSetChanged();
    }
    BroadcastSender _Sender;
    Thread _SenderThread;
    private void StartBroadcastSender()
    {
        _Sender = new BroadcastSender(this);
        _SenderThread = new Thread(_Sender);
        _SenderThread.start();
        while(!_Sender.Started());
    }
    private void StopBroadcastSender()
    {
        _Sender.Stop();
        while(!_Sender.Stopped());
    }
}