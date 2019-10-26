package DataBeans;

import net.sf.json.JSONObject;

public class MakeFriendsResponseBean extends CommonResponseBean implements ObjectToJson {
    public String from;
    public String time;

    @Override
    public String convertFromObject() {
        JSONObject jsonObject = new JSONObject();
        solveWith(jsonObject);
        jsonObject.put("from", from);
        jsonObject.put("time", time);
        return jsonObject.toString();
    }
}
