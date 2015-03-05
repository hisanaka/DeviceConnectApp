package jp.or.ixqsware.deviceconnectapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.utils.AuthProcesser;
import org.deviceconnect.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.or.ixqsware.deviceconnectapp.MainActivity;
import jp.or.ixqsware.deviceconnectapp.R;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class ServiceFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<String>> {
    private ListView serviceList;
    private TextView errorView;
    private ProgressDialog dialog;
    private String accessToken;
    private String clientId;
    private String clientSecret;
    private SharedPreferences preferences;
    private String ipAddress;
    private int portNumber;
    private String deviceId;
    private String deviceName;

    public static ServiceFragment newInstance(int sectionNumber) {
        ServiceFragment fragment = new ServiceFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity);}

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_service, container, false);
        serviceList = (ListView) rootView.findViewById(R.id.profile_list);
        errorView = (TextView) rootView.findViewById(R.id.error_view);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ipAddress = getArguments().getString(KEY_SERVER_ADDRESS, SERVER);
        portNumber = getArguments().getInt(KEY_SERVER_PORT, PORT);
        deviceId = getArguments().getString(KEY_DEVICE_ID, null);
        deviceName = getArguments().getString(KEY_DEVICE_NAME, null);

        getActivity().setTitle(getString(R.string.profiles_section_name) + "(" + deviceName + ")");

        if (deviceId == null) {
            errorView.setText("Invalid device ID.");
            errorView.setVisibility(View.VISIBLE);
        } else {
            if (dialog == null) { dialog = new ProgressDialog(getActivity()); }
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.loading_label));
            dialog.show();

            Bundle args = new Bundle();
            args.putString(KEY_SERVER_ADDRESS, ipAddress);
            args.putInt(KEY_SERVER_PORT, portNumber);
            args.putString(KEY_DEVICE_ID, deviceId);
            getLoaderManager().restartLoader(0, args, this);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        ServiceDiscoverAsyncTaskLoader loader
                = new ServiceDiscoverAsyncTaskLoader(getActivity(), args);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {
        ServiceInfoAdapter adapter = new ServiceInfoAdapter(getActivity(), 0, data);
        serviceList.setAdapter(adapter);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        accessToken = preferences.getString(PREF_KEY_TOKEN, null);
        long getTime = preferences.getLong(PREF_KEY_TOKEN_GET, 0);
        clientId = preferences.getString(PREF_KEY_CLIENT_ID, null);
        clientSecret = preferences.getString(PREF_KEY_CLIENT_SECRET, null);

        if (accessToken == null || clientId == null || clientSecret == null) {
            getAccessToken();
        } else {
            Calendar calendar = Calendar.getInstance();
            long elapseTimeInMillis = calendar.getTimeInMillis() - getTime;
            long elapseDate = elapseTimeInMillis / (24 * 60 * 60 * 1000);
            if (elapseDate > 60) {
                getAccessToken();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {

    }

    private void getAccessToken() {
        String appName = getResources().getString(R.string.app_name);
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
                getActivity().getPackageName(),
                appName,
                lstPermissions,
                authHandler
        );
    }

    /**
     *
     */
    public class ServiceInfoAdapter extends ArrayAdapter<String> implements View.OnClickListener {
        private LayoutInflater inflater;

        public ServiceInfoAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.device_row, null);
            }
            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
            nameView.setText(getItem(position));

            convertView.setOnClickListener(this);
            return convertView;
        }

        @Override
        public void onClick(View v) {
            TextView nameView = (TextView) v.findViewById(R.id.device_name);
            String serviceName =  nameView.getText().toString();

            Bundle args = new Bundle();
            args.putString(KEY_SERVER_ADDRESS, ipAddress);
            args.putInt(KEY_SERVER_PORT, portNumber);
            args.putString(KEY_DEVICE_NAME, deviceName);
            args.putString(KEY_DEVICE_ID, deviceId);
            args.putString(KEY_PROFILE_NAME, serviceName);

            MainActivity activity = (MainActivity) getActivity();
            activity.showOperationFragment(args);
        }
    }

    private AuthProcesser.AuthorizationHandler authHandler
            = new AuthProcesser.AuthorizationHandler() {
        @Override
        public void onAuthorized(String clientId, String clientSecret, String accessToken_) {
            Calendar calendar = Calendar.getInstance();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_KEY_TOKEN, accessToken_);
            editor.putLong(PREF_KEY_TOKEN_GET, calendar.getTimeInMillis());
            editor.putString(PREF_KEY_CLIENT_ID, clientId);
            editor.putString(PREF_KEY_CLIENT_SECRET, clientSecret);
            editor.apply();

            accessToken = accessToken_;
        }

        @Override
        public void onAuthFailed(DConnectMessage.ErrorCode error) {
        }
    };

    /**
     *
     */
    public static class ServiceDiscoverAsyncTaskLoader extends AsyncTaskLoader<ArrayList<String>> {
        private String deviceId;
        private String ipAddress;
        private int portNumber;
        private String accessToken;

        public ServiceDiscoverAsyncTaskLoader(Context context, Bundle args) {
            super(context);

            this.ipAddress = args.getString(KEY_SERVER_ADDRESS);
            this.portNumber = args.getInt(KEY_SERVER_PORT);
            this.deviceId = args.getString(KEY_DEVICE_ID);

            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(context);
            accessToken = sharedPreferences.getString(PREF_KEY_TOKEN, null);
        }

        @Override
        public ArrayList<String> loadInBackground() {
            ArrayList<String> arrServices = new ArrayList<>();
            DConnectMessage message;

            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setProfile(ServiceInformationProfileConstants.PROFILE_NAME);
            uriBuilder.setScheme("http");
            uriBuilder.setHost(ipAddress);
            uriBuilder.setPort(portNumber);
            uriBuilder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, deviceId);
            uriBuilder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
            /* 旧バージョン
            uriBuilder.setProfile(SystemProfileConstants.PROFILE_NAME);
            uriBuilder.setAttribute(SystemProfileConstants.ATTRIBUTE_DEVICE);
            uriBuilder.setScheme("http");
            uriBuilder.setHost(ipAddress);
            uriBuilder.setPort(portNumber);
            uriBuilder.addParameter(DConnectMessage.EXTRA_DEVICE_ID, deviceId);
            uriBuilder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, null);
            */

            try {
                HttpUriRequest request = new HttpGet(uriBuilder.build());
                DefaultHttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(request);
                message = (new HttpMessageFactory()).newDConnectMessage(response);
            } catch (IOException e) {
                arrServices.add(e.getMessage());
                return arrServices;
            } catch (URISyntaxException e) {
                arrServices.add(e.getMessage());
                return arrServices;
            }
            if (message == null) {
                arrServices.add("Error: No response.");
                return arrServices;
            }

            int result = message.getInt(DConnectMessage.EXTRA_RESULT);
            if (result == DConnectMessage.RESULT_ERROR) {
                arrServices.add("Error: " + message.getString(DConnectMessage.EXTRA_ERROR_MESSAGE));
                return arrServices;
            }
            List<Object> services = message.getList(SystemProfileConstants.PARAM_SUPPORTS);
            if (services != null) {
                for (Object object : services) {
                    arrServices.add((String) object);
                }
            } else {
                arrServices.add("No services.");
            }
            return arrServices;
        }
    }
}
