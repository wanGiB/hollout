package com.wan.hollout.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.wan.hollout.db.HolloutDb;

/**
 * @author Wan Clem
 */

@Table(database = HolloutDb.class)
public class PathEntity extends BaseModel {

    @PrimaryKey
    @Column
    public String pathId;

    @Column
    public String pathName;

    @Column
    public String personId;

    public String getPathName() {
        return pathName;
    }

    public String getPathId() {
        return pathId;
    }

    public String getPersonId() {
        return personId;
    }

}
