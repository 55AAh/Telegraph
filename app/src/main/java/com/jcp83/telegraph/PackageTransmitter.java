package com.jcp83.telegraph;

import java.io.Serializable;

public class PackageTransmitter implements Serializable
{
    private int _ID;
    private int _Offset;
    private byte[] _Data = null;
    protected boolean _IsSingle = false;
    public PackageTransmitter(int _ID, int _Offset)
    {
        this._ID = _ID;
        this._Offset = _Offset;
    }
    public int GetID() { return _ID; }
    public int Offset() { return _Offset; }
    public byte[] GetData()
    {
        return _Data;
    }
    public void SetData(byte[] _Data) { this._Data = _Data; }
}
