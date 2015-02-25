package jp.or.ixqsware.deviceconnectapp;

import java.util.ArrayList;

/**
 * Created by hnakadate on 15/01/13.
 */
public class ControlObject {
    private String mType;
    private String mLabel;
    private String mMethod;
    private String mInterface;
    private ArrayList<String> arrValues = new ArrayList<>();
    private ArrayList<ControlObject> arrChildren = new ArrayList<>();

    public ControlObject() {}

    public String getType() {
        return this.mType;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public String getMethod() {
        return this.mMethod;
    }

    public String getInterface() {
        return this.mInterface;
    }

    public ArrayList<ControlObject> getChildren() {
        return this.arrChildren;
    }

    public String[] getValues() {
        return this.arrValues.toArray(new String[arrValues.size()]);
    }

    public int getChildCount() {
        return this.arrChildren.size();
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public void setMethod(String method) {
        this.mMethod = method;
    }

    public void setInterface(String interface_) {
        this.mInterface = interface_;
    }

    public void addValue(String value) {
        this.arrValues.add(value);
    }

    public void addChild(ControlObject obj) {
        this.arrChildren.add(obj);
    }
}
