package ro.citynow;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerResponse {
    private Integer responseCode;
    private String responseBody;

    public ServerResponse(Integer code, String body) {
        this.responseBody = body;
        this.responseCode = code;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Integer getResponseCode() {
        return this.responseCode;
    }

    public void setRawResponse(String response) {
        this.responseBody = response;
    }

    public String getRawResponse() {
        return responseBody;
    }

    public JSONObject parseAsJson() throws JSONException {
        return new JSONObject(responseBody);
    }
}
