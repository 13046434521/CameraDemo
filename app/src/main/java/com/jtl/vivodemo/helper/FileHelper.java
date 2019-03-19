package com.jtl.vivodemo.helper;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;

/**
 * @作者：jtl
 * @时间：
 * @描述：文件工具类
 */
public class FileHelper {
    private static String mSDCardFolderPath;
    private static String mImgFolderPath;
    private static String mLogFolderPath;
    private static String mDataFolderPath;

    private FileHelper() {
    }

    public static FileHelper getInstance() {
        return FileHelperHolder.sFileHelper;
    }

    public void init() {
        mSDCardFolderPath = getSDCardFolderPath();
        mImgFolderPath = getImgFolderPath();
        mLogFolderPath = getLogFolderPath();
        mDataFolderPath = getDataFolderPath();
    }

    public String getSDCardFolderPath() {
        if (TextUtils.isEmpty(mSDCardFolderPath)) {
            mSDCardFolderPath = Environment.getExternalStorageDirectory().getPath() + "/IMI";
        }
        mkdirs(mSDCardFolderPath);

        return mSDCardFolderPath;
    }

    public String getImgFolderPath() {
        if (TextUtils.isEmpty(mImgFolderPath)) {
            mImgFolderPath = getSDCardFolderPath() + "/Img/";
        }
        mkdirs(mImgFolderPath);

        return mImgFolderPath;
    }

    public String getLogFolderPath() {
        if (TextUtils.isEmpty(mLogFolderPath)) {
            mLogFolderPath = getSDCardFolderPath() + "/Log/";
        }
        mkdirs(mLogFolderPath);

        return mLogFolderPath;
    }

    public String getDataFolderPath() {
        if (TextUtils.isEmpty(mDataFolderPath)) {
            mDataFolderPath = getSDCardFolderPath() + "/Data/";
        }
        mkdirs(mDataFolderPath);

        return mDataFolderPath;
    }

    public File mkdirs(@NonNull String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    //true为存在，false为不存在
    public boolean mkdir(String path) {
        File file = new File(path);

        return file.exists();
    }

    private static class FileHelperHolder {
        private static FileHelper sFileHelper = new FileHelper();
    }
}
