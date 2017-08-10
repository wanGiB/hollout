package com.wan.hollout.layoutmanagers.chipslayoutmanager.anchor;

public interface IAnchorFactory {
    /** find the view in a higher row which is closest to the left border*/
    AnchorViewState getAnchor();

    AnchorViewState createNotFound();

    /** modify anchorView state according to pre-layout state */
    void resetRowCoordinates(AnchorViewState anchorView);
}
