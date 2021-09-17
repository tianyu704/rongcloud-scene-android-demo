package cn.rong.combusis.provider.wrapper;

import com.kit.wapper.IResultBack;

import java.util.List;

import cn.rong.combusis.provider.voiceroom.RoomType;

/**
 * 页面列表数据¬
 *
 * @param <T>
 */
public interface IListProvider<T> {
    int PAGE_SIZE = 10;

    /**
     * load by page
     *
     * @param page
     * @param roomType
     * @param resultBack
     */
    void loadPage(int page, RoomType roomType, IResultBack<List<T>> resultBack);
}
