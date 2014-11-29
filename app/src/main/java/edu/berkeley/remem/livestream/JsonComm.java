package edu.berkeley.remem.livestream;

import android.os.AsyncTask;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by root on 11/27/14.
 */
public class JsonComm extends AsyncTask<String, Map, Void> {
    private String url;
    private Map params;


    public JsonComm(String mUrl, Map mParams) {
        url = mUrl;
        params = mParams;
    }

    @Override
    protected Void doInBackground(String... strings) {
        System.out.println("Inside of makeRequest");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        Iterator iter = params.entrySet().iterator();

        JSONObject jsonPOST = new JSONObject();

        try {
            while (iter.hasNext()) {
                Map.Entry pairs = (Map.Entry) iter.next();
                String key = (String) pairs.getKey();
                String value = (String) pairs.getValue();
                jsonPOST.put(key, value);
            }

            StringEntity se = new StringEntity(jsonPOST.toString());
            httpPost.setEntity(se);
            httpPost.setHeader("deviceType", "Android");
            httpPost.setHeader("Content-type", "application/json");

            ResponseHandler responseHandler = new BasicResponseHandler();
            String response = (String) httpClient.execute(httpPost, responseHandler);
            httpClient.execute(httpPost);
            System.out.println(response);
            //return response;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
