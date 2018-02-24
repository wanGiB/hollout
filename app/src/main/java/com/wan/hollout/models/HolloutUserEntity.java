package com.wan.hollout.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.wan.hollout.database.HolloutDb;

/**
 * @author Wan Clem
 */
@Table(database = HolloutDb.class)
public class HolloutUserEntity extends BaseModel {

    @PrimaryKey
    @Column
    public String entityId;

    @Column
    public String entityName;

    @Column
    public String entityProfilePhotoUrl;

    @Column
    public String entityCoverPhotoUrl;

    public String getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityProfilePhotoUrl() {
        return entityProfilePhotoUrl;
    }

    public String getEntityCoverPhotoUrl() {
        return entityCoverPhotoUrl;
    }

}
