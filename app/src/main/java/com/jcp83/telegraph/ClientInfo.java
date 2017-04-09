package com.jcp83.telegraph;

import java.util.UUID;

public class ClientInfo
{
    private String _Name;
    private UUID _UUID;
    public String GetName() { return _Name; }
    public UUID GetUUID() { return _UUID; }
    public ClientInfo(String _Name, UUID _UUID)
    {
        this._Name = _Name;
        this._UUID = _UUID;
    }
}
