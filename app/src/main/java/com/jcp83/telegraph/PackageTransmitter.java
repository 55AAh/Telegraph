package com.jcp83.telegraph;

import java.io.Serializable;

public class PackageTransmitter implements Serializable
{
    protected int _UID;
    private int _Offset;
    private byte[] _Data = null;
    protected boolean _IsSystem = false;
    protected boolean _IsSystemTask = false;
    public PackageTransmitter(int _UID, int _Offset)
    {
        this._UID = _UID;
        this._Offset = _Offset;
    }
    public int Offset() { return _Offset; }
    public byte[] GetData()
    {
        return _Data;
    }
    public void SetData(byte[] _Data) { this._Data = _Data; }
}
