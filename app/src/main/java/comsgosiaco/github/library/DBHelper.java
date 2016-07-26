package comsgosiaco.github.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "library.db";
    public static final int DATABASE_VERSION = 3;
    public static final String TABLE_NAME = "library";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title"; //book title
    public static final String COLUMN_AUTHOR = "author"; //book author
    public static final String COLUMN_PUBLISHER = "publisher"; //book publisher
    public static final String COLUMN_YEAR = "year"; //year publisher
    public static final String COLUMN_ISBN = "isbn"; //isbn
    public static final String COLUMN_LOANED = "loaned"; //loaned or not TRUE or FALSE
    public static final String COLUMN_LOANEE = "loanee"; //person borrowing the book
    public static final String COLUMN_EMAIL = "email"; //email of borrower
    public static final String COLUMN_DATE = "date"; //date loaned
    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + TABLE_NAME + " " +
                        "(id integer primary key, title text, author text, publisher text, year text, isbn text, loaned text, loanee text, email text, date text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertBook(String title, String author, String publisher, String year, String isbn, String loaned, String loanee, String email, String date)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("author", author);
        contentValues.put("publisher", publisher);
        contentValues.put("year", year);
        contentValues.put("isbn", isbn);
        contentValues.put("loaned", loaned);
        contentValues.put("loanee", loanee);
        contentValues.put("email", email);
        contentValues.put("date", date);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + TABLE_NAME + " where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public boolean updateBook(int id, String title, String author, String publisher, String year, String isbn, String loaned, String loanee, String email, String date)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("title", title);
        contentValues.put("author", author);
        contentValues.put("publisher", publisher);
        contentValues.put("year", year);
        contentValues.put("isbn", isbn);
        contentValues.put("loaned", loaned);
        contentValues.put("loanee", loanee);
        contentValues.put("email", email);
        contentValues.put("date", date);
        db.update(TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteBook(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllBooks()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_TITLE)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getAllLoanedBooks()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            if(res.getString(res.getColumnIndex(COLUMN_LOANED)).equals("TRUE"))
            {
                array_list.add(res.getString(res.getColumnIndex(COLUMN_TITLE)));
            }
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getAllAvailableBooks()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            if(!res.getString(res.getColumnIndex(COLUMN_LOANED)).equals("TRUE"))
            {
                array_list.add(res.getString(res.getColumnIndex(COLUMN_TITLE)));
            }
            res.moveToNext();
        }
        return array_list;
    }
}