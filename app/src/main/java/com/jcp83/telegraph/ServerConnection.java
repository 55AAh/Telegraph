/*package com.jcp83.telegraph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class _ServerConnection implements Runnable
{
    final int Timeout=30000;
    Socket _Socket;
    String Login;
    int ConnectionID;
    ServerRoomActivity _ServerRoomActivity;
    public _ServerConnection(Socket _Socket, int ConnectionID)
    {
        this._Socket = _Socket;
        this.ConnectionID = ConnectionID;
    }
    public void Log(String Msg)
    {
        _ServerRoomActivity.ShowMessage("SERVER ["+ConnectionID+"] : "+Msg);
    }
    public void Disconnect()
    {
        Log("Client disconnected.");
        try
        {
            _Socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void run()
    {
        Log("New client connected.");
        try
        {
            _Socket.setSoTimeout(0);
            ObjectInputStream DIS = new ObjectInputStream(_Socket.getInputStream());
            String Buf;
            Package LOGIN_P=(Package)DIS.readObject();
            Login = LOGIN_P._Data;
            Log("Client login : "+Login);
            Package LOGIN_PASSWORD_P=(Package)DIS.readObject();
            Buf=LOGIN_PASSWORD_P._Data;
            if(LOGIN_P._Command!= Command.LOGIN||LOGIN_PASSWORD_P._Command!=Command.LOGIN_PASSWORD) {Log("Incorrect login signature."); return;}
            ObjectOutputStream DOS = new ObjectOutputStream(_Socket.getOutputStream());
            if(Buf.compareTo("1234")!=0)
            {
                Log("Incorrect password !");
                Package LOGIN_FAILED_P = new Package(Command.LOGIN_FAILED, "");
                DOS.writeObject(LOGIN_FAILED_P);
                DOS.flush();
                return;
            }
            Log("Client successfully connected.");
            Package LOGIN_SUCCESS_P = new Package(Command.LOGIN_SUCCESS, "");
            DOS.writeObject(LOGIN_SUCCESS_P);
            DOS.flush();
            _Socket.setSoTimeout(Timeout);
            while (true)
            {
                Package MESSAGE = (Package)DIS.readObject();
                switch(MESSAGE._Command)
                {
                    case MESSAGE:
                        Log("Message from client       : " + MESSAGE._Data);
                        MESSAGE._Data = "#" + MESSAGE._Data;
                        Log("Sending message to client : " + MESSAGE._Data);
                        DOS.writeObject(MESSAGE);
                        DOS.flush();
                        break;
                    default:
                        break;
                }

            }
        }
        catch (Exception e){e.printStackTrace();Disconnect();}
    }
}
*/