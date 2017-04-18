package com.jcp83.telegraph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FileDownloader implements Runnable
{
    public static final int REQUEST_QUEUE_SIZE = 10;
    private String _Path;
    protected PackageTask _Task;
    protected Thread _Thread;
    private String _RPath;
    private FileOutputStream _Stream;
    private BufferedOutputStream _BStream;
    private ClientRoomActivity _ClientRoomActivity;
    private ServerRoomActivity _ServerRoomActivity;
    public FileDownloader(String _Path, PackageTask _Task, ClientRoomActivity _ClientRoomActivity)
    {
        this._Path = _Path;
        this._Task = _Task;
        this._Task._UseOffset = true;
        this._ClientRoomActivity = _ClientRoomActivity;
    }
    public FileDownloader(String _Path, PackageTask _Task, ServerRoomActivity _ServerRoomActivity)
    {
        this._Path = _Path;
        this._Task = _Task;
        this._ServerRoomActivity = _ServerRoomActivity;
    }
    private void Init()
    {
        try
        {
            File _FHandle = new File(_Path);
            if(!_FHandle.getParentFile().exists()) _FHandle.getParentFile().mkdirs();
            int _ResultIndex = 1;
            while(_FHandle.exists())
            {
                _RPath = FilePath.Get(_Path, _ResultIndex);
                _FHandle = new File(_RPath);
                _ResultIndex++;
            }
            _FHandle.createNewFile();
            _Stream = new FileOutputStream(_FHandle);
            _BStream = new BufferedOutputStream(_Stream);
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void Receive()
    {
        int _RequestElapsed = REQUEST_QUEUE_SIZE;
        Package REQUEST = new Package(Command.TASK_REQUEST, REQUEST_QUEUE_SIZE, String.valueOf(_Task._UID));
        _ClientRoomActivity._Client.SendSystemMessage(REQUEST);
        boolean _Stop = false;
        while(!_Stop)
        {
            if (_Task._Stack.isEmpty()) continue;
            PackageTransmitter _Transmitter = _Task.Get();
            if(_Transmitter==null) continue;
            Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
            switch(PACKAGE.GetCommand())
            {
                case FILE:
                    try
                    {
                        byte[] a = PACKAGE._Data;
                        _BStream.write(a);
                    }
                    catch (Exception e) { e.printStackTrace(); }
                    _RequestElapsed--;
                    if(_RequestElapsed<=0)
                    {
                        _RequestElapsed = REQUEST_QUEUE_SIZE;
                        _ClientRoomActivity._Client.SendSystemMessage(REQUEST);
                    }
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
            _BStream.flush();
            _BStream.close();
            _Stream.close();
        }
        catch(Exception e) { e.printStackTrace(); }
        if(_ClientRoomActivity!=null) _ClientRoomActivity._DownloadedFilesNotifyList.add(_RPath);
        if(_ServerRoomActivity!=null) _ServerRoomActivity._DownloadedFilesNotifyList.add(_RPath);
    }
    public void run()
    {
        Init();
        Receive();
        End();
        _Task._Completed = true;
    }
}
