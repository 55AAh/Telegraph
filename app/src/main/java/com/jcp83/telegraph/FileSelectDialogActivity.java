package com.jcp83.telegraph;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSelectDialogActivity extends AppCompatActivity
{
    private File _CurrentDir;
    private List<String> _DirEntriesFullPath = new ArrayList<>();
    private List<String> _DirEntries = new ArrayList<>();
    private TextView TitleManagerTextView = null;
    private ListView FileList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select_dialog);
        TitleManagerTextView = (TextView)findViewById(R.id.TitleManagerTextView);
        FileList = (ListView)findViewById(R.id.FileList);
        FileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String SelectedFileString = _DirEntriesFullPath.get(position);
                if(SelectedFileString.equals("..")) UpToOneLevel();
                else BrowseTo(new File(SelectedFileString));
            }
        });
        _CurrentDir = new File("/");
        BrowseTo(new File("/"));
    }
    @Override
    protected void onStart()
    {
        super.onStart();
    }
    private void UpToOneLevel() {
        if (_CurrentDir.getParent() != null) {
            BrowseTo(_CurrentDir.getParentFile());
        }
    }
    private void BrowseTo(final File Dir)
    {
        if (Dir.isDirectory())
        {
            _CurrentDir = Dir;
            Fill(Dir.listFiles());
            TitleManagerTextView.setText(Dir.getPath());
        }
        else
        {
            DialogInterface.OnClickListener okButtonListener = new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface arg0, int arg1)
                {
                    Toast.makeText(getApplicationContext(), Dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            };
            new AlertDialog.Builder(this)
                    .setTitle("FILE_SELECT_DIALOG")
                    .setMessage("OPEN FILE '"+ Dir.getName() + "' ?")
                    .setPositiveButton("Yes", okButtonListener)
                    .setNegativeButton("No", null)
                    .show();
        }
    }
    private void Fill(File[] files) {
        _DirEntries.clear();
        _DirEntriesFullPath.clear();

        if (_CurrentDir.getParent() != null)
        {
            _DirEntries.add("..");
            _DirEntriesFullPath.add("..");
        }
        for (File File : files)
        {
            if(File.isDirectory())
            {
                String DirName = File.getPath();
                DirName=DirName.substring(DirName.lastIndexOf('/'));
                _DirEntries.add(DirName);
            }
            else
            _DirEntries.add(File.getName());
            _DirEntriesFullPath.add(File.getPath());
        }
        ArrayAdapter<String> directoryList = new ArrayAdapter<>(this, R.layout.row, _DirEntries);
        FileList.setAdapter(directoryList);
    }
}
