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


@Table(database = HolloutDb.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class PathEntity extends BaseModel {

    @PrimaryKey
    @Column
    public String pathId;

    @Column
    public String pathName;

    @Column
    public String personId;

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

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
