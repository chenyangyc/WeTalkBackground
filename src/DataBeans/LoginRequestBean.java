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
        JSONObject jsonObj = JSONObject.fromObject(jsonData);
        userName = jsonObj.getString("username");
        pwd = jsonObj.getString("pwd");
    }
}
