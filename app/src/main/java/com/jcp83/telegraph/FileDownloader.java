package com.jcp83.telegraph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FileDownloader implements Runnable
{
    public static final int MAX_WAIT_CYCLES = 30;
    private String _Path;
    protected PackageTask _Task;
    protected Thread _Thread;
    private String _RPath;
    protected int _MsgID;
    private Message _Msg = null;
    private int _FileSize = 0;
    private int _FileReadySize = 0;
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
            boolean _FSFound = false;
            while(!_FSFound)
            {
                for (int c = 0; c<_Task._Stack.size();c++)
                {
                    PackageTransmitter _Transmitter = _Task._Stack.get(c);
                    Package PACKAGE = (Package)Package._GetObject(_Transmitter.GetData());
                    if(PACKAGE.GetCommand()==Command.FILE_SIZE)
                    {
                        _FileSize = Integer.valueOf(PACKAGE.GetData().toString());
                        _FSFound = true;
                        break;
                    }
                }
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void Receive()
    {
        int _Offset = 0;
        int _WaitCycles = 0;
        boolean _Stop = false;
        Package REQUEST = new Package(Command.TASK_REQUEST, 0, String.valueOf(_Task._UID));
        if(_ClientRoomActivity!=null)
            _ClientRoomActivity._Client.SendSystemMessage(REQUEST);
        //if(_ServerRoomActivity!=null)
        //    _ServerRoomActivity._Server.SendSystemMessage(REQUEST);
        while(!_Stop)
        {
            try
            {
                boolean _NoPackages = false;
                PackageTransmitter _Transmitter = null;
                if (_Task._Stack.isEmpty()) _NoPackages = true;
                else
                {
                    _Transmitter = _Task.Get();
                    if (_Transmitter == null) _NoPackages = true;
                }
                if (_NoPackages)
                {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                    }
                    _WaitCycles++;
                    if (_WaitCycles >= MAX_WAIT_CYCLES)
                    {
                        _WaitCycles = 0;
                        REQUEST = new Package(Command.TASK_REQUEST, _Offset, String.valueOf(_Task._UID));
                        _ClientRoomActivity._Client.SendSystemMessage(REQUEST);
                        //if(_ServerRoomActivity!=null)
                        //    _ServerRoomActivity._Server.SendSystemMessage(REQUEST);
                    }
                    continue;
                }
                Package PACKAGE = (Package) Package._GetObject(_Transmitter.GetData());
                switch (PACKAGE.GetCommand())
                {
                    case FILE:
                        try
                        {
                            byte[] BUF = PACKAGE._Data;
                            _FileReadySize += BUF.length;
                            float _Completed = Math.round((_FileReadySize * 10000f) / _FileSize) / 100f;
                            if(_Completed == 100f)
                            {
                                _Completed = 0;
                            }

                            _Msg.SetSender((" " + _Completed) + "%");
                            _BStream.write(BUF);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        _Offset++;
                        REQUEST = new Package(Command.TASK_REQUEST, _Offset, String.valueOf(_Task._UID));
                        _ClientRoomActivity._Client.SendSystemMessage(REQUEST);
                        //if(_ServerRoomActivity!=null)
                        //    _ServerRoomActivity._Server.SendSystemMessage(REQUEST);
                        break;
                    case TASK_ENDED:
                        _Stop = true;
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e) {}
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
        _Msg.SetText("FILE '"+_RPath+"' DOWNLOADED.");
        _Msg.SetSender("100 %");
    }
    public void run()
    {
        Init();
        while(_Msg==null)
        {
            if (_ClientRoomActivity != null)
                for (int c = 0; c < _ClientRoomActivity._Messages.size(); c++)
                {
                    _Msg = _ClientRoomActivity._Messages.get(c);
                    if (_Msg._FileTaskUID == _MsgID) break;
                }
            //if(_ServerRoomActivity!=null)
            //_Msg = _ServerRoomActivity._Messages.get(_MsgID);
        }
        _Msg.SetSender(" 0%");
        Receive();
        End();
        _Task._Completed = true;
    }
}
