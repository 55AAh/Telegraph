package com.jcp83.telegraph;

import android.content.SharedPreferences;

import java.io.File;
import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

public class Settings implements Serializable
{
    public static final String APP_SETTINGS = "SETTINGS";
    public static final String APP_SETTINGS_INITIALIZED = "INITIALIZED";
    public static final String APP_SETTINGS_USERNAME = "USERNAME";
    public static final String APP_SETTINGS_UUID = "UUID";
    public static final String APP_SETTINGS_LAST_UPLOAD_DIR = "LAST_UPLOAD_DIR";
    public static final String APP_SETTINGS_DOWNLOAD_DIR = "DOWNLOAD_DIR";
    private SharedPreferences _Settings;
    private String _UserName;
    private UUID _UUID;
    private String _LastUploadDir;
    private String _DownloadDir;
    protected boolean CheckInitConfig()
    {
        return _Settings.contains(APP_SETTINGS_INITIALIZED);
    }
    protected void Load()
    {
        if(!CheckInitConfig()) return;
        _UserName = _Settings.getString(APP_SETTINGS_USERNAME, "USER"+Math.abs(new Random().nextInt(1000)));
        _UUID = UUID.fromString(_Settings.getString(APP_SETTINGS_UUID, UUID.randomUUID().toString()));
        _LastUploadDir = _Settings.getString(APP_SETTINGS_LAST_UPLOAD_DIR, "storage/emulated/0/Download");
        _DownloadDir = _Settings.getString(APP_SETTINGS_DOWNLOAD_DIR, "/storage/emulated/0/Download");
    }
    protected void Save()
    {
        SharedPreferences.Editor _Editor = _Settings.edit();
        if(!CheckInitConfig()) _Editor.putBoolean(APP_SETTINGS_INITIALIZED, true);
        _Editor.putString(APP_SETTINGS_USERNAME, _UserName);
        _Editor.putString(APP_SETTINGS_LAST_UPLOAD_DIR, _LastUploadDir);
        _Editor.putString(APP_SETTINGS_DOWNLOAD_DIR, _DownloadDir);
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
    protected UUID GetUUID() { return _UUID; }
    protected void SetUUID(UUID _UUID)
    {
        this._UUID = _UUID;
    }
    protected String GetLastUploadDir() { return _LastUploadDir; }
    protected void SetLastUploadDir(String _LastUploadDir) { this._LastUploadDir = _LastUploadDir; }
    protected String GetDownloadDir() { return _DownloadDir; }
    protected void SetDownloadDir(String _DownloadDir) { this._DownloadDir = _DownloadDir; }
    Settings(SharedPreferences _Settings)
    {
        this._Settings = _Settings;
    }
}
