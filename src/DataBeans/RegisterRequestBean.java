package DataBeans;

import net.sf.json.JSONObject;

public class RegisterRequestBean implements JsonToObject {
    private String userName, pwd;

    public String getUserName() {
        return userName;
    }

    public String getPwd() {
        return pwd;
    }

    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObj = new JSONObject();
        userName = jsonObj.getString("userName");
        pwd = jsonObj.getString("pwd");
    }
}
