package com.salmannazir.filemanager.parsers;



import com.salmannazir.filemanager.network.L;
import com.salmannazir.filemanager.network.TaskResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Salman on 06/06/2016.
 */
public class FileUploadParser implements BaseParser {

    @Override
    public TaskResult parse(int httpCode, String response) {
        TaskResult result = new TaskResult();
        if(httpCode == SUCCESS) {
            try {
                JSONObject j = new JSONObject(response);
                String status = j.optString("status");
                if(status.equalsIgnoreCase("ok")) {
                    JSONArray arr = j.optJSONArray("results");
                    if (arr != null) {
//
                        result.success(true);
                    }
                } else {
                    result.code = httpCode;
                    result.message = j.optString("status");
                }
            } catch (Exception e) {
                L.d(getClass().getName() + " :> " + e.getMessage());
            }
        } else {
            try {
                JSONObject j = new JSONObject(response);
                result.code = httpCode;
                result.message = j.optString("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return result;
    }
}
