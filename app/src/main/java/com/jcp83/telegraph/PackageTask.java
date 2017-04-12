package com.jcp83.telegraph;

import java.util.ArrayList;

public class PackageTask
{
    protected ArrayList<PackageTransmitter> _Stack = new ArrayList<>();
    protected int _UID;
    protected boolean _Completed = false;
    public PackageTask(int _UID)
    {
        this._UID = _UID;
    }
    public void Add(PackageTransmitter P) { _Stack.add(P); }
    public boolean IsCompleted() { return _Completed; }
    public PackageTransmitter Get()
    {
        PackageTransmitter P = _Stack.get(0);
        _Stack.remove(0);
        return P;
    }
}
