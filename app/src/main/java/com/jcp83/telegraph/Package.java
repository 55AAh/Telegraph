package com.jcp83.telegraph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Package implements Serializable
{
    static final long serialVersionUID=1;
    public final Command _Command;
    public final String _Data;
    public Package(Command _Command,String _Data)
    {
        this._Command=_Command;
        this._Data=_Data;
    }
    public static byte[] GetBytes(Package P)
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
    public static Package GetPackage(byte[] B)
    {
        ByteArrayInputStream _BAIS = new ByteArrayInputStream(B);
        ObjectInputStream _OIS;
        try
        {
            _OIS = new ObjectInputStream(_BAIS);
            return (Package)_OIS.readObject();
        }
        catch (Exception e) { return null; }
    }
}