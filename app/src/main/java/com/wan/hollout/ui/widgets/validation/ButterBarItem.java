package com.wan.hollout.ui.widgets.validation;

import java.io.Serializable;

public class ButterBarItem implements Serializable {

    private String itemName;

    private int itemIcon;

    public ButterBarItem(String itemName, int itemIcon) {
        this.itemName = itemName;
        this.itemIcon = itemIcon;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemIcon() {
        return itemIcon;
    }

    public void setItemIcon(int itemIcon) {
        this.itemIcon = itemIcon;
    }

}
