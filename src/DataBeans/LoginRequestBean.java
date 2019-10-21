package DataBeans;

import net.sf.json.JSONObject;

public class LoginRequestBean implements JsonToObject {
    private String userName, pwd;

    public String getUserName() {
        return this.userName;
    }

    public String getPwd() {
        return this.pwd;
    }

    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObj = new JSONObject(jsonData);
        userName = jsonObj.getString("userName");
        pwd = jsonObj.getString("pwd");
    }
}
