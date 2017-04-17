package com.jcp83.telegraph;

public class FilePath
{
    public static String Get(String _Path, int _Index)
    {
        String _PathWFN = "";
        String _PathWFE = _Path;
        if(_Path.lastIndexOf('/')>=0)
        {
            _PathWFN=_Path.substring(0, _Path.lastIndexOf('/')+1);
            _PathWFE=_Path.substring(_Path.lastIndexOf('/')+1);
        }
        String _PathFE = "";
        if(_PathWFE.lastIndexOf('.')>0)
        {
            _PathFE = _PathWFE.substring(_PathWFE.lastIndexOf('.')+1);
            _PathWFE = _PathWFE.substring(0, _PathWFE.lastIndexOf('.'));
        }
        String _Res = _PathWFN+_PathWFE+" ("+_Index+")";
        if(_PathFE.isEmpty()) return _Res+_PathFE;
        else return _Res+'.'+_PathFE;
    }
}
