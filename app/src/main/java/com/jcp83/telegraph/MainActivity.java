package com.jcp83.telegraph;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
        RequestRFPermission();
        startActivity(new Intent(MainActivity.this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        if(!_Settings.CheckInitConfig()) OpenSettings();
        _Settings.Load();
        new LockOrientation(this);
        RequestRFPermission();
    }
    private void Restart()
    {
        Toast.makeText(getApplicationContext(), "REBOOTING ...", Toast.LENGTH_LONG).show();
        Intent _RestartActivity = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, 0, _RestartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager _AlarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        _AlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
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
    private boolean RequestRFPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) return true;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, OpenFileDialog.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case OpenFileDialog.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Restart();
                }
                return;
            }
        }
    }
    public void CreateButtonClick(View view)
    {
        if(!RequestRFPermission()) return;
        if(!_Settings.CheckInitConfig()) OpenSettings();
        startActivity(new Intent(MainActivity.this,RoomPresetActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    public void JoinButtonClick(View view)
    {
        if(!RequestRFPermission()) return;
        if(!_Settings.CheckInitConfig()) OpenSettings();
        startActivity(new Intent(MainActivity.this,FindRoomActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}