package cn.rong.combusis.ui.room.fragment.gift;

import androidx.annotation.DrawableRes;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author gyn
 * @date 2021/10/9
 */
public class Gift implements Serializable {
    private int index;
    @DrawableRes
    private int icon;
    private String name;
    private int price;

    public Gift(int index, @DrawableRes int icon, String name, int price) {
        this.index = index;
        this.icon = icon;
        this.name = name;
        this.price = price;
    }

    public Gift() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public @DrawableRes
    int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gift)) return false;
        Gift gift = (Gift) o;
        return getIndex() == gift.getIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex());
    }
}
