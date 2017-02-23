/*package com.jcp83.telegraph;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class _ClientConnection implements Runnable
{
    Client _Client;
    Socket _Socket;
    String Login;
    ClientRoomActivity _ClientRoomActivity;
    public void Log(String Msg)
    {
        _ClientRoomActivity.ShowMessage(Msg);
    }
    public _ClientConnection(Socket _Socket, Client _Client)
    {
        this._Socket=_Socket;
        this._Client=_Client;
    }
    public void run()
    {
        try
        {
            ObjectOutputStream DOS = new ObjectOutputStream(_Socket.getOutputStream());
            String Buf;
            Log("Enter login    : "); Login="USER_0";//Keyboard.readLine();
            Package LOGIN_P=new Package(Command.LOGIN,Login);
            Log("Enter password : ");
            Package LOGIN_PASSWORD_P=new Package(Command.LOGIN_PASSWORD,"1234");
            DOS.writeObject(LOGIN_P);
            DOS.writeObject(LOGIN_PASSWORD_P);
            DOS.flush();
            ObjectInputStream DIS = new ObjectInputStream(_Socket.getInputStream());
            Package LOGIN_RESULT_P = (Package)DIS.readObject();
            switch(LOGIN_RESULT_P._Command)
            {
                case LOGIN_SUCCESS:
                    Log("Login successfully.");
                    boolean Stop=false;
                    while(!Stop)
                    {
                        //Log("Enter message : ");
                        //Buf = "TEST MESSAGE";//Keyboard.readLine();
                        while(_Client.PushList.size()==0);
                        Buf=_Client.PushList.get(0)._Data;
                        _Client.PushList.remove(0);
                        Log("Sending \""+Buf+"\"");
                        Thread.sleep(1000);
                        Package MESSAGE = new Package(Command.MESSAGE, Buf);
                        DOS.writeObject(MESSAGE);
                        DOS.flush();
                        Package ANSWER = (Package) DIS.readObject();
                        Log("Server : ["+ANSWER._Command+"] : \""+ANSWER._Data+"\"");
                    }
                    _Socket.close();
                    break;
                case LOGIN_FAILED:
                    Log("Incorrect password !");
                    try
                    {
                        //_Socket.close();
                    }
                    catch(Exception e){}
                    return;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log("Server disconnected.");
        }
    }
}
*/