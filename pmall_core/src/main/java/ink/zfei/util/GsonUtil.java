package ink.zfei.util;

import com.google.gson.Gson;

public class GsonUtil {


    public static  <T>  String Obj2JsonStr(T t) {

        Gson gson = new Gson();
        return gson.toJson(t);
    }
}
