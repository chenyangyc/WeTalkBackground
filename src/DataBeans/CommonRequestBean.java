package DataBeans;

import net.sf.json.JSONObject;

import javax.swing.*;

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
        JSONObject jsonObj = JSONObject.fromObject(jsonData);
        userName = jsonObj.getString("username");
        token = jsonObj.getInt("token");
    }
}
