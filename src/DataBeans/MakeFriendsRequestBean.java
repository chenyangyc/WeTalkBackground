package DataBeans;

import net.sf.json.JSONObject;

public class MakeFriendsRequestBean extends CommonRequestBean implements JsonToObject {
    private String newFriend;
    private CommonRequestBean commonRequestBean = new CommonRequestBean();
    private String userName;
    private int token;

    public String getUserName() {
        return userName;
    }

    public int getToken() {
        return token;
    }
    public String getNewFriend() {
        return newFriend;
    }

    @Override
    public void convertFromJson(String jsonData) {
        commonRequestBean.convertFromJson(jsonData);
        JSONObject jsonObj = JSONObject.fromObject(jsonData);
        userName = jsonObj.getString("username");
        token = jsonObj.getInt("token");
        newFriend = jsonObj.getString("newfriend");
    }
}
