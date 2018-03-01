package com.wan.hollout.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.wan.hollout.database.HolloutDb;

/**
 * @author Wan Clem
 */

@SuppressWarnings("WeakerAccess")
@Table(database = HolloutDb.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
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

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setEntityProfilePhotoUrl(String entityProfilePhotoUrl) {
        this.entityProfilePhotoUrl = entityProfilePhotoUrl;
    }

    public void setEntityCoverPhotoUrl(String entityCoverPhotoUrl) {
        this.entityCoverPhotoUrl = entityCoverPhotoUrl;
    }
}
