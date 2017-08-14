package com.wan.hollout.rendering;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import com.wan.hollout.caching.HeaderProvider;
import com.wan.hollout.caching.HeaderViewCache;
import com.wan.hollout.ui.adapters.StickyRecyclerHeadersAdapter;


/**
 * @author Wan Clem
 */

public class StickyGridRecyclerViewDecoration extends RecyclerView.ItemDecoration {

    private final StickyRecyclerHeadersAdapter mAdapter;
    private final SparseArray<Rect> mHeaderRects = new SparseArray<>();
    private final HeaderProvider mHeaderProvider;
    private final OrientationProvider mOrientationProvider;
    private final HeaderPositionCalculator mHeaderPositionCalculator;
    private final HeaderRenderer mRenderer;
    private final DimensionCalculator mDimensionCalculator;

    /**
     * The following field is used as a buffer for internal calculations. Its sole purpose is to avoid
     * allocating new Rect every time we need one.
     */
    private final Rect mTempRect = new Rect();

    // TODO: Consider passing in orientation to simplify orientation accounting within calculation
    public StickyGridRecyclerViewDecoration(StickyRecyclerHeadersAdapter adapter) {
        this(adapter, new LinearLayoutOrientationProvider(), new DimensionCalculator());
    }

    private StickyGridRecyclerViewDecoration(StickyRecyclerHeadersAdapter adapter, OrientationProvider orientationProvider,
                                            DimensionCalculator dimensionCalculator) {
        this(adapter, orientationProvider, dimensionCalculator, new HeaderRenderer(orientationProvider),
                new HeaderViewCache(adapter, orientationProvider));
    }

    private StickyGridRecyclerViewDecoration(StickyRecyclerHeadersAdapter adapter, OrientationProvider orientationProvider,
                                            DimensionCalculator dimensionCalculator, HeaderRenderer headerRenderer, HeaderProvider headerProvider) {
        this(adapter, headerRenderer, orientationProvider, dimensionCalculator, headerProvider,
                new HeaderPositionCalculator(adapter, headerProvider, orientationProvider,
                        dimensionCalculator));
    }

    private StickyGridRecyclerViewDecoration(StickyRecyclerHeadersAdapter adapter, HeaderRenderer headerRenderer,
                                            OrientationProvider orientationProvider, DimensionCalculator dimensionCalculator, HeaderProvider headerProvider,
                                            HeaderPositionCalculator headerPositionCalculator) {
        mAdapter = adapter;
        mHeaderProvider = headerProvider;
        mOrientationProvider = orientationProvider;
        mRenderer = headerRenderer;
        mDimensionCalculator = dimensionCalculator;
        mHeaderPositionCalculator = headerPositionCalculator;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int itemPosition = parent.getChildAdapterPosition(view);

        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        boolean underHeader = isUnderHeader(itemPosition);

        if (underHeader) {

            View header = getHeaderView(parent, itemPosition);

            outRect.top = header.getHeight();
        }
    }

    private boolean isUnderHeader(int itemPosition) {
        return isUnderHeader(itemPosition, 3);
    }

    /**
     * checks if item is "under header"
     * <p/>
     * Items is under header if any of the following conditions are true:
     * <p/>
     * a) within spanCount the header id has changed once
     *
     * @param itemPosition
     * @return
     */
    private boolean isUnderHeader(int itemPosition, int spanCount) {
        if (itemPosition == 0) {
            return true;
        }

        //get current items header id
        String headerId = mAdapter.getHeaderId(itemPosition);

        //loop through each item within spancount
        for (int i = 1; i < spanCount + 1; i++) {
            String previousHeaderId = null;

            int previousItemPosition = itemPosition - i;

            //gets previous items headerId
            if (previousItemPosition >= 0 && previousItemPosition < mAdapter.getItemCount()) {
                previousHeaderId = mAdapter.getHeaderId(previousItemPosition);
            }

            //checks if header id at given position is different from previous header id and if so, returns true to indicate this item belongs under the header
            if (!headerId.equals(previousHeaderId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        final int childCount = parent.getChildCount();

        //checks if there's any childs, aka can we even have any header?
        if (childCount <= 0 || mAdapter.getItemCount() <= 0) {
            return;
        }

        //stores the "highest" seen top value of any header to perform the pusheroo of topmost header
        int highestTop = Integer.MAX_VALUE;

        //loops through childs in the recyclerview on reverse order to perform the pushing of uppermost header faster, because before it, there is the next headers top stored to highestTop
        for (int i = childCount - 1; i >= 0; i--) {
            View itemView = parent.getChildAt(i);

            //fetches the position within adapter
            int position = parent.getChildAdapterPosition(itemView);

            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            //only draw if is the first withing recyclerview, aka is the first view in whole tree or if the item in question is the first under its category(or header..)
            if (i == 0 || isFirstUnderHeader(position)) {
                //fetches the header from header provider, which is basically just call to adapters getHeader/bindHeader
                View header = mHeaderProvider.getHeader(parent, position);

                //calculates the translations of the header within view, which is on top of the give item
                int translationX = parent.getLeft();
                int translationY = Math.max(itemView.getTop() - header.getHeight(), 0);

                mTempRect.set(translationX, translationY, translationX + header.getWidth(),
                        translationY + header.getHeight());

                //moves the header so it is pushed by the following header upwards
                if (mTempRect.bottom > highestTop) {
                    mTempRect.offset(0, highestTop - mTempRect.bottom);
                }

                //draws the actual header
                drawHeader(parent, c, header, mTempRect);

                //stores top of the header to help with the pushing of topmost header
                highestTop = mTempRect.top;
            }
        }
    }
    /**
     * Gets the header view for the associated position.  If it doesn't exist yet, it will be
     * created, measured, and laid out.
     *
     * @param parent   the recyclerview
     * @param position the position to get the header view for
     * @return Header view
     */
    public View getHeaderView(RecyclerView parent, int position) {
        return mHeaderProvider.getHeader(parent, position);
    }

    public void drawHeader(RecyclerView recyclerView, Canvas canvas, View header, Rect offset) {
        canvas.save();

        canvas.translate(offset.left, offset.top);

        header.draw(canvas);

        canvas.restore();
    }

    private boolean isFirstUnderHeader(int position) {
        return position == 0 || mAdapter.getHeaderId(position) != mAdapter.getHeaderId(position - 1);
    }

}
