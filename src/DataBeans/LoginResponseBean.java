package DataBeans;

import net.sf.json.JSONObject;

public class LoginResponseBean extends CommonResponseBean implements ObjectToJson{
    public int token;

    @Override
    public String convertFromObject() {
        JSONObject jsonObj = new JSONObject();
        solveWith(jsonObj);
        jsonObj.put("token", token);
        return jsonObj.toString();
    }
}
