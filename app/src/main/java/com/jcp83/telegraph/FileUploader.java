package com.jcp83.telegraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileUploader
{
    public static final int BUFFER_SIZE = 1024;
    protected PackageTask _Task;
    private String _Path;
    private int _Offset = 0;
    private int _Availaible;
    private File _File;
    private InputStream _IS;
    private byte[] _Data;
    public FileUploader(PackageTask _Task, String _Path)
    {
        this._Task = _Task;
        this._Path = _Path;
    }
    protected void Init()
    {
        try
        {
            _File = new File(_Path);
            _IS = new FileInputStream(_File);
            _Availaible = _IS.available();
            _Data = new byte[_Availaible];
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
    protected boolean Send()
    {
        byte[] _Buf = new byte[BUFFER_SIZE];
        for(int sc=0;sc<BUFFER_SIZE;sc++)
            _Buf[sc]=_Data[_Offset*BUFFER_SIZE+sc];
        _Offset++;
        if(_Offset*BUFFER_SIZE>=_Availaible) return false;
        PackageTransmitter _Transmitter = new PackageTransmitter()
        /// /_Task.Add();
        return true;
    }
}
