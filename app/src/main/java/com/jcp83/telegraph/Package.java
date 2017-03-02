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
    private byte[] _Data = null;
    public Package(Command _Command)
    {
        this._Command = _Command;
    }
    public Package(Command _Command,Object _Data)
    {
        this._Command=_Command;
        this._Data = _GetBytes(_Data);
    }
    public Command GetCommand()
    {
        return _Command;
    }
    public Object GetData()
    {
        return _GetObject(_Data);
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