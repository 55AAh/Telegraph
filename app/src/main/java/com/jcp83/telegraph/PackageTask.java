package com.jcp83.telegraph;

import java.util.ArrayList;

public class PackageTask
{
    private ArrayList<PackageTransmitter> _Stack = new ArrayList<>();
    protected int _Elapsed = 0;
    protected int _UID;
    public PackageTask(int _UID)
    {
        this._UID=_UID;
    }
    protected boolean _Completed = false;
    public void Add(PackageTransmitter P) { _Stack.add(P); _Elapsed++; }
    public boolean IsCompleted() { return _Completed; }
    public PackageTransmitter Handle()
    {
        PackageTransmitter P = _Stack.get(0);
        _Stack.remove(0);
        _Elapsed--;
        if(_Elapsed==0) _Completed = true;
        return P;
    }
}
