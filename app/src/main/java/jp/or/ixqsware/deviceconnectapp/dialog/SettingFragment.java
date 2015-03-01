package jp.or.ixqsware.deviceconnectapp.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import jp.or.ixqsware.deviceconnectapp.R;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class SettingFragment extends DialogFragment
        implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private EditText editAddress;
    private EditText editPort;
    private Spinner spinXml;
    private EditText editXml;
    private SharedPreferences preferences;
    private LinearLayout urlArea;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Settings");
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        editAddress = (EditText) dialogView.findViewById(R.id.address_edit);
        editPort = (EditText) dialogView.findViewById(R.id.port_edit);
        spinXml = (Spinner) dialogView.findViewById(R.id.path_spin);
        editXml = (EditText) dialogView.findViewById(R.id.path_edit);
        urlArea = (LinearLayout) dialogView.findViewById(R.id.url_area);

        Button saveButton = (Button) dialogView.findViewById(R.id.save_button);
        Button cancelButton = (Button) dialogView.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        editAddress.setText(preferences.getString(PREF_KEY_SERVER_ADDRESS, SERVER));
        editPort.setText(Integer.toString(preferences.getInt(PREF_KEY_SERVER_PORT, PORT)));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.xml_path));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinXml.setAdapter(adapter);
        spinXml.setOnItemSelectedListener(this);

        String xmlSource = preferences.getString(PREF_KEY_XML_SOURCE, getString(R.string.xml_asset));
        if (getString(R.string.xml_url).equals(xmlSource)) {
            spinXml.setSelection(1);
            editXml.setText(preferences.getString(PREF_KEY_XML_PATH, "http://"));
            urlArea.setVisibility(View.VISIBLE);
        }
        return dialogView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dialog dialog = getDialog();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.width = (int) (metrics.widthPixels * 0.9);
        layoutParams.height = (int) (metrics.heightPixels * 0.9);

        dialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        String item = (String) spinner.getSelectedItem();
        if (getString(R.string.xml_asset).equals(item)) {
            urlArea.setVisibility(View.GONE);
        } else if (getString(R.string.xml_url).equals(item)) {
            urlArea.setVisibility(View.VISIBLE);
            editXml.setText(preferences.getString(PREF_KEY_XML_PATH, "http://"));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.save_button:
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(
                        PREF_KEY_SERVER_ADDRESS,
                        editAddress.getText().toString()
                );
                editor.putInt(
                        PREF_KEY_SERVER_PORT,
                        Integer.parseInt(editPort.getText().toString())
                );
                String xmlSource = (String) spinXml.getSelectedItem();
                editor.putString(
                        PREF_KEY_XML_SOURCE,
                        xmlSource
                );
                if (getString(R.string.xml_url).equals(xmlSource)) {
                    editor.putString(
                            PREF_KEY_XML_PATH,
                            editXml.getText().toString()
                    );
                }
                editor.apply();
                break;

            case R.id.cancel_button:
                break;
        }
        dismiss();
    }
}
