package DataBeans;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

public class FriendsResponseBean extends CommonResponseBean implements ObjectToJson {
    public List<String> friendsList;

    @Override
    public String convertFromObject() {
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        solveWith(jsonObj);
        for(String str: friendsList) {
            jsonArray.put(friendsList);
        }
        jsonObj.put("friends", friendsList);
        return jsonObj.toString();
    }
}
