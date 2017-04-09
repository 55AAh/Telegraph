package com.jcp83.telegraph;

import android.content.SharedPreferences;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

public class Settings implements Serializable
{
    public static final String APP_SETTINGS = "SETTINGS";
    public static final String APP_SETTINGS_INITIALIZED = "INITIALIZED";
    public static final String APP_SETTINGS_USERNAME = "USERNAME";
    public static final String APP_SETTINGS_UUID = "UUID";
    private SharedPreferences _Settings;
    private String _UserName;
    private UUID _UUID;
    protected boolean CheckInitConfig()
    {
        return _Settings.contains(APP_SETTINGS_INITIALIZED);
    }
    protected void Load()
    {
        if(!CheckInitConfig()) return;
        _UserName = _Settings.getString(APP_SETTINGS_USERNAME, "USER"+Math.abs(new Random().nextInt(1000)));
        _UUID = UUID.fromString(_Settings.getString(APP_SETTINGS_UUID, UUID.randomUUID().toString()));
    }
    protected void Save()
    {
        SharedPreferences.Editor _Editor = _Settings.edit();
        if(!CheckInitConfig()) _Editor.putBoolean(APP_SETTINGS_INITIALIZED, true);
        _Editor.putString(APP_SETTINGS_USERNAME, _UserName);
        _Editor.putString(APP_SETTINGS_UUID, _UUID.toString());
        _Editor.apply();
    }
    protected String GetUserName()
    {
        return _UserName;
    }
    protected UUID GetUUID() { return _UUID; }
    protected void SetUserName(String _UserName)
    {
        this._UserName = _UserName;
    }
    Settings(SharedPreferences _Settings)
    {
        this._Settings = _Settings;
    }
}
