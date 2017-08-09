package com.wan.hollout.interfaces;

import android.view.View;

import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;


public interface DrawerRecyclerInterface {

    void onCategorySelected(View v, DrawerItemCategory drawerItemCategory);

    void onPageSelected(View v, DrawerItemPage drawerItemPage);

    void onHeaderSelected();
}
