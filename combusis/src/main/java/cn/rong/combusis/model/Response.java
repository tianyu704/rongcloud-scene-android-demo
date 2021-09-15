package cn.rong.combusis.model;

import androidx.annotation.Nullable;

import com.bcq.net.wrapper.OkUtil;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {

    @SerializedName("code")
    private Integer code;
    @SerializedName("data")
    private JsonElement data;
    @SerializedName("msg")
    private String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public JsonElement getData() {
        return data;
    }


    @Nullable
    public <T> T get(Class<T> tClass) {
        if (null != data && !data.isJsonNull() && null != tClass) {
            return OkUtil.json2Obj(data, tClass);
        }
        return null;
    }

    @Nullable
    public <T> List<T> getList(Class<T> tClass) {
        if (null != data && !data.isJsonNull() && null != tClass) {
            return OkUtil.json2List(data, tClass);
        }
        return null;
    }

    @Nullable
    public <T> T get(String key, Class<T> tClass) {
        if (null != data && null != tClass && data.isJsonObject() && data.getAsJsonObject().has(key)) {
            return OkUtil.json2Obj(data.getAsJsonObject().get(key), tClass);
        }
        return null;
    }

    @Nullable
    public <T> List<T> getList(String key, Class<T> tClass) {
        if (null != data && null != tClass && data.isJsonObject() && data.getAsJsonObject().has(key)) {
            return OkUtil.json2List(data.getAsJsonObject().get(key), tClass);
        }
        return null;
    }
}
