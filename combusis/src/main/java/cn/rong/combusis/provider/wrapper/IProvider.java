package cn.rong.combusis.provider.wrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kit.wapper.IResultBack;

import java.util.List;

/**
 * 实体provider的接口
 *
 * @param <T>
 */
public interface IProvider<T> {

    /**
     * 更新
     *
     * @param t
     */
    void update(T t);

    /**
     * 异步获取 缓存没有尝试从网络取
     *
     * @param key
     * @param resultBack
     */
    void getAsyn(@NonNull String key, IResultBack<T> resultBack);

    /**
     * 异步批量获取 缓存没有尝试从网络取
     *
     * @param keys
     */
    void batchGetAsyn(@NonNull List<String> keys, @NonNull IResultBack<List<T>> resultBack);

    /**
     * 监听单个
     *
     * @param key
     * @param resultBack
     */
    void observeSingle(@NonNull String key, @NonNull IResultBack<T> resultBack);

    /**
     * 移除单个实例监听器
     *
     * @param key
     */
    void removeSingleObserver(String key);

//    /**
//     * 监听所有
//     *
//     * @param resultBack
//     */
//    void observeAll(@NonNull IResultBack<List<T>> resultBack);
//
//    /**
//     * 移除全量监听器
//     *
//     * @param resultBack
//     */
//    void removeAllObserver(@NonNull IResultBack<List<T>> resultBack);

    /**
     * 远端获取实例
     *
     * @param ids        参数
     * @param resultBack
     */
    void provideFromService(@NonNull List<String> ids, @Nullable IResultBack<List<T>> resultBack);
}
