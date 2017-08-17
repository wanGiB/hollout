package com.wan.hollout.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * @author Wan Clem
 */
@Database(name = HolloutDb.NAME, version = HolloutDb.VERSION, backupEnabled = true)
public class HolloutDb {
    static final String NAME = "HolloutDb";
    public static final int VERSION = 1;

}
