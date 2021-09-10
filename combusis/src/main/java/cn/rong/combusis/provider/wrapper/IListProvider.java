package cn.rong.combusis.provider.wrapper;

import com.rongcloud.common.net.IResultBack;

import java.util.List;

/**
 * 页面列表数据¬
 *
 * @param <T>
 */
public interface IListProvider<T> {
    int PAGE_SIZE = 10;

    void loadPage(int page, IResultBack<List<T>> resultBack);
}
