package DataBeans;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;

public class FriendsResponseBean extends CommonResponseBean implements ObjectToJson {
    public ArrayList<String> friendsList;

    @Override
    public String convertFromObject() {
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        solveWith(jsonObj);
        for(String str: friendsList) {
            jsonArray.put(str);
        }
        jsonObj.put("friends", jsonArray);
        return jsonObj.toString();
    }
}
