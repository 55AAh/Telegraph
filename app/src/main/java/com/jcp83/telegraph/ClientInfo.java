package com.jcp83.telegraph;

import java.util.ArrayList;
import java.util.UUID;

public class ClientInfo
{
    private String _Name;
    private UUID _UUID;
    private Server _Server;
    protected ServerSender _Sender;
    protected ServerListener _Listener;
    protected boolean _Disconnected = false;
    protected void Receive()
    {
        if(!_Listener._Stack.isEmpty())
        {
            PackageTransmitter _Transmitter = _Listener.Get();
            if(_Transmitter._IsSystem) { _Server.HandleSystemMessage(_Transmitter, _UUID); return; }
            if(_Transmitter._IsSystemTask) { _TasksPopStack.get(0).Add(_Transmitter); return; }
            for(int c=0;c<_TasksPopStack.size();c++)
            {
                PackageTask _Task = _TasksPopStack.get(c);
                if (_Task._UID == _Transmitter._UID)
                {
                    _Task.Add(_Transmitter);
                    break;
                }
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
            if(!_Task._Stack.isEmpty()) Send(_Task.Get());
            if(_Task.IsCompleted()) _TasksPushStack.remove(_LastHandledPushTask);
            _LastHandledPushTask++;
        }
        if(!_TasksPopStack.isEmpty())
        {
            PackageTask _Task = _TasksPopStack.get(_LastHandledPopTask);
            if(!_Task._Stack.isEmpty())
            {
                if(_LastHandledPopTask==0)
                    _Server.HandleSystemTaskTransmitter(_Task.Get(), _UUID);
                //else

            }
            if(_Task.IsCompleted()) _TasksPopStack.remove(_LastHandledPushTask);
            _LastHandledPopTask++;
        }
    }
    public String GetName() { return _Name; }
    public UUID GetUUID() { return _UUID; }
    public ClientInfo(Server _Server, ServerListener _Listener, ServerSender _Sender, String _Name, UUID _UUID)
    {
        this._Server = _Server;
        this._Sender = _Sender;
        this._Listener = _Listener;
        this._Name = _Name;
        this._UUID = _UUID;
    }
}