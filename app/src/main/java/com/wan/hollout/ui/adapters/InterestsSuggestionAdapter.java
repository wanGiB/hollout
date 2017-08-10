package com.wan.hollout.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class InterestsSuggestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> occupations;
    private LayoutInflater layoutInflater;
    private OccupationSelectedListener occupationSelectedListener;
    private Context context;
    private String searchKey;

    public interface OccupationSelectedListener {
        void onSelectedOccupation(String occupation);
    }

    public InterestsSuggestionAdapter(Context context, List<ParseObject> interests,
                                      String searchKey,
                                      OccupationSelectedListener occupationSelectedListener) {
        this.context = context;
        this.occupations = interests;
        this.searchKey = searchKey;
        this.occupationSelectedListener = occupationSelectedListener;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View occupationView = layoutInflater.inflate(R.layout.chip, parent, false);
        return new OccupationsHolder(occupationView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        OccupationsHolder occupationsHolder = (OccupationsHolder) holder;
        ParseObject parseObject = occupations.get(position);
        if (parseObject != null) {
            occupationsHolder.bindOccupation(context, parseObject, searchKey, occupationSelectedListener);
        }
    }

    @Override
    public int getItemCount() {
        return occupations != null ? occupations.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class OccupationsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.chip_item)
        TextView occupationNameView;

        public OccupationsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindOccupation(Context context, ParseObject parseObject, String searchKey,
                                   final OccupationSelectedListener occupationSelectedListener) {

            final String occupationName = StringUtils.capitalize(parseObject.getString("name"));

            occupationNameView.setText(UiUtils.highlightTextIfNecessary(searchKey, new SpannableString(occupationName),
                    ContextCompat.getColor(context, R.color.colorAccent)));

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (occupationSelectedListener != null) {
                        occupationSelectedListener.onSelectedOccupation(occupationName);
                    }
                }

            });

        }

    }

}
