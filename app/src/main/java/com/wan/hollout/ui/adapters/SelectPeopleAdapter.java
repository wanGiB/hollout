package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class SelectPeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ConversationItem> people;
    private LayoutInflater layoutInflater;

    private Activity context;
    private String searchString;

    public SelectPeopleAdapter(Activity context, List<ConversationItem> people) {
        this.context = context;
        this.people = people;
        this.layoutInflater = LayoutInflater.from(context);
    }

    private String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.selectable_person, parent, false);
        return new SelectablePersonHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SelectablePersonHolder selectablePersonHolder = (SelectablePersonHolder) holder;
        selectablePersonHolder.bindData(context, people.get(position), getSearchString(), this);
    }

    @Override
    public int getItemCount() {
        return people != null ? people.size() : 0;
    }

    static class SelectablePersonHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.person_photo)
        CircleImageView personPhotoView;

        @BindView(R.id.person_name)
        HolloutTextView personNameView;

        @BindView(R.id.parent_view)
        View parentView;

        SelectablePersonHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(Activity context, final ConversationItem conversationItem, String searchString, final SelectPeopleAdapter selectPeopleAdapter) {
            final ParseObject conversationObject = conversationItem.getRecipient();
            String contactName = conversationObject.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String contactPhotoUrl = conversationObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            personNameView.setText(WordUtils.capitalize(contactName));
            if (StringUtils.isNotEmpty(contactPhotoUrl)) {
                UiUtils.loadImage(context, contactPhotoUrl, personPhotoView);
            }
            if (StringUtils.isNotEmpty(searchString)) {
                personNameView.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(contactName),
                        ContextCompat.getColor(context, R.color.hollout_color_three)));
            } else {
                personNameView.setText(WordUtils.capitalize(contactName));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!AppConstants.selectedPeople.contains(conversationItem)) {
                        AppConstants.selectedPeople.add(conversationItem);
                        AppConstants.selectedPeoplePositions.put(conversationObject.getString(AppConstants.REAL_OBJECT_ID).hashCode(), true);
                    } else {
                        AppConstants.selectedPeople.remove(conversationItem);
                        AppConstants.selectedPeoplePositions.put(conversationObject.getString(AppConstants.REAL_OBJECT_ID).hashCode(), false);
                    }
                    selectPeopleAdapter.notifyDataSetChanged();
                    EventBus.getDefault().post(AppConstants.REFRESH_SELECTED_PEOPLE_TO_FORWARD_MESSAGE);
                }
            });

            invalidateItemView(context, conversationObject.getString(AppConstants.REAL_OBJECT_ID).hashCode());

        }

        private void invalidateItemView(Activity activity, int objectHashCode) {
            parentView.setBackgroundColor(AppConstants.selectedPeoplePositions.get(objectHashCode)
                    ? ContextCompat.getColor(activity, R.color.blue_100) :
                    ContextCompat.getColor(activity, R.color.white));
        }

    }

}
