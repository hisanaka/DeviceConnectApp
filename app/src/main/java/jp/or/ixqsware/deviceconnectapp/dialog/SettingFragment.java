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
import android.widget.EditText;

import jp.or.ixqsware.deviceconnectapp.R;

import static jp.or.ixqsware.deviceconnectapp.Constants.PORT;
import static jp.or.ixqsware.deviceconnectapp.Constants.PREF_KEY_SERVER_ADDRESS;
import static jp.or.ixqsware.deviceconnectapp.Constants.PREF_KEY_SERVER_PORT;
import static jp.or.ixqsware.deviceconnectapp.Constants.SERVER;

public class SettingFragment extends DialogFragment {
    private EditText editAddress;
    private EditText editPort;
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

        editAddress.setText(preferences.getString(PREF_KEY_SERVER_ADDRESS, SERVER));
        editPort.setText(Integer.toString(preferences.getInt(PREF_KEY_SERVER_PORT, PORT)));

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
}
