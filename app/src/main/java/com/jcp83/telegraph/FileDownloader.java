package com.jcp83.telegraph;

import android.content.Context;

import java.io.File;
import java.io.OutputStream;

public class FileDownloader implements Runnable
{
    private String _Path;
    protected PackageTask _Task;
    protected Thread _Thread;
    private OutputStream _OS;
    private ClientRoomActivity _ClientRoomActivity;
    public FileDownloader(String _Path, PackageTask _Task, ClientRoomActivity _ClientRoomActivity)
    {
        this._Path = _Path;
        this._Task = _Task;
        this._ClientRoomActivity = _ClientRoomActivity;
    }
    private void Init()
    {
        try
        {
            _OS = _ClientRoomActivity.openFileOutput(_Path, Context.MODE_PRIVATE);
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void Receive()
    {
        boolean _Stop = false;
        while(!_Stop)
        {
            if (_Task._Stack.isEmpty()) continue;
            PackageTransmitter _Transmitted = _Task.Get();
            Package PACKAGE = (Package)Package._GetObject(_Transmitted.GetData());
            switch(PACKAGE.GetCommand())
            {
                case FILE:
                    try
                    {
                        _OS.write(Package._GetBytes(PACKAGE.GetData()));
                    }
                    catch (Exception e) { e.printStackTrace(); }
                    break;
                case TASK_ENDED: _Stop = true; break;
                default: break;
            }
        }
    }
    private void End()
    {
        try
        {
            _OS.close();
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    public void run()
    {
        Init();
        Receive();
        End();
        _Task._Completed = true;
    }
}
