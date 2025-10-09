package SupplyChainContract;

import com.owlike.genson.Genson;

public class Util {
    private static final Genson genson = new Genson();

    public static String toJSONString(Object obj) {
        return genson.serialize(obj);
    }

    public static <T> T fromJSONString(String json, Class<T> clazz) {
        return genson.deserialize(json, clazz);
    }
}
