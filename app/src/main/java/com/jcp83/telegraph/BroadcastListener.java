package com.jcp83.telegraph;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class BroadcastListener extends Thread
{
    public static final int PORT = 7001;
    public static final int TIMEOUT = 500;
    private boolean _Started = false;
    boolean Started() { return _Started; }
    private boolean _Stop = false;
    private boolean _Stopped = false;
    private Socket _Socket;
    public boolean Stopped() { return _Stopped; }
    public void Stop() { _Stop = true; }
    ServerConnector _ServerConnector;
    protected void Log(String Msg)
    {
        _ServerConnector.Log(Msg);
    }
    public BroadcastListener(ServerConnector _ServerConnector)
    {
        this._ServerConnector = _ServerConnector;
    }
    private void Start()
    {
        Log("\nBROADCAST RECEIVER STARTED.");
        _Started = true;
        try
        {
            ServerSocket _ServerSocket = new ServerSocket(PORT);
            _ServerSocket.setSoTimeout(TIMEOUT);
            while(!_Stop)
            {
                boolean Connected = false;
                while(!Connected&&!_Stop)
                {
                    Connected = true;
                    try
                    {
                        _Socket = _ServerSocket.accept();
                        Log("\n\tACCEPTED");
                    }
                    catch (SocketTimeoutException e) { Connected = false; }
                }
                if(_Stop)
                {
                    _ServerSocket.close();
                    Log("\nBROADCAST RECEIVER STOPPED");
                    _Stopped = true;
                    return;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                _Socket.close();
            }
            catch (IOException e1) { }
        }
        Log("\nBROADCAST RECEIVER STOPPED");
        _Stopped = true;
    }
    public void run()
    {
        Start();
    }
}
