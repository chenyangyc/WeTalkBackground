package DataBeans;

import net.sf.json.JSONObject;

public class CommonResponseBean {
    private int code;
    private String msg;
    private String type;

    protected void convertToJson(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        jsonObject.put("type", type);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void solveWith(JSONObject jsonObject) {
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        jsonObject.put("type", type);
    }

    public String convertFromObject() {
        JSONObject jsonObject = new JSONObject();
        solveWith(jsonObject);
        return jsonObject.toString();
    }
}
