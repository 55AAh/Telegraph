package com.jcp83.telegraph;

import android.content.SharedPreferences;

import java.io.Serializable;
import java.util.Random;

public class Settings implements Serializable
{
    public static final String APP_SETTINGS = "SETTINGS";
    public static final String APP_SETTINGS_INITIALIZED = "INITIALIZED";
    public static final String APP_SETTINGS_USERNAME = "USERNAME";
    private SharedPreferences _Settings;
    private String _UserName;
    protected boolean CheckInitConfig()
    {
        return _Settings.contains(APP_SETTINGS_INITIALIZED);
    }
    protected void Load()
    {
        if(!CheckInitConfig()) return;
        _UserName = _Settings.getString(APP_SETTINGS_USERNAME, "USER"+Math.abs(new Random().nextInt(1000)));
    }
    protected void Save()
    {
        if(!CheckInitConfig()) return;
        SharedPreferences.Editor _Editor = _Settings.edit();
        _Editor.putString(APP_SETTINGS_USERNAME, _UserName);
        _Editor.apply();
    }
    protected String GetUserName()
    {
        return _UserName;
    }
    protected void SetUserName(String _UserName)
    {
        this._UserName = _UserName;
    }
    Settings(SharedPreferences _Settings)
    {
        this._Settings = _Settings;
    }
}
