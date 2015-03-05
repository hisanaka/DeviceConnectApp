package jp.or.ixqsware.deviceconnectapp;

import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;

/**
 * Created by hnakadate on 15/01/05.
 */
public class Constants {
    public static final String SERVER = "localhost";
    public static final int PORT = 4035;
    public static final String DEVICE_URL = "http://" + SERVER + ":" + PORT + "/gotapi/system";

    public static final String PREF_KEY_TOKEN = "token";
    public static final String PREF_KEY_TOKEN_GET = "token_get";
    public static final String PREF_KEY_CLIENT_ID = "client_id";
    public static final String PREF_KEY_CLIENT_SECRET = "secret";
    public static final String PREF_KEY_PERMISSIONS = "permissions";
    public static final String PREF_KEY_SERVER_ADDRESS = "server_address";
    public static final String PREF_KEY_SERVER_PORT = "server_port";
    public static final String PREF_KEY_XML_SOURCE = "xml_source";
    public static final String PREF_KEY_XML_PATH = "xml_path";

    public static final String KEY_SECTION_NUMBER = "section";
    public static final String KEY_SERVER_ADDRESS = "address";
    public static final String KEY_SERVER_PORT = "port";
    public static final String KEY_PERMISSION = "permission";
    public static final String KEY_PERMISSION_INDEX = "index";

    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_PROFILE_NAME = "profile";

    public static final String KEY_FILE_PATH = "path";
    public static final String KEY_PATH_FIELD = "path_field";

    public static final int DEVICE_SECTION = 0;
    public static final int SERVICE_SECTION = 1;
    public static final int OPERATION_SECTION = 2;
    public static final int PERMISSION_SECTION = 3;
}
