package com.jcp83.telegraph;

import android.widget.TextView;

public class Message
{
    protected String _Sender;
    protected String _Text;
    protected String _Time;
    protected int _FileID;
    protected int _FileTaskUID;
    protected TextView _SenderTextView;
    protected TextView _TextView;
    protected void SetSender(String _Sender)
    {
        try
        {
            this._Sender = _Sender;
            _SenderTextView.setText(_Sender);
        }
        catch(Exception e) {}
    }
    protected void SetText(String _Text)
    {
        try
        {
            this._Text = _Text;
            _TextView.setText(_Text);
        }
        catch(Exception e) {}
    }
    public Message(String _Sender, String _Text, String _Time)
    {
        this._Sender = _Sender;
        this._Text = _Text;
        this._Time = _Time;
    }
}
