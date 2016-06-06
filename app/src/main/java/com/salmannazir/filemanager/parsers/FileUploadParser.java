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
        result.success(true);
        return result;
    }
}
