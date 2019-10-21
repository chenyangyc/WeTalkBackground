package DataBeans;

import net.sf.json.JSONObject;

public class CommonRequestBean implements JsonToObject {
    private String userName;
    private int token;

    public String getUserName() {
        return userName;
    }

    public int getToken() {
        return token;
    }


    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObj = new JSONObject();
        userName = jsonObj.getString("userName");
        token = jsonObj.getInt("token");
    }
}
