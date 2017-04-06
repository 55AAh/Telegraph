package com.jcp83.telegraph;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    protected Settings _Settings;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, Context.MODE_PRIVATE));
        setContentView(R.layout.activity_main);
    }
    private void OpenSettings()
    {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        if(!_Settings.CheckInitConfig()) OpenSettings();
        _Settings.Load();
        new LockOrientation(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int ID = item.getItemId();
        switch(ID)
        {
            case R.id.action_settings: OpenSettings(); break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void CreateButtonClick(View view)
    {
        //startActivity(new Intent(MainActivity.this, FileSelectDialogActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        if(!_Settings.CheckInitConfig()) OpenSettings();
        startActivity(new Intent(MainActivity.this,RoomPresetActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    public void JoinButtonClick(View view)
    {
        startActivity(new Intent(MainActivity.this,FindRoomActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}