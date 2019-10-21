package DataBeans;

import net.sf.json.JSONObject;

public class MakeFriendsResponseBean extends CommonResponseBean implements ObjectToJson {
    public String from;

    @Override
    public String convertFromObject() {
        JSONObject jsonObject = new JSONObject();
        solveWith(jsonObject);
        jsonObject.put("from", from);
        return jsonObject.toString();
    }
}
