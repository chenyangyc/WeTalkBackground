package DataBeans;

import net.sf.json.JSONObject;

public class ResultRequestBean extends CommonRequestBean implements JsonToObject {
    private int status, token;
    private String from, to;

    public int getStatus() {
        return status;
    }

    public int getToken() {
        return token;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObject = JSONObject.fromObject(jsonData);
        status = jsonObject.getInt("status");
        token = jsonObject.getInt("token");
        from = jsonObject.getString("from");
        to = jsonObject.getString("to");
    }
}
