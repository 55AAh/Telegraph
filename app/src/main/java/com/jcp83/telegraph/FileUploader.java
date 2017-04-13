package com.jcp83.telegraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUploader implements Runnable
{
    public static final int BUFFER_SIZE = 1024;
    protected PackageTask _Task;
    private String _Path;
    private String _Sender;
    private int _Offset = 0;
    private long _Availaible;
    private File _File;
    private InputStream _IS;
    protected Thread _Thread;
    public FileUploader(PackageTask _Task, String _Path, String _Sender)
    {
        this._Task = _Task;
        this._Path = _Path;
        this._Sender = _Sender;
    }
    protected void Init()
    {
        try
        {
            _File = new File(_Path);
            _IS = new FileInputStream(_File);
            _Availaible = _File.length();
            _Task._Elapsed = _Availaible%BUFFER_SIZE+1;
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    protected void End()
    {
        try
        {
            _IS.close();
        }
        catch (Exception e) { }
    }
    protected void Send()
    {
        if(_Availaible<=0) return;
        byte[] _Buf;
        if(_Availaible>=BUFFER_SIZE) _Buf = new byte[BUFFER_SIZE]; else _Buf=new byte[(int)_Availaible];
        try
        {
            _IS.read(_Buf);
        }
        catch (Exception e) { e.printStackTrace(); }
        _Task.Add(new Package(Command.FILE, _Buf, _Sender).GetTransmitter(_Task._UID));
        _Availaible-=BUFFER_SIZE;
    }
    public void run()
    {
        Init();
        while(_Availaible>0) Send();
        _Task.Add(new Package(Command.TASK_ENDED, "", _Sender).GetTransmitter(_Task._UID));
        End();
    }
}
