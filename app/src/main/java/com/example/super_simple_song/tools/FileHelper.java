package com.example.super_simple_song.tools;

import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

public class FileHelper {
    public static int SUCCESS = 0;
    public static int CANNOT_FIND_FILE = SUCCESS + 1;
    public static int PERMISSION_DENIED = SUCCESS + 2;

    /**
     * 获取sd卡上filepath文件夹里面的所有后缀名为suffix的文件名，并保存到resultlist里面
     */
    public static int getFileList(String filepath, @NonNull String suffix, List<String> resultlist)
    {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory(); // 获得SD卡路径
            String sdPath = path.toString() + filepath;
            File songsfile = new File(sdPath);
            if(null == songsfile)
            {
                //错误提示：找不到目录
                return CANNOT_FIND_FILE;
            }
            else
            {
                File[] files = songsfile.listFiles();// 读取
                getFileName(files, resultlist,suffix);
            }
        }
        else
        {
            //没权限，处理
            return PERMISSION_DENIED;
        }
        return SUCCESS;
    }

    private static void getFileName(File[] files, List<String> list, @NonNull String suffix) {
        if(null == list)
            return;
        if (files != null)// 先判断目录是否为空，否则会报空指针
        {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFileName(file.listFiles(),list, suffix);
                } else {
                    String fileName = file.getName();
                    if (null != fileName && fileName.endsWith(suffix)) {
                        list.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }
}
