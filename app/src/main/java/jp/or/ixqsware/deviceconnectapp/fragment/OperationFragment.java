package jp.or.ixqsware.deviceconnectapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.event.EventHandler;
import org.deviceconnect.message.http.event.CloseHandler;
import org.deviceconnect.message.http.event.HttpEventManager;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.or.ixqsware.deviceconnectapp.ControlObject;
import jp.or.ixqsware.deviceconnectapp.LoadInfoAsyncTaskLoader;
import jp.or.ixqsware.deviceconnectapp.R;
import jp.or.ixqsware.deviceconnectapp.file_selector.FileSelectDialog;

import static jp.or.ixqsware.deviceconnectapp.Constants.*;

public class OperationFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<HashMap<Integer, ArrayList<ControlObject>>> {
    private TextView resultView;
    private LinearLayout operationView;
    private String ipAddress;
    private int portNumber;
    private String deviceId;
    private String profileName;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog = null;
    private Fragment mFragment;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            resultView.setText((String) message.obj);
        }
    };

    public static OperationFragment newInstance(int sectionNumber) {
        OperationFragment fragment = new OperationFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public OperationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_operation, container, false);
        setHasOptionsMenu(true);

        operationView = (LinearLayout) rootView.findViewById(R.id.operation_area);
        resultView = (TextView) rootView.findViewById(R.id.result_area);

        mFragment = this;

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        ipAddress = bundle.getString(KEY_SERVER_ADDRESS, SERVER);
        portNumber = bundle.getInt(KEY_SERVER_PORT, PORT);
        deviceId = bundle.getString(KEY_DEVICE_ID, null);
        profileName = bundle.getString(KEY_PROFILE_NAME, null);
        String deviceName = bundle.getString(KEY_DEVICE_NAME, null);

        getActivity().setTitle(profileName);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.loading_label));
        progressDialog.show();

        Bundle args = new Bundle();
        args.putString(KEY_DEVICE_NAME, deviceName);
        args.putString(KEY_PROFILE_NAME, profileName);
        getLoaderManager().restartLoader(3, args, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        HttpEventManager.INSTANCE.disconnect();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_operation, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.share_result:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, resultView.getText().toString());
                startActivity(sendIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        Bundle args = data.getExtras();
        String pathField = args.getString(KEY_PATH_FIELD);
        String mPath = args.getString(KEY_FILE_PATH);
        View view = findView((ViewGroup) getView(), pathField);
        if (view != null) {
            if (view instanceof EditText) {
                ((EditText) view).setText(mPath);
            }
        }
    }

    @Override
    public void onClick(View v) {
        executeOperation(v);
    }

    private void executeOperation(View v) {
        resultView.setText("");

        String httpMethod = (String) v.getTag(R.string.method_tag);
        String mAttribute = (String) v.getTag(R.string.interface_tag);
        if (httpMethod.length() == 0) { return; }

        HashMap<String, String> mapOptions = new HashMap<>();
        LinearLayout optionView
                = (LinearLayout) ((LinearLayout) v.getParent()).findViewById(R.id.option_view_id);
        if (optionView != null) {
            for (int i = 0; i < optionView.getChildCount(); i++) {
                LinearLayout rowView = (LinearLayout) optionView.getChildAt(i);
                View view = rowView.getChildAt(rowView.getChildCount() - 1);
                String optionName = (String) view.getTag(R.string.option_tag);
                if (view instanceof Spinner) {
                    mapOptions.put(optionName, (String) ((Spinner) view).getSelectedItem());
                } else if (view instanceof EditText) {
                    mapOptions.put(optionName, ((EditText) view).getText().toString());
                } else if (view instanceof LinearLayout) {
                    // 現在はfileが指定された場合のみ
                    String editTag = ((Button) v).getText().toString().replace(" ", "_") + "_edit";
                    EditText pathEdit = (EditText) findView((ViewGroup) view, editTag);
                    String filePath = pathEdit.getText().toString();
                    mapOptions.put(optionName, filePath);
                }
            }
        }

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http");
        uriBuilder.setHost(ipAddress);
        uriBuilder.setPort(portNumber);
        uriBuilder.setProfile(profileName);
        if (mAttribute.length() > 0) {
            if (mAttribute.contains("/")) {
                uriBuilder.setInterface(mAttribute.split("/")[0]);
                uriBuilder.setAttribute(mAttribute.split("/")[1]);
            } else {
                uriBuilder.setAttribute(mAttribute);
            }
        }

        String accessToken = preferences.getString(PREF_KEY_TOKEN, null);

        //uriBuilder.addParameter(DConnectMessage.EXTRA_DEVICE_ID, deviceId);
        uriBuilder.addParameter(DConnectMessage.EXTRA_SERVICE_ID, deviceId);
        uriBuilder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
        for (Map.Entry<String, String> stringStringEntry : mapOptions.entrySet()) {
            Map.Entry entry = (Map.Entry) stringStringEntry;
            uriBuilder.addParameter((String) entry.getKey(), (String) entry.getValue());
        }

        switch (httpMethod.toUpperCase()) {
            case "EVENT":
                String clientId = preferences.getString(PREF_KEY_CLIENT_ID, null);
                uriBuilder.addParameter(DConnectMessage.EXTRA_SESSION_KEY, clientId);
                registerService(uriBuilder);
                break;

            default:
                sendRequest(httpMethod, uriBuilder);
                break;
        }
    }

    private View findView(ViewGroup parent, String tag) {
        View view = null;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof LinearLayout || child instanceof ScrollView) {
                view = findView((ViewGroup) child, tag);
            } else if (child.getTag(R.string.path_tag) != null) {
                if (tag.equals(child.getTag(R.string.path_tag))) { view = child; }
            }
            if (view != null) { break; }
        }
        return view;
    }

    private void registerService(final URIBuilder uriBuilder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String clientId = preferences.getString(PREF_KEY_CLIENT_ID, null);
                Message msg = Message.obtain();
                try {
                    HttpResponse response;
                    boolean result = HttpEventManager.INSTANCE.connect(
                            ipAddress,
                            portNumber,
                            false,
                            clientId,
                            new CloseHandler() {
                                @Override
                                public void onClosed() {
                                }
                            }
                    );
                    if (!result) {
                        msg.obj = "Failed to open WebSocket.";
                    } else {
                        response = HttpEventManager.INSTANCE.registerEvent(
                                uriBuilder,
                                new EventHandler() {
                                    @Override
                                    public void onEvent(JSONObject event) {
                                        Message msg = Message.obtain();
                                        msg.obj = event.toString();
                                        mHandler.sendMessage(msg);
                                    }
                                }
                        );
                        msg.obj = EntityUtils.toString(response.getEntity(), "UTF-8");
                    }
                } catch (IOException e) {
                    msg.obj = e.getMessage();
                }
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private void sendRequest(final String mMethod, final URIBuilder uriBuilder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                try {
                    HttpUriRequest request = new HttpGet(uriBuilder.build());
                    switch (mMethod.toUpperCase()) {
                        case "GET":
                            request = new HttpGet(uriBuilder.build());
                            break;
                        case "PUT":
                            request = new HttpPut(uriBuilder.build());
                            break;
                        case "POST":
                            request = new HttpPost(uriBuilder.build());
                            break;
                        case "DELETE":
                            request = new HttpDelete(uriBuilder.build());
                            break;
                    }
                    DefaultHttpClient mClient = new DefaultHttpClient();
                    HttpResponse response = mClient.execute(request);
                    msg.obj = EntityUtils.toString(response.getEntity(), "UTF-8");
                } catch (IOException | URISyntaxException e) {
                    msg.obj = e.getMessage();
                }
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public Loader<HashMap<Integer, ArrayList<ControlObject>>> onCreateLoader(int id, Bundle args) {
        LoadInfoAsyncTaskLoader loader = new LoadInfoAsyncTaskLoader(getActivity(), args);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<HashMap<Integer, ArrayList<ControlObject>>> loader,
                               HashMap<Integer, ArrayList<ControlObject>> data) {
        for (int key : data.keySet()) {
            switch (key) {
                case 0:
                    createLayout(data.get(key));
                    break;

                case -1:
                    resultView.setText("Ignore file.");
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    break;

                case -2:
                    resultView.setText("XML parse error.");
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<HashMap<Integer, ArrayList<ControlObject>>> loader) {

    }

    private void createLayout(ArrayList<ControlObject> arrControls) {
        LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parentParams.setLayoutDirection(LinearLayout.VERTICAL);
        parentParams.setMargins(0, 0, 0, 16);

        LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        optionParams.setLayoutDirection(LinearLayout.HORIZONTAL);
        optionParams.setMargins(16, 8, 8, 0);

        for (ControlObject control : arrControls) {
            LinearLayout parentView = new LinearLayout(getActivity());
            parentView.setLayoutParams(parentParams);
            parentView.setOrientation(LinearLayout.VERTICAL);
            parentView.setBackground(getResources().getDrawable(R.drawable.border_bottom));

            Button button = (Button) generateView(control, null).get(0);
            parentView.addView(button);

            String parentTag = control.getLabel();
            if (control.getChildCount() > 0) {
                LinearLayout optionView = new LinearLayout(getActivity());
                optionView.setId(R.id.option_view_id);
                optionView.setOrientation(LinearLayout.VERTICAL);
                optionView.setLayoutParams(parentParams);

                ArrayList<ControlObject> children = control.getChildren();
                for (ControlObject child : children) {
                    LinearLayout rowView = new LinearLayout(getActivity());
                    rowView.setOrientation(LinearLayout.HORIZONTAL);
                    rowView.setLayoutParams(optionParams);
                    rowView.setWeightSum(2);

                    ArrayList<View> arrViews = generateView(child, parentTag);
                    rowView.addView(arrViews.get(0));
                    if (arrViews.size() > 1) {
                        rowView.addView(arrViews.get(1));
                    }

                    optionView.addView(rowView);
                }
                parentView.addView(optionView);
            }
            operationView.addView(parentView);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private ArrayList<View> generateView(ControlObject control, final String parentTag) {
        ArrayList<View> arrViews = new ArrayList<>();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );

        String mType = control.getType().toLowerCase();
        if (!"button".equals(mType)) {
            TextView label = new TextView(getActivity());
            label.setText(control.getLabel());
            label.setLayoutParams(layoutParams);
            label.setTextSize(18);
            arrViews.add(label);
        }

        switch (mType) {
            case "button":
                Button button = new Button(getActivity());
                button.setText(control.getLabel());
                button.setOnClickListener(this);
                button.setTag(R.string.interface_tag, control.getInterface());
                button.setTag(R.string.method_tag, control.getMethod());
                button.setLayoutParams(layoutParams);
                button.setTextSize(18);
                arrViews.add(button);
                break;

            case "text":
                EditText editText = new EditText(getActivity());
                editText.setTag(R.string.option_tag, control.getLabel());
                editText.setLayoutParams(layoutParams);
                editText.setTextSize(18);
                arrViews.add(editText);
                break;

            case "select":
                String[] values = control.getValues();
                ArrayAdapter<String> adapter
                        = new ArrayAdapter<>(getActivity(), R.layout.spinner_item);
                for (String value : values) {
                    adapter.add(value);
                }
                Spinner spinner = new Spinner(getActivity());
                spinner.setAdapter(adapter);
                spinner.setLayoutParams(layoutParams);
                spinner.setTag(R.string.option_tag, control.getLabel());
                arrViews.add(spinner);
                break;

            case "file":
                LinearLayout pathParent = new LinearLayout(getActivity());
                pathParent.setOrientation(LinearLayout.HORIZONTAL);
                pathParent.setLayoutParams(layoutParams);
                pathParent.setTag(R.string.option_tag, control.getLabel());

                EditText pathView = new EditText(getActivity());
                pathView.setTag(R.string.path_tag, parentTag.replace(" ", "_") + "_edit");
                pathView.setTextSize(18);
                pathView.setLayoutParams(layoutParams);
                pathView.setSingleLine();

                Button selectButton = new Button(getActivity());
                selectButton.setTag(R.string.select_file_tag, control.getLabel());
                selectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        // dConnectHostでは外部領域より上のフォルダにアセスできない(x_x)
                        args.putString(KEY_FILE_PATH, Environment.getExternalStorageDirectory().toString());
                        args.putString(KEY_PATH_FIELD, parentTag.replace(" ", "_") + "_edit");

                        FileSelectDialog dialog = new FileSelectDialog();
                        dialog.setTargetFragment(mFragment, 0);
                        dialog.setArguments(args);
                        dialog.show(getFragmentManager(), "select_file");
                    }
                });
                selectButton.setBackgroundResource(R.drawable.ic_action_collection);
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(68, 68);
                buttonParams.setMargins(8, 16, 8, 8);
                selectButton.setLayoutParams(buttonParams);

                pathParent.addView(pathView);
                pathParent.addView(selectButton);

                arrViews.add(pathParent);
                break;
        }

        return arrViews;
    }
}
