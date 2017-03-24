package com.jcp83.telegraph;

import java.io.Serializable;

public class RoomInfo implements Serializable
{
    private String Name;
    public String GetName() { return Name; }
    RoomInfo(String Name)
    {
        this.Name = Name;
    }
}