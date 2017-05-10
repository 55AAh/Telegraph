package com.jcp83.telegraph;

import java.util.ArrayList;

public class PackageTask
{
    protected ArrayList<PackageTransmitter> _Stack = new ArrayList<>();
    protected int _UID;
    protected boolean _Completed = false;
    private int _LastOffset = 0;
    protected boolean _UseOffset = false;
    protected int _RequestOffset = -1;
    public PackageTask(int _UID)
    {
        this._UID = _UID;
    }
    public void Add(PackageTransmitter P)
    {
        if(!_UseOffset)
        {
            _Stack.add(P);
            return;
        }
        int _InsertIndex = 0;
        for(int c=0;c<_Stack.size();c++)
        {
            PackageTransmitter _Transmitter = _Stack.get(c);
            if(_Transmitter._Offset < P._Offset) _InsertIndex++; else break;
        }
        if(_InsertIndex<_Stack.size()) _Stack.add(_InsertIndex, P);
        else _Stack.add(P);
    }
    public boolean IsCompleted() { return _Completed; }
    public PackageTransmitter Get()
    {
        if(_UseOffset) if(_Stack.get(0)._Offset>_LastOffset) return null;
        PackageTransmitter P = _Stack.get(0);
        _Stack.remove(0);
        if(P._Last) _Completed = true;
        _LastOffset++;
        return P;
    }
}
