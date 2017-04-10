package com.jcp83.telegraph;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity
{
    private Settings _Settings;
    private EditText _Settings_UserNameBox;
    private String _UserName;
    private UUID _UUID;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        _Settings_UserNameBox = (EditText)findViewById(R.id.Settings_UserNameBox);
    }
    private void InitSettings()
    {
        _UserName = "USER"+Math.abs(new Random().nextInt(1000));
        _Settings_UserNameBox.post(new SetUserName(_UserName));
        _UUID = UUID.randomUUID();
    }
    class SetUserName implements Runnable
    {
        private String _UserName;
        @Override
        public void run()
        {
            _Settings_UserNameBox.setText(_UserName);
        }
        SetUserName(String _UserName)
        {
            this._UserName = _UserName;
        }
    }
    private void LoadSettings()
    {
        _Settings.Load();
        _Settings_UserNameBox.setText(_Settings.GetUserName());
    }
    private void SaveSettings()
    {
        _Settings.SetUserName(_Settings_UserNameBox.getText().toString());
        _Settings.SetUUID(_UUID);
        _Settings.Save();
        Toast.makeText(getApplicationContext(), "SETTINGS SAVED", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onStart()
    {
        _Settings = new Settings(getSharedPreferences(Settings.APP_SETTINGS, 0));
        if(_Settings.CheckInitConfig()) LoadSettings(); else InitSettings();
        super.onStart();
    }
    private void Exit()
    {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    public void SaveAndExitButtonClick(View view)
    {
        SaveSettings();
        Exit();
    }
}
