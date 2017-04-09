package com.jcp83.telegraph;

import java.util.ArrayList;

public class PackageTask
{
    private ArrayList<PackageTransmitter> _Stack = new ArrayList<>();
    private boolean _ToAll;
    private int _ReceiverID;
    public PackageTask(int _ReceiverID)
    {
        this._ReceiverID = _ReceiverID;
        this._ToAll = false;
    }
    public PackageTask()
    {
        _ToAll = true;
    }
    public void Add(PackageTransmitter P) { _Stack.add(P); }
    public boolean IsCompleted() { return _Stack.size()==0; }
    public PackageTransmitter Handle()
    {
        PackageTransmitter P = _Stack.get(0);
        _Stack.remove(0);
        return P;
    }
    public boolean IsToAll() { return _ToAll; }
    public int GetReceiverID() { return _ReceiverID; }
}
