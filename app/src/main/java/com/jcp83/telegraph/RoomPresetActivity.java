package com.jcp83.telegraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.util.Random;

public class RoomPresetActivity extends AppCompatActivity
{
    public static final String RoomNameIntentID = "ROOM_NAME";
    public static final String RoomStartVisibilityIntentID = "ROOM_START_VISIBILITY";
    private EditText _RoomStartNameEditBox = null;
    private ToggleButton _RoomStartVisibilityToggleButton = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_preset);
        _RoomStartNameEditBox = (EditText)findViewById(R.id.RoomStartNameEditBox);
        _RoomStartVisibilityToggleButton = (ToggleButton)findViewById(R.id.RoomStartVisibilityToggleButton);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        _RoomStartNameEditBox.setText("ROOM".concat(String.valueOf(Math.abs(new Random().nextInt(1000)))));
    }
    private void Create()
    {
        Intent _Intent = new Intent(RoomPresetActivity.this, ServerRoomActivity.class);
        _Intent.putExtra(RoomNameIntentID, _RoomStartNameEditBox.getText().toString());
        _Intent.putExtra(RoomStartVisibilityIntentID, _RoomStartVisibilityToggleButton.isChecked());
        _Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(_Intent);
    }
    public void CreateRoomButtonClick(View view)
    {
        Create();
    }
}
