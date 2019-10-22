package DataBeans;

import net.sf.json.JSONObject;


public class SendMsgRequestBean extends CommonRequestBean implements JsonToObject{
    private String to, msg;
    private String userName;
    private int token;

    public String getUserName() {
        return this.userName;
    }

    public int getToken() {
        return this.token;
    }

    public String getTo() {
        return to;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObject = JSONObject.fromObject(jsonData);
        userName = jsonObject.getString("username");
        token = jsonObject.getInt("token");
        to = jsonObject.getString("to");
        msg = jsonObject.getString("msg");
    }
}
