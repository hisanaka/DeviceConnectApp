package jp.or.ixqsware.deviceconnectapp;

/**
 * Created by hnakadate on 15/01/06.
 */
public class SmartService {
    private String mName;

    public SmartService(final String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public int getIconId() {
        return android.R.drawable.ic_menu_info_details;
    }
}
