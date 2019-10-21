package DataBeans;

import net.sf.json.JSONObject;


public class SendMsgRequestBean extends CommonRequestBean implements JsonToObject{
    private String to, msg;


    public String getTo() {
        return to;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public void convertFromJson(String jsonData) {
        JSONObject jsonObject = JSONObject.fromObject(jsonData);
        to = jsonObject.getString("to");
        msg = jsonObject.getString("msg");
    }
}
