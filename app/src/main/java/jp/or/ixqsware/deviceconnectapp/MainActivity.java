package jp.or.ixqsware.deviceconnectapp;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.AuthProcesser;

import java.util.Calendar;

import jp.or.ixqsware.deviceconnectapp.fragment.DeviceFragment;
import jp.or.ixqsware.deviceconnectapp.fragment.OperationFragment;
import jp.or.ixqsware.deviceconnectapp.fragment.PermissionFragment;
import jp.or.ixqsware.deviceconnectapp.fragment.ServiceFragment;
import jp.or.ixqsware.deviceconnectapp.dialog.SettingFragment;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DeviceFragment fragment = DeviceFragment.newInstance(DEVICE_SECTION);
        //PluginsFragment fragment = PluginsFragment.newInstance(DEVICE_SECTION);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                SettingFragment settingFragment = new SettingFragment();
                settingFragment.show(getFragmentManager(), "Settings");
                return true;

            case R.id.action_token:
                String appName = getPackageName();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String ipAddress = preferences.getString(PREF_KEY_SERVER_ADDRESS, SERVER);
                int portNumber = preferences.getInt(PREF_KEY_SERVER_PORT, PORT);

                String prefPermissions = preferences.getString(PREF_KEY_PERMISSIONS, "");
                String[] lstPermissions;
                if (prefPermissions.length() == 0) {
                    lstPermissions = getResources().getStringArray(R.array.permissions);
                } else {
                    lstPermissions = prefPermissions.split(";");
                }
                AuthProcesser.asyncAuthorize(
                        ipAddress,
                        portNumber,
                        false,
                        getPackageName(),
                        appName,
                        lstPermissions,
                        authHandler);
                break;

            case R.id.action_permission:
                showPermissionFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTitle(String title) {
        getActionBar().setTitle(title);
    }

    public void showPermissionFragment() {
        PermissionFragment fragment = PermissionFragment.newInstance(PERMISSION_SECTION);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showServiceFragment(Bundle args) {
        ServiceFragment fragment = ServiceFragment.newInstance(SERVICE_SECTION);
        fragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showOperationFragment(Bundle args) {
        OperationFragment fragment = OperationFragment.newInstance(OPERATION_SECTION);
        fragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private AuthProcesser.AuthorizationHandler authHandler
            = new AuthProcesser.AuthorizationHandler() {
        @Override
        public void onAuthorized(String clientId, String clientSecret, String accessToken_) {
            Calendar calendar = Calendar.getInstance();
            SharedPreferences preferences
                    = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_KEY_TOKEN, accessToken_);
            editor.putLong(PREF_KEY_TOKEN_GET, calendar.getTimeInMillis());
            editor.putString(PREF_KEY_CLIENT_ID, clientId);
            editor.putString(PREF_KEY_CLIENT_SECRET, clientSecret);
            editor.apply();
        }

        @Override
        public void onAuthFailed(DConnectMessage.ErrorCode error) {
        }
    };
}
