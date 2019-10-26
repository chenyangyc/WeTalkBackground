package DataBeans;

import net.sf.json.JSONObject;


public class SendMsgRequestBean extends CommonRequestBean implements JsonToObject{
    private String to, msg;
    private String userName;
    private int token;
    private String time;

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

    public String getTime() { return time; }

    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObject = JSONObject.fromObject(jsonData);
        userName = jsonObject.getString("username");
        token = jsonObject.getInt("token");
        to = jsonObject.getString("to");
        msg = jsonObject.getString("msg");
        time = jsonObject.getString("time");
    }
}
