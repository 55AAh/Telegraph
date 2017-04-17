package com.jcp83.telegraph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Package implements Serializable
{
    static final long serialVersionUID=1;
    private final Command _Command;
    protected byte[] _Data = null;
    private final String _Sender;
    public Package(Command _Command, Object _Data, String _Sender)
    {
        this._Command=_Command;
        this._Data = _GetBytes(_Data);
        this._Sender = _Sender;
    }
    public Package(Command _Command, byte[] _Data, String _Sender)
    {
        this._Command = _Command;
        this._Data = _Data;
        this._Sender = _Sender;
    }
    public Command GetCommand()
    {
        return _Command;
    }
    public Object GetData()
    {
        return _GetObject(_Data);
    }
    public String GetSender() { return _Sender; }
    public PackageTransmitter GetTransmitter(int UID)
    {
        PackageTransmitter PT = new PackageTransmitter(UID, 0);
        PT.SetData(_GetBytes(this));
        return PT;
    }
    public static byte[] _GetBytes(Object P)
    {
        ByteArrayOutputStream _BAOS = new ByteArrayOutputStream();
        ObjectOutputStream _OOS;
        try
        {
            _OOS = new ObjectOutputStream(_BAOS);
            _OOS.writeObject(P);
        }
        catch (Exception e) { return null; }
        return _BAOS.toByteArray();
    }
    public static Object _GetObject(byte[] B)
    {
        ByteArrayInputStream _BAIS = new ByteArrayInputStream(B);
        ObjectInputStream _OIS;
        try
        {
            _OIS = new ObjectInputStream(_BAIS);
            return _OIS.readObject();
        }
        catch (Exception e) { e.printStackTrace(); return null; }
    }
}