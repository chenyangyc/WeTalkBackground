package DataBeans;

import net.sf.json.JSONObject;

public class MakeFriendsRequestBean extends CommonRequestBean implements JsonToObject {
    private String newFriend;
    private CommonRequestBean commonRequestBean = new CommonRequestBean();
    public String getNewFriend() {
        return newFriend;
    }

    @Override
    public void convertFromJson(String jsonData) {
        commonRequestBean.convertFromJson(jsonData);
        JSONObject jsonObj = JSONObject.fromObject(jsonData);
        newFriend = jsonObj.getString("newFriend");
    }
}
