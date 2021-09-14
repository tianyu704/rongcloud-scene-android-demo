package com.bcq.net.net;

import com.bcq.net.wrapper.interfaces.BusiCallback;
import com.bcq.net.wrapper.interfaces.IPage;
import com.bcq.net.wrapper.interfaces.IResult;

import java.util.List;

/**
 * @author: BaiCQ
 * @ClassName: ListCallback
 * @Description: 有body网络请求的回调
 */
public class ListCallback<R> implements BusiCallback<IResult.ObjResult<List<R>>, List<R>, IPage, R> {
    private Class<R> rClass;

    public ListCallback(Class<R> rClass) {
        this.rClass = rClass;
        if (null == rClass) {
            throw new IllegalArgumentException("The R Class<R> Can Not Null !");
        }
    }

    public void onResult(IResult.ObjResult<List<R>> result) {
    }

    public void onError(int code, String errMsg) {
    }

    @Override
    public void onAfter() {
    }

    @Override
    public Class<R> onGetType() {
        return rClass;
    }
}
