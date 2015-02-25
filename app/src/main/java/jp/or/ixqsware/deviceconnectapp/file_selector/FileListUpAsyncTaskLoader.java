package jp.or.ixqsware.deviceconnectapp.file_selector;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static jp.or.ixqsware.deviceconnectapp.Constants.KEY_FILE_PATH;

/**
 * Created by hnakadate on 15/02/21.
 */
public class FileListUpAsyncTaskLoader extends AsyncTaskLoader<List<File>> {
    private String filePath;

    public FileListUpAsyncTaskLoader(Context context, Bundle args) {
        super(context);
        this.filePath = args.getString(KEY_FILE_PATH, File.separator);
    }

    @Override
    public List<File> loadInBackground() {
        List<File> lstFiles = new ArrayList<>();
        File[] files = new File(filePath).listFiles();
        if (files != null) {
            for (File file : files) {
                lstFiles.add(file);
            }
        }
        return lstFiles;
    }
}
