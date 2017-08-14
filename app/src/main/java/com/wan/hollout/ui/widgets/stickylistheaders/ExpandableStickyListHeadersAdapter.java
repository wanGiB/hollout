package com.wan.hollout.ui.widgets.stickylistheaders;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lsjwzh
 */
class ExpandableStickyListHeadersAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final StickyListHeadersAdapter mInnerAdapter;
    DualHashMap<View, Long> mViewToItemIdMap = new DualHashMap<View, Long>();
    DistinctMultiHashMap<String, View> mHeaderIdToViewMap = new DistinctMultiHashMap<>();
    List<String> mCollapseHeaderIds = new ArrayList<>();

    ExpandableStickyListHeadersAdapter(StickyListHeadersAdapter innerAdapter) {
        this.mInnerAdapter = innerAdapter;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return mInnerAdapter.getHeaderView(position, convertView, parent);
    }

    @Override
    public String getHeaderId(int position) {
        return mInnerAdapter.getHeaderId(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mInnerAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int i) {
        return mInnerAdapter.isEnabled(i);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        mInnerAdapter.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        mInnerAdapter.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public int getCount() {
        return mInnerAdapter.getCount();
    }

    @Override
    public Object getItem(int i) {
        return mInnerAdapter.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return mInnerAdapter.getItemId(i);
    }

    @Override
    public boolean hasStableIds() {
        return mInnerAdapter.hasStableIds();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView = mInnerAdapter.getView(i, view, viewGroup);
        mViewToItemIdMap.put(convertView, getItemId(i));
        mHeaderIdToViewMap.add(getHeaderId(i), convertView);
        if (mCollapseHeaderIds.contains(getHeaderId(i))) {
            convertView.setVisibility(View.GONE);
        } else {
            convertView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int i) {
        return mInnerAdapter.getItemViewType(i);
    }

    @Override
    public int getViewTypeCount() {
        return mInnerAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return mInnerAdapter.isEmpty();
    }

    public List<View> getItemViewsByHeaderId(String headerId) {
        return mHeaderIdToViewMap.get(headerId);
    }

    public boolean isHeaderCollapsed(String headerId) {
        return mCollapseHeaderIds.contains(headerId);
    }

    public void expand(String headerId) {
        if (isHeaderCollapsed(headerId)) {
            mCollapseHeaderIds.remove(headerId);
        }
    }

    public void collapse(String headerId) {
        if (!isHeaderCollapsed(headerId)) {
            mCollapseHeaderIds.add(headerId);
        }
    }

    public View findViewByItemId(long itemId) {
        return mViewToItemIdMap.getKey(itemId);
    }

    public long findItemIdByView(View view) {
        return mViewToItemIdMap.get(view);
    }
}
