package comsgosiaco.github.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class loanTab extends AppCompatActivity {

    private DBHelper librarydb;
    private ListView obj;
    private ArrayList array_list;
    private ArrayAdapter<String> arrayAdapter;
    private String name = "";
    private String email = "";
    private int index = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_tab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select a book to loan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        librarydb = new DBHelper(this);
        array_list = librarydb.getAllAvailableBooks();
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, array_list);

        obj = (ListView)findViewById(R.id.listViewLoan);
        obj.setAdapter(arrayAdapter);
        obj.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                doLaunchContactPicker(findViewById(android.R.id.content), 1001);
                index = arg2;
            }
        });
        obj.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Cursor cursor = librarydb.getData(pos + 1);
                cursor.moveToFirst();
                librarydb.deleteBook(pos + 1);
                array_list = librarydb.getAllAvailableBooks();
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    public void showToast(CharSequence text)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void doLaunchContactPicker(View view, int code) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Cursor cursor = null;
            switch (requestCode) {
                case 1001: //Export all
                    try {
                        Uri result = data.getData();

                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything email
                        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[] { id },
                                null);

                        int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                        // let's just get the first email
                        if (cursor.moveToFirst()) {
                            email = cursor.getString(emailIdx);
                            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        }
                    } catch (Exception e) {
                        showToast("Failed to get email data");
                        email = "";
                        name = "";
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (email.length() == 0 || !isValidEmail(email)) {
                            Toast.makeText(this, "No email found for contact.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            if(!email.equals("") && !name.equals(""))
                            {
                                Cursor cs = librarydb.getData(index+1);
                                cs.moveToFirst();
                                int id = cs.getInt(cs.getColumnIndex(DBHelper.COLUMN_ID));
                                String title = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_TITLE));
                                String author = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_AUTHOR));
                                String publisher = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_PUBLISHER));
                                String year = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_YEAR));
                                String isbn = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN));
                                String loaned = "TRUE";
                                String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
                                librarydb.updateBook(id, title, author, publisher, year, isbn, loaned, name, email, date);
                                array_list = librarydb.getAllAvailableBooks();
                                arrayAdapter.clear();
                                arrayAdapter.addAll(array_list);
                                arrayAdapter.notifyDataSetChanged();
                                showToast(title + " successfully loaned to " + name);
                            }
                        }
                    }
                    break;
            }

        }
        else
        {
            email = "";
            name = "";
        }
    }

    private boolean isValidEmail(CharSequence email)
    {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}
