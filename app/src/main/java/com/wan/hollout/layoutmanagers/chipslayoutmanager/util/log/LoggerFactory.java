package com.wan.hollout.layoutmanagers.chipslayoutmanager.util.log;

import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;

public class LoggerFactory {
    @NonNull
    public IFillLogger getFillLogger(SparseArray<View> viewCache) {
        return new FillLogger(viewCache);
    }

}
