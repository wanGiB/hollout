package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.PersonView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseUser> people;
    private Activity activity;
    private LayoutInflater layoutInflater;

    private String searchString;

    public PeopleAdapter(Activity activity, List<ParseUser> people) {
        this.activity = activity;
        this.people = people;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
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
        ParseUser parseUser = people.get(position);
        personHolder.bindData(activity, getSearchString(), parseUser);
    }

    @Override
    public int getItemCount() {
        return people != null ? people.size() : 0;
    }

    static class PersonHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.person_view)
        PersonView personView;

        PersonHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Activity activity, String searchString, ParseUser parseUser) {
            personView.bindData(activity, searchString, parseUser);
        }

    }

}
