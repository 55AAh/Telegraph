package com.jcp83.telegraph;

import java.io.BufferedInputStream;
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
    private InputStream _Stream;
    private BufferedInputStream _BStream;
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
            _Stream = new FileInputStream(_File);
            _BStream = new BufferedInputStream(_Stream);
            _Availaible = _File.length();
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    protected void End()
    {
        PackageTransmitter _Transmitter = new Package(Command.TASK_ENDED, "", _Sender).GetTransmitter(_Task._UID);
        _Transmitter._Offset = _Offset;
        _Transmitter._Last = true;
        _Task.Add(_Transmitter);
        try
        {
            _BStream.close();
            _Stream.close();
        }
        catch (Exception e) { }
    }
    protected void Send()
    {
        if(_Availaible<=0) return;
        if(_Task._Request<=0) return;
        byte[] _Buf;
        if(_Availaible>=BUFFER_SIZE) _Buf = new byte[BUFFER_SIZE]; else _Buf=new byte[(int)_Availaible];
        try
        {
            _BStream.read(_Buf);
        }
        catch (Exception e) { e.printStackTrace(); }
        PackageTransmitter _Transmitter = new Package(Command.FILE, _Buf, _Sender).GetTransmitter(_Task._UID);
        _Transmitter._Offset = _Offset;
        _Offset++;
        _Task.Add(_Transmitter);
        _Availaible-=BUFFER_SIZE;
        _Task._Request--;
    }
    public void run()
    {
        Init();
        while(_Availaible>0) Send();
        End();
    }
}
