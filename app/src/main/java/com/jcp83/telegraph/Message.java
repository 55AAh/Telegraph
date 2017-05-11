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
            final String _NewSender = _Sender;
            _SenderTextView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _SenderTextView.setText(_NewSender);
                }
            });
        }
        catch(Exception e) {}
    }
    protected void SetText(final String _Text)
    {
        try
        {
            this._Text = _Text;
            final String _NewText = _Text;
            _TextView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    _TextView.setText(_NewText);
                }
            });
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
