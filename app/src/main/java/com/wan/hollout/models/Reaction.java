package com.wan.hollout.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.wan.hollout.db.HolloutDb;

/**
 * @author Wan Clem
 */
@SuppressWarnings("WeakerAccess")
@Table(database = HolloutDb.class)
public class Reaction {

    @PrimaryKey
    @Column
    public String id;

    @Column
    public String reactionValue;

    public String getId() {
        return id;
    }

    public String getReactionValue() {
        return reactionValue;
    }

}
