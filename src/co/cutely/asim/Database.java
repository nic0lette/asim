package co.cutely.asim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 *
 *
 * Created by johanna on 11/2/14.
 */
public class Database  {
    private Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public Database(Context context) {
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

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            String insert = context.getString(R.string.create_message_table,
                    context.getString(R.string.message_table_name));

            database.execSQL(insert);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            throw new IllegalStateException("Database upgrade is not possible");
        }
    }

    /**
     * Database layout plan (tenative):
     *
     * messages:
     * id               serial
     * timestamp        integer, default = now
     * xmpp_id          integer (?), target or sender
     * sender           bool, T if we sent the message
     * processed        bool, T if we read the message (received) or sent it successfully (sent)
     * message          text
     * xhtml            bool, T if xhtml formatted message
     * otr              bool, T is message was otr encrypted
     *
     * xmpp_ids:
     * id               integer (?)
     * email            text
     *
     */
    public static class Message {
        public final int id;
        public final int ts;
        public final int xmpp_id; // we probably need to do this differently later...
        public final boolean sender;
        public final boolean processed;
        public final String message;
        public final boolean xhtml;
        public final boolean otr;

        public Message(int id, int ts, int xmpp_id, boolean sender, boolean processed, String message, boolean xhtml, boolean otr) {
            this.id = id;
            this.ts = ts;
            this.xmpp_id = xmpp_id;
            this.sender = sender;
            this.processed = processed;
            this.message = message;
            this.xhtml = xhtml;
            this.otr = otr;
        }
    }

}
