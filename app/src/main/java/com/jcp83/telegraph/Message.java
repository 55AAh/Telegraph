package com.jcp83.telegraph;

public class Message
{
    protected String _Sender;
    protected String _Text;
    protected String _Time;
    public Message(String _Sender, String _Text, String _Time)
    {
        this._Sender = _Sender;
        this._Text = _Text;
        this._Time = _Time;
    }
}
