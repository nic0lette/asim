package co.cutely.asim;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import co.cutely.asim.messages.ChatMessage;

/**
 * Class handling all the Database logic, including table creation, etc.
 */
public class Database  {
    private static final String TAG = Database.class.getSimpleName();

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

    public ChatMessage createChatMessage(String xmpp_id, String xmpp_target, boolean sender, boolean processed,
                                         String message, boolean xhtml, boolean otr) {
        Log.i(TAG, "Starting database insert");

        ContentValues values = new ContentValues();
        values.put("xmpp_id", xmpp_id);
        values.put("xmpp_target", xmpp_target);
        values.put("sender", sender);
        values.put("processed", processed);
        values.put("message", message);
        values.put("xhtml", xhtml);
        values.put("otr", otr);

        long id = db.insert(context.getString(R.string.message_table_name), null, values);
        long ts = System.currentTimeMillis()/1000;

        Log.i(TAG, "Inserted message " + id + " into database: " + message);

        return new ChatMessage(id, ts, xmpp_id, xmpp_target, sender, processed, message, xhtml, otr);
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
            final String insert = context.getString(R.string.message_table_create,
                    context.getString(R.string.message_table_name));

            database.execSQL(insert);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            throw new IllegalStateException("Database upgrade is not possible");
        }
    }

}
