package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter.criteria;

import android.support.annotation.NonNull;

public interface ICriteriaFactory {
    @NonNull
    IFinishingCriteria getBackwardFinishingCriteria();

    @NonNull
    IFinishingCriteria getForwardFinishingCriteria();
}
