package com.bcq.net.wrapper;

import com.bcq.net.wrapper.interfaces.IPage;
import com.bcq.net.wrapper.interfaces.IWrap;
import com.google.gson.JsonElement;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import okhttp3.Response;

public class Wrapper implements IWrap {
    private int code;
    //net info
    private String message;
    //数据集
    private JsonElement body;
    //页码索引
    private int page = -1;
    //总页
    private int total = 0;

    public Wrapper setCode(int code) {
        this.code = code;
        return this;
    }

    public void setBody(JsonElement body) {
        this.body = body;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPage(int page, int total) {
        this.page = page;
        this.total = total;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public JsonElement getBody() {
        return body;
    }

    @Override
    public IPage getPage() {
        if (page < 0) return null;
        return new Page(page, total);
    }

    @Nullable
    public <T> T get(Class<T> tClass) {
        if (null != body && !body.isJsonNull() && null != tClass) {
            return OkUtil.json2Obj(body, tClass);
        }
        return null;
    }

    @Nullable
    public <T> List<T> getList(Class<T> tClass) {
        if (null != body && !body.isJsonNull() && null != tClass) {
            return OkUtil.json2List(body, tClass);
        }
        return null;
    }

    @Nullable
    public <T> T get(String key, Class<T> tClass) {
        if (null != body && null != tClass && body.isJsonObject() && body.getAsJsonObject().has(key)) {
            return OkUtil.json2Obj(body.getAsJsonObject().get(key), tClass);
        }
        return null;
    }

    @Nullable
    public <T> List<T> getList(String key, Class<T> tClass) {
        if (null != body && null != tClass && body.isJsonObject() && body.getAsJsonObject().has(key)) {
            return OkUtil.json2List(body.getAsJsonObject().get(key), tClass);
        }
        return null;
    }
}

