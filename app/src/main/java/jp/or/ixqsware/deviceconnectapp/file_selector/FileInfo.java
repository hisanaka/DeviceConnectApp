package jp.or.ixqsware.deviceconnectapp.file_selector;

import java.io.File;

/**
 * Created by hnakadate on 15/02/21.
 */
public class FileInfo implements Comparable<FileInfo> {

    private String fileName;
    private File mFile;

    public FileInfo(String mName, File file) {
        this.fileName = mName;
        mFile = file;
    }

    public String getFileName() {
        return this.fileName;
    }

    public File getFile() {
        return this.mFile;
    }

    @Override
    public int compareTo(FileInfo another) {
        if (mFile.isDirectory() && !another.getFile().isDirectory()) {
            return -1;
        } else if (!mFile.isDirectory() && another.getFile().isDirectory()) {
            return 1;
        } else {
            return mFile.getName().toLowerCase().compareTo(another.getFileName().toLowerCase());
        }
    }
}
