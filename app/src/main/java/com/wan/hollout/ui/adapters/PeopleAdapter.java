package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.models.NearbyPerson;
import com.wan.hollout.ui.widgets.NearbyPersonView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NearbyPerson> people;
    private Activity activity;
    private LayoutInflater layoutInflater;

    private String searchString;

    public PeopleAdapter(Activity activity, List<NearbyPerson> people) {
        this.activity = activity;
        this.people = people;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    private String getSearchString() {
        return searchString;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.row_person_to_meet, parent, false);
        return new PersonHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final PersonHolder personHolder = (PersonHolder) holder;
        ParseObject parseUser = people.get(position).getPerson();
        personHolder.bindData(activity, getSearchString(), parseUser);
    }

    @Override
    public int getItemCount() {
        return people != null ? people.size() : 0;
    }

    static class PersonHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.person_view)
        NearbyPersonView nearbyPersonView;

        PersonHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Activity activity, String searchString, ParseObject parseUser) {
            nearbyPersonView.bindData(activity, searchString, parseUser);
        }

    }

}
