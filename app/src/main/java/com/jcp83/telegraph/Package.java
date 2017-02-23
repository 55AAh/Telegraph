package com.jcp83.telegraph;

import java.io.Serializable;

public class Package implements Serializable
{
    static final long serialVersionUID=1;
    public Command _Command;
    public String _Data;
    public Package(Command _Command,String _Data)
    {
        this._Command=_Command;
        this._Data=_Data;
    }
}