package jp.or.ixqsware.deviceconnectapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
//import org.deviceconnect.profile.NetworkServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.utils.AuthProcesser;
import org.deviceconnect.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.or.ixqsware.deviceconnectapp.MainActivity;
import jp.or.ixqsware.deviceconnectapp.R;
import jp.or.ixqsware.deviceconnectapp.SmartDevice;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class DeviceFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<HashMap<String, ArrayList<SmartDevice>>> {
    private ListView deviceList;
    private TextView errorView;
    private ProgressDialog dialog = null;
    private SharedPreferences preferences;

    public static DeviceFragment newInstance(int sectionNumber) {
        DeviceFragment fragment = new DeviceFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_device, container, false);

        deviceList = (ListView) rootView.findViewById(R.id.device_list);
        errorView = (TextView) rootView.findViewById(R.id.error_view);

        Button searchButton = (Button) rootView.findViewById(R.id.device_search_button);
        searchButton.setOnClickListener(this);

        getActivity().setTitle(getString(R.string.devices_section_name));

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        deviceList.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        String ipAddress = preferences.getString(PREF_KEY_SERVER_ADDRESS, SERVER);
        int portNumber = preferences.getInt(PREF_KEY_SERVER_PORT, PORT);

        String accessToken = preferences.getString(PREF_KEY_TOKEN, null);
        if (accessToken == null) {
                getAccessToken(ipAddress, portNumber);
        } else {
            if (dialog == null) dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.loading_label));
            dialog.show();

            Bundle args = new Bundle();
            args.putString(KEY_SERVER_ADDRESS, ipAddress);
            args.putInt(KEY_SERVER_PORT, portNumber);

            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public Loader<HashMap<String, ArrayList<SmartDevice>>> onCreateLoader(int id, Bundle args) {
        DeviceDiscoverAsyncTaskLoader loader
                = new DeviceDiscoverAsyncTaskLoader(getActivity(), args);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<HashMap<String, ArrayList<SmartDevice>>> loader,
                               HashMap<String, ArrayList<SmartDevice>> data) {
        if (data.containsKey("Success")) {
            DeviceInfo adapter = new DeviceInfo(getActivity(), 0, data.get("Success"));
            deviceList.setAdapter(adapter);
            deviceList.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.GONE);
        } else {
            Iterator iterator = data.entrySet().iterator();
            Map.Entry entry = (Map.Entry) iterator.next();
            errorView.setText((String) entry.getKey());
            deviceList.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
        }
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onLoaderReset(Loader<HashMap<String, ArrayList<SmartDevice>>> loader) {

    }

    private void getAccessToken(String ipAddress, int portNumber) {
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
        }

        @Override
        public void onAuthFailed(DConnectMessage.ErrorCode error) {
        }
    };

    /**
     *
     */
    private class DeviceInfo extends ArrayAdapter<SmartDevice> {
        private LayoutInflater inflater;

        public DeviceInfo(Context context, int textViewResourceId, ArrayList<SmartDevice> objects) {
            super(context, textViewResourceId, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.device_row, null);
            }
            TextView nameView = (TextView) convertView.findViewById(R.id.device_name);

            final SmartDevice item = getItem(position);
            nameView.setText(item.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ipAddress = preferences.getString(PREF_KEY_SERVER_ADDRESS, SERVER);
                    int portNumber = preferences.getInt(PREF_KEY_SERVER_PORT, PORT);
                    String deviceId = item.getId();
                    String deviceName = item.getName();

                    Bundle args = new Bundle();
                    args.putString(KEY_SERVER_ADDRESS, ipAddress);
                    args.putInt(KEY_SERVER_PORT, portNumber);
                    args.putString(KEY_DEVICE_ID, deviceId);
                    args.putString(KEY_DEVICE_NAME, deviceName);

                    MainActivity activity = (MainActivity) getActivity();
                    activity.showServiceFragment(args);
                }
            });

            return convertView;
        }
    }

    /**
     *
     */
    public static class DeviceDiscoverAsyncTaskLoader
            extends AsyncTaskLoader<HashMap<String, ArrayList<SmartDevice>>> {
        private String ipAddress;
        private int portNumber;
        private String accessToken;

        public DeviceDiscoverAsyncTaskLoader(Context context, Bundle args) {
            super(context);
            ipAddress = args.getString(KEY_SERVER_ADDRESS);
            portNumber = args.getInt(KEY_SERVER_PORT);
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(context);
            accessToken = sharedPreferences.getString(PREF_KEY_TOKEN, null);
        }

        @Override
        public HashMap<String, ArrayList<SmartDevice>> loadInBackground() {
            HashMap<String, ArrayList<SmartDevice>> mapDevices = new HashMap<>();
            ArrayList<SmartDevice> arrDevices = new ArrayList<>();

            DConnectMessage message;
            try {
                URIBuilder builder = new URIBuilder();
                /*
                builder.setProfile(NetworkServiceDiscoveryProfileConstants.PROFILE_NAME);
                builder.setAttribute(NetworkServiceDiscoveryProfileConstants.ATTRIBUTE_GET_NETWORK_SERVICES);
                */
                builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
                builder.setScheme("http");
                builder.setHost(ipAddress);
                builder.setPort(portNumber);
                builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);

                HttpUriRequest request = new HttpGet(builder.build());
                DefaultHttpClient mClient = new DefaultHttpClient();
                HttpResponse response = mClient.execute(request);
                message = (new HttpMessageFactory()).newDConnectMessage(response);
            } catch (URISyntaxException e) {
                mapDevices.put("Error: " + e.getMessage(), null);
                return mapDevices;
            } catch (IOException e) {
                mapDevices.put("Error: " + e.getMessage(), null);
                return mapDevices;
            }

            if (message == null) {
                mapDevices.put("Error: No response.", null);
                return mapDevices;
            }

            int result = message.getInt(DConnectMessage.EXTRA_RESULT);
            if (result == DConnectMessage.RESULT_ERROR) {
                mapDevices.put(
                        "Error: " + message.getString(DConnectMessage.EXTRA_ERROR_MESSAGE),
                        null);
                return mapDevices;
            }
            /*
            List<Object> services = message.getList(
                    NetworkServiceDiscoveryProfileConstants.PARAM_SERVICES);
             */
            List<Object> services = message.getList(
                    ServiceDiscoveryProfileConstants.PARAM_SERVICES);
            if (services != null) {
                for (Object object: services) {
                    Map<String, Object> service = (Map<String, Object>) object;
                    SmartDevice device = new SmartDevice(
                            /*
                            service.get(NetworkServiceDiscoveryProfileConstants.PARAM_ID).toString(),
                            service.get(NetworkServiceDiscoveryProfileConstants.PARAM_NAME).toString());
                             */
                            service.get(ServiceDiscoveryProfileConstants.PARAM_ID).toString(),
                            service.get(ServiceDiscoveryProfileConstants.PARAM_NAME).toString());
                    arrDevices.add(device);
                }
                mapDevices.put("Success", arrDevices);
            } else {
                mapDevices.put("Error: No plugins.", null);
            }
            return mapDevices;
        }
    }
}
