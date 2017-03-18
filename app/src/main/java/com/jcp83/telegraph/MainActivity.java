package com.jcp83.telegraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        new LockOrientation(this);
    }
    public void CreateButtonClick(View view)
    {
        startActivity(new Intent(MainActivity.this,ServerRoomActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
    public void JoinButtonClick(View view)
    {
        startActivity(new Intent(MainActivity.this,FindRoomActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}