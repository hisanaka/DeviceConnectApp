package jp.or.ixqsware.deviceconnectapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static jp.or.ixqsware.deviceconnectapp.Constants.KEY_DEVICE_NAME;
import static jp.or.ixqsware.deviceconnectapp.Constants.KEY_PROFILE_NAME;

public class LoadInfoAsyncTaskLoader
        extends AsyncTaskLoader<HashMap<Integer, ArrayList<ControlObject>>> {
    private String profileName;
    private String deviceName;
    private String XPATH_PROFILE = "//Profile[@name='%1$s']/*";

    public LoadInfoAsyncTaskLoader(Context context, Bundle args) {
        super(context);
        profileName = args.getString(KEY_PROFILE_NAME);
        deviceName = args.getString(KEY_DEVICE_NAME);
    }

    @Override
    public HashMap<Integer, ArrayList<ControlObject>> loadInBackground() {
        HashMap<Integer, ArrayList<ControlObject>> mapResult = new HashMap<>();
        ArrayList<ControlObject> arrControls = new ArrayList<>();

        AssetManager manager = getContext().getAssets();
        try {
            InputStream is = manager.open(deviceName.replace(" ", "_") + ".xml");
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
            Document docXml = dBuilder.parse(is);

            XPathFactory xFactory = XPathFactory.newInstance();
            XPath xPath = xFactory.newXPath();

            NodeList attrNodes = (NodeList) xPath.evaluate(
                    String.format(XPATH_PROFILE, profileName),
                    docXml,
                    XPathConstants.NODESET);
            for (int i = 0; i < attrNodes.getLength(); i++) {
                Node attrNode = attrNodes.item(i);
                ControlObject obj = new ControlObject();
                obj.setLabel(xPath.evaluate("./Name/text()", attrNode));
                obj.setType("button");
                obj.setMethod(xPath.evaluate("./Method/text()", attrNode));
                obj.setInterface(xPath.evaluate("./Path/text()", attrNode));

                // オプション
                NodeList optionNodes= (NodeList) xPath.evaluate(
                        "./Option",
                        attrNode,
                        XPathConstants.NODESET);
                for (int j = 0; j < optionNodes.getLength(); j++) {
                    Node optionNode = optionNodes.item(j);
                    ControlObject child = new ControlObject();
                    child.setLabel(xPath.evaluate("./Name/text()", optionNode));
                    child.setType(xPath.evaluate("./Type/text()", optionNode));
                    child.setMethod("");
                    child.setInterface("");

                    NodeList valueNodes = (NodeList) xPath.evaluate("./Value",
                            optionNode, XPathConstants.NODESET);
                    for (int k = 0; k < valueNodes.getLength(); k++) {
                        Node valueNode = valueNodes.item(k);
                        child.addValue(xPath.evaluate("./text()", valueNode));
                    }
                    obj.addChild(child);
                }
                arrControls.add(obj);
            }
            mapResult.put(0, arrControls);
        } catch (IOException e) {
            mapResult.put(-1, null);
        } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
            mapResult.put(-2, null);
        }
        return mapResult;
    }
}

