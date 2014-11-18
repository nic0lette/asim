package co.cutely.asim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class handling all the Database logic, including table creation, etc.
 */
public class Database  {
    private final Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public Database(final Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        db = null;
        dbHelper = null;
    }

    public class DatabaseHelper extends SQLiteOpenHelper {
        private final Context context;
        public static final String DATABASE_NAME = "asim.db";
        public static final int DATABASE_VERSION = 1;

        public DatabaseHelper(final Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            this.context = context;
        }

        @Override
        public void onCreate(final SQLiteDatabase database) {
            final String insert = context.getString(R.string.create_message_table,
                    context.getString(R.string.message_table_name));

            database.execSQL(insert);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            throw new IllegalStateException("Database upgrade is not possible");
        }
    }

}
