package jp.or.ixqsware.deviceconnectapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SmartDevice implements Parcelable {

    public static final Parcelable.Creator<SmartDevice> CREATOR
           = new Parcelable.Creator<SmartDevice>() {

        @Override
        public SmartDevice createFromParcel(Parcel source) {
            return new SmartDevice(source);
        }

        @Override
        public SmartDevice[] newArray(int size) {
            return new SmartDevice[size];
        }
    };

    private String mName;
    private String mType;
    private String mId;

    private List<SmartService> mServiceList = new ArrayList<SmartService>();

    public SmartDevice(final String id, final String name) {
        setId(id);
        setName(name);
    }

    private SmartDevice(final Parcel in) {
        setName(in.readString());
        setType(in.readString());
        setId(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mType);
        dest.writeString(mId);
    }

    @Override
    public String toString() {
        return mName;
    }

    public void setId(final String id) {
        mId = id;
    }

    public void setName(final String name) {
        mName = name;
    }

    public void setType(final String type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public String getType() {
        return mType;
    }

    public String getId() {
        return mId;
    }

    public List<SmartService> getServiceList() {
        return mServiceList;
    }

    public void addService(final SmartService service) {
        mServiceList.add(service);
    }

    public void removeService(final SmartService service) {
        mServiceList.remove(service);
    }
}
