package com.jcp83.telegraph;

import java.util.ArrayList;

public class ServerInfo
{
    private Client _Client;
    protected ClientSender _Sender;
    protected ClientListener _Listener;
    protected boolean _Disconnected = false;
    protected void Receive()
    {
        if(!_Listener._Stack.isEmpty())
        {
            PackageTransmitter _Transmitter = _Listener.Get();
            if(_Transmitter._IsSystem) _Client.HandleSystemMessage(_Transmitter);
            boolean HasTask = false;
            for(int c=0;c<_TasksPopStack.size();c++)
            {
                PackageTask _Task = _TasksPopStack.get(c);
                if (_Task._UID == _Transmitter._UID)
                {
                    _Task.Add(_Transmitter);
                    HasTask = true;
                    break;
                }
            }
            if(!HasTask)
            {
                PackageTask _Task = new PackageTask(_Client.GetNewTaskUID());
                _Task.Add(_Transmitter);
                _TasksPopStack.add(_Task);
            }
        }
    }
    protected void Send(PackageTransmitter _Transmitter)
    {
        _Sender.Send(_Transmitter);
        if(_Sender._Failed) { _Disconnected = true; }
    }
    protected ArrayList<PackageTask> _TasksPushStack = new ArrayList<>();
    protected ArrayList<PackageTask> _TasksPopStack = new ArrayList<>();
    protected int _LastHandledPushTask=0;
    protected int _LastHandledPopTask=0;
    protected void HandleTasks()
    {
        if(_LastHandledPushTask>_TasksPushStack.size()-1) _LastHandledPushTask=0;
        if(_LastHandledPopTask>_TasksPopStack.size()-1) _LastHandledPopTask=0;
        if(!_TasksPushStack.isEmpty())
        {
            PackageTask _Task = _TasksPushStack.get(_LastHandledPushTask);
            Send(_Task.Handle());
            if(_Task.IsCompleted()) _TasksPushStack.remove(_LastHandledPushTask);
            _LastHandledPushTask++;
        }
        if(!_TasksPopStack.isEmpty())
        {
            PackageTask _Task = _TasksPopStack.get(_LastHandledPopTask);
            _Client.HandleTransmitter(_Task.Handle());
            if(_Task.IsCompleted()) _TasksPopStack.remove(_LastHandledPushTask);
            _LastHandledPopTask++;
        }
    }
    public ServerInfo(Client _Client, ClientListener _Listener, ClientSender _Sender)
    {
        this._Client = _Client;
        this._Listener = _Listener;
        this._Sender = _Sender;
    }
}
