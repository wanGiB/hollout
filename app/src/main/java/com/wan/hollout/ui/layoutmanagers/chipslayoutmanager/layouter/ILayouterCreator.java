package com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.layouter;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.wan.hollout.ui.layoutmanagers.chipslayoutmanager.anchor.AnchorViewState;

interface ILayouterCreator {

    Rect createOffsetRectForBackwardLayouter(@NonNull AnchorViewState anchorRect);

    AbstractLayouter.Builder createBackwardBuilder();

    AbstractLayouter.Builder createForwardBuilder();

    Rect createOffsetRectForForwardLayouter(AnchorViewState anchorRect);

}
