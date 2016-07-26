package comsgosiaco.github.library;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class availableTab extends AppCompatActivity {

    private DBHelper librarydb;
    private ListView obj;
    private ArrayList array_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_tab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select a book to return");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        librarydb = new DBHelper(this);
        array_list = librarydb.getAllLoanedBooks();
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter(this,android.R.layout.simple_list_item_1, array_list);

        obj = (ListView)findViewById(R.id.listViewReturn);
        obj.setAdapter(arrayAdapter);
        obj.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Cursor cursor = librarydb.getData(arg2 + 1);
                cursor.moveToFirst();
                String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE));
                String loanee = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOANEE));
                String email = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EMAIL));
                String date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE));
                showToast(title + " loaned to " + loanee + " <" + email + "> on " + date);
            }
        });
        obj.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Cursor cursor = librarydb.getData(pos+1);
                cursor.moveToFirst();
                int ID = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE));
                String author = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_AUTHOR));
                String publisher = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PUBLISHER));
                String year = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_YEAR));
                String isbn = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ISBN));
                String loaned = "FALSE";
                String loanee = "";
                String email = "";
                String date = "";
                librarydb.updateBook(ID, title, author, publisher, year, isbn, loaned, loanee, email, date);
                array_list = librarydb.getAllLoanedBooks();
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();
                showToast(title + " successfully returned to the library!");
                return true;
            }
        });
    }

    public void showToast(CharSequence text)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}
