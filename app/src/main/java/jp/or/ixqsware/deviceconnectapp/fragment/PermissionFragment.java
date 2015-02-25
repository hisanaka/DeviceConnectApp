package jp.or.ixqsware.deviceconnectapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.or.ixqsware.deviceconnectapp.R;
import jp.or.ixqsware.deviceconnectapp.view.SortableListView;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class PermissionFragment extends Fragment {
    private SharedPreferences preferences;
    private SortableListView sortableView;
    private int posStart = -1;
    private ArrayList<String> arrPermissions;
    private Fragment permissionFragment;
    private PermissionsAdapter adapter;

    public static PermissionFragment newInstance(int sectionNumber) {
        PermissionFragment instance = new PermissionFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_SECTION_NUMBER, sectionNumber);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_permission, null);
        setHasOptionsMenu(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        sortableView = (SortableListView) rootView.findViewById(R.id.permission_list);
        sortableView.setDragListener(new DragListener());
        sortableView.setSortable(true);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        permissionFragment = this;

        String prefPermissions = preferences.getString(PREF_KEY_PERMISSIONS, "");
        String[] lstPermissions;
        if (prefPermissions.length() == 0) {
            lstPermissions = getResources().getStringArray(R.array.permissions);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_KEY_PERMISSIONS, list2String(lstPermissions));
            editor.apply();
        } else {
            lstPermissions = prefPermissions.split(";");
        }
        arrPermissions = new ArrayList<>(Arrays.asList(lstPermissions));
        adapter = new PermissionsAdapter(getActivity(), 0, arrPermissions);
        sortableView.setAdapter(adapter);
        sortableView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                args.putInt(KEY_PERMISSION_INDEX, position);
                args.putString(KEY_PERMISSION, adapter.getItem(position));
                EditDialog editDialog = new EditDialog();
                editDialog.setTargetFragment(permissionFragment, 2);
                editDialog.setArguments(args);
                editDialog.show(getFragmentManager(), "Edit");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        String prefPermissions = preferences.getString(PREF_KEY_PERMISSIONS, "");
        String[] lstPermissions;
        if (prefPermissions.length() == 0) {
            lstPermissions = getResources().getStringArray(R.array.permissions);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_KEY_PERMISSIONS, list2String(lstPermissions));
            editor.apply();
        } else {
            lstPermissions = prefPermissions.split(";");
        }
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(lstPermissions));
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case 0:     // ADD
                String newOne = bundle.getString(KEY_PERMISSION);
                if (newOne.length() > 0) {
                    arrayList.add(newOne);
                }
                break;

            case 1:     // Delete
                arrayList.remove(bundle.getString(KEY_PERMISSION));
                break;

            case 2:     // Edit
                arrayList.set(
                        bundle.getInt(KEY_PERMISSION_INDEX),
                        bundle.getString(KEY_PERMISSION)
                );
                break;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(
                PREF_KEY_PERMISSIONS,
                list2String(arrayList.toArray(new String[arrayList.size()]))
        );
        editor.apply();

        adapter.clear();
        adapter.addAll(arrayList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_permission, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_permission:
                AddDialog addDialog = new AddDialog();
                addDialog.setTargetFragment(permissionFragment, 1);
                addDialog.show(getFragmentManager(), "Add");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String list2String(String[] list) {
        StringBuilder sb = new StringBuilder();
        for (String str : list) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * カスタムアダプター
     */
    private class PermissionsAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;

        public PermissionsAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String permission = (String) getItem(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.permission_row, null);
            }

            TextView nameView = (TextView) convertView.findViewById(R.id.permission_label);
            nameView.setText(permission);
            nameView.setVisibility(position == posStart ? View.INVISIBLE : View.VISIBLE);

            ImageButton delButton = (ImageButton) convertView.findViewById(R.id.del_button);
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_PERMISSION, permission);
                    DialogFragment deleteFragment = new DeleteDialog();
                    deleteFragment.setTargetFragment(permissionFragment, 0);
                    deleteFragment.setArguments(bundle);
                    deleteFragment.show(getFragmentManager(), "Delete");
                }
            });

            return convertView;
        }
    }

    /**
     * 新パーミッション追加用ダイアログ
     */
    public static class AddDialog extends DialogFragment {
        private EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater
                = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.dialog_add, null);
            editText = (EditText) dialogView.findViewById(R.id.edit_view);
            builder.setView(dialogView);
            builder.setMessage(getString(R.string.enter_text))
                    .setPositiveButton(
                            getString(R.string.ok_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    Bundle args = new Bundle();
                                    args.putString(KEY_PERMISSION, editText.getText().toString());
                                    intent.putExtras(args);
                                    getTargetFragment().onActivityResult(
                                            0,
                                            Activity.RESULT_OK,
                                            intent
                                    );
                                }
                            }
                    )
                    .setNegativeButton(
                            getString(R.string.cancel_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getTargetFragment().onActivityResult(
                                            0,
                                            Activity.RESULT_CANCELED,
                                            new Intent()
                                    );
                                }
                            }
                    );

            return builder.create();
        }
    }

    /**
     * パーミッション削除確認ダイアログ
     */
    public static class DeleteDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String permission = getArguments().getString(KEY_PERMISSION);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.del_conf, permission))
                    .setPositiveButton(
                            getString(R.string.ok_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Bundle args = new Bundle();
                                    args.putString(KEY_PERMISSION, permission);
                                    Intent intent = new Intent();
                                    intent.putExtras(args);
                                    getTargetFragment().onActivityResult(
                                            1,
                                            Activity.RESULT_OK,
                                            intent
                                    );
                                }
                            }
                    )
                    .setNegativeButton(
                            getString(R.string.cancel_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getTargetFragment().onActivityResult(
                                            1,
                                            Activity.RESULT_CANCELED,
                                            new Intent()
                                    );
                                }
                            }
                    );
            return builder.create();
        }
    }

    /**
     * パーミッション修正ダイアログ
     */
    public static class EditDialog extends DialogFragment {
        private EditText editText;
        private int index;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater
                    = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.dialog_add, null);
            editText = (EditText) dialogView.findViewById(R.id.edit_view);

            index = getArguments().getInt(KEY_PERMISSION_INDEX);
            String oldOne = getArguments().getString(KEY_PERMISSION);
            editText.setText(oldOne);

            builder.setView(dialogView);
            builder.setMessage(getString(R.string.enter_text))
                    .setPositiveButton(
                            getString(R.string.ok_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    Bundle args = new Bundle();
                                    args.putInt(KEY_PERMISSION_INDEX, index);
                                    args.putString(KEY_PERMISSION, editText.getText().toString());
                                    intent.putExtras(args);
                                    getTargetFragment().onActivityResult(
                                            2,
                                            Activity.RESULT_OK,
                                            intent
                                    );
                                }
                            }
                    )
                    .setNegativeButton(
                            getString(R.string.cancel_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getTargetFragment().onActivityResult(
                                            2,
                                            Activity.RESULT_CANCELED,
                                            new Intent()
                                    );
                                }
                            }
                    );

            return builder.create();
        }
    }

    /**
     * ドラッグリスナー
     */
    public class DragListener extends SortableListView.SimpleDragListener {
        @Override
        public int onStartDrag(int position) {
            posStart = position;
            sortableView.invalidateViews();
            return position;
        }

        @Override
        public int onDuringDrag(int positionFrom, int positionTo) {
            if (positionFrom < 0 || positionTo < 0 || positionFrom == positionTo) {
                return positionFrom;
            }

            int i;
            int min;
            int max;
            if (positionFrom < positionTo) {
                min = positionFrom;
                max = positionTo;
                final String data = arrPermissions.get(min);
                i = min;
                while (i < max) {
                    arrPermissions.set(i, arrPermissions.get(++i));
                }
                arrPermissions.set(max, data);
            } else if (positionFrom > positionTo) {
                min = positionTo;
                max = positionFrom;
                final String data = arrPermissions.get(max);
                i = max;
                while (i > min) {
                    arrPermissions.set(i, arrPermissions.get(--i));
                }
                arrPermissions.set(min, data);
            }
            posStart = positionTo;
            sortableView.invalidateViews();
            return positionTo;
        }

        @Override
        public boolean onStopDrag(int positionFrom, int positionTo) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(
                    PREF_KEY_PERMISSIONS,
                    list2String(arrPermissions.toArray(new String[arrPermissions.size()]))
            );
            editor.apply();

            posStart = -1;
            sortableView.invalidateViews();

            return super.onStopDrag(positionFrom, positionTo);
        }
    }
}
