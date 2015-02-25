package jp.or.ixqsware.deviceconnectapp.file_selector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.or.ixqsware.deviceconnectapp.R;
import static jp.or.ixqsware.deviceconnectapp.Constants.*;

/**
 * Created by hnakadate on 15/02/19.
 */
public class FileSelectDialog extends DialogFragment
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<File>> {
    private FileInfoAdapter adapter;
    private ListView fileListView;
    private ProgressBar progressBar;
    private String previousDir = File.separator;
    private String pathField;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater
                = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View dialogView = inflater.inflate(R.layout.dialog_file_select, null);
        progressBar = (ProgressBar) dialogView.findViewById(R.id.progress_bar);
        fileListView = (ListView) dialogView.findViewById(R.id.file_list);

        fileListView.setOnItemClickListener(this);

        builder.setView(dialogView);

        pathField = getArguments().getString(KEY_PATH_FIELD);

        Bundle args = new Bundle();
        args.putString(KEY_FILE_PATH, File.separator);
        getLoaderManager().restartLoader(1, args, this);

        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo info = adapter.getItem(position);
        if (info.getFileName().equals(File.separator)) { return; }

        if (info.getFile().isDirectory()) {
            previousDir = info.getFile().getParent();
            if (previousDir == null) { previousDir = File.separator; }
            Bundle args = new Bundle();
            args.putString(KEY_FILE_PATH, info.getFile().getAbsolutePath());
            getLoaderManager().restartLoader(1, args, this);
        } else {
            Bundle args = new Bundle();
            args.putString(KEY_PATH_FIELD, pathField);
            args.putString(KEY_FILE_PATH, info.getFile().getAbsolutePath());

            Intent intent = new Intent();
            intent.putExtras(args);
            getTargetFragment().onActivityResult(0, Activity.RESULT_OK, intent);
            dismiss();
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        FileListUpAsyncTaskLoader loader = new FileListUpAsyncTaskLoader(getActivity(), args);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        List<FileInfo> infos = new ArrayList<>();
        infos.add(new FileInfo("..", new File(previousDir)));
        if (data.size() > 0) {
            for (File file : data) {
                infos.add(new FileInfo(file.getName(), file));
            }
            Collections.sort(infos);
        }
        adapter = new FileInfoAdapter(getActivity(), infos);
        fileListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {

    }
}
