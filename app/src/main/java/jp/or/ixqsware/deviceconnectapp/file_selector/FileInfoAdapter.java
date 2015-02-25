package jp.or.ixqsware.deviceconnectapp.file_selector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import jp.or.ixqsware.deviceconnectapp.R;

/**
 * Created by hnakadate on 15/02/21.
 */
public class FileInfoAdapter extends ArrayAdapter<FileInfo> {
    private List<FileInfo> listFileInfos;
    private LayoutInflater inflater;

    public FileInfoAdapter(Context context, List<FileInfo> objects) {
        super(context, -1, objects);
        this.listFileInfos = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public FileInfo getItem(int position) {
        return listFileInfos.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.file_item, null);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.file_name_view);

        FileInfo fileInfo = listFileInfos.get(position);
        if (fileInfo.getFile().isDirectory()) {
            nameView.setText(fileInfo.getFileName() + "/");
        } else {
            nameView.setText(fileInfo.getFileName());
        }

        return convertView;
    }
}
