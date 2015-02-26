package jp.or.ixqsware.deviceconnectapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import jp.or.ixqsware.deviceconnectapp.R;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class SettingFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText editAddress;
    private EditText editPort;
    private Spinner spinXml;
    private EditText editXml;
    private SharedPreferences preferences;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater
                = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        editAddress = (EditText) dialogView.findViewById(R.id.address_edit);
        editPort = (EditText) dialogView.findViewById(R.id.port_edit);
        spinXml = (Spinner) dialogView.findViewById(R.id.path_spin);
        editXml = (EditText) dialogView.findViewById(R.id.path_edit);

        editAddress.setText(preferences.getString(PREF_KEY_SERVER_ADDRESS, SERVER));
        editPort.setText(Integer.toString(preferences.getInt(PREF_KEY_SERVER_PORT, PORT)));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
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
            editXml.setVisibility(View.VISIBLE);
        }

        builder.setView(dialogView);
        builder.setMessage(getString(R.string.server_settings))
                .setPositiveButton(
                        getString(R.string.ok_button_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                        xmlSource);
                                if (getString(R.string.xml_url).equals(xmlSource)) {
                                    editor.putString(
                                            PREF_KEY_XML_PATH,
                                            editXml.getText().toString()
                                    );
                                }
                                editor.apply();
                            }
                        }
                )
                .setNegativeButton(
                        getString(R.string.cancel_button_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );
        return builder.create();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        String item = (String) spinner.getSelectedItem();
        if (getString(R.string.xml_asset).equals(item)) {
            editXml.setVisibility(View.GONE);
        } else if (getString(R.string.xml_url).equals(item)) {
            editXml.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
