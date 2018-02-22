package com.wan.hollout.database;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.wan.hollout.models.ChatMessage;

/**
 * @author Wan Clem
 */
@Database(name = HolloutDb.NAME, version = HolloutDb.VERSION, backupEnabled = true)
public class HolloutDb {
    static final String NAME = "HolloutDb";
    public static final int VERSION = 6;

    @Migration(version = HolloutDb.VERSION, database = HolloutDb.class)
    public static class ChatMigration extends AlterTableMigration<ChatMessage> {

        public ChatMigration(Class<ChatMessage> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            super.onPreMigrate();
            addColumn(SQLiteType.TEXT, "fromPhotoUrl");
            addColumn(SQLiteType.INTEGER, "videoDuration");
            addColumn(SQLiteType.INTEGER, "fileUploadProgress");
            addColumn(SQLiteType.INTEGER, "fileMimeType");
            addColumn(SQLiteType.INTEGER, "readSoundBanged");
        }

    }

}
