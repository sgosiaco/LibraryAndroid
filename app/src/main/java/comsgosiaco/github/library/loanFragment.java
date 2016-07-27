package comsgosiaco.github.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class loanFragment extends ListFragment{

    private DBHelper librarydb;
    private ArrayList array_list;
    private ArrayAdapter<String> arrayAdapter;
    private String name = "";
    private String email = "";
    private int index = -1;
    private int RESULT_OK = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        librarydb = new DBHelper(inflater.getContext());
        array_list = librarydb.getAllAvailableBooks();
        arrayAdapter= new ArrayAdapter(inflater.getContext(),android.R.layout.simple_list_item_1, array_list);
        setListAdapter(arrayAdapter);
        view.setBackgroundColor(Color.parseColor("#FAFAFA"));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState)
    {
        super.onActivityCreated(savedState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Select a book to loan");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(142,168,195)));

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                doLaunchContactPicker(getView().findViewById(android.R.id.content), 1001);
                index = pos;
                //debug
                //Cursor c = librarydb.getAvailData(position);
                //c.moveToFirst();
                //showToast(c.getInt(c.getColumnIndex(DBHelper.COLUMN_ID)) + c.getString(c.getColumnIndex(DBHelper.COLUMN_TITLE)));
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = librarydb.getData(position);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE));
        String author = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_AUTHOR));
        String isbn = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ISBN));
        showToast(title + " <" + isbn + "> by " + author);
    }


    public void showToast(CharSequence text)
    {
        Context context = getActivity();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void doLaunchContactPicker(View view, int code) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Cursor cursor = null;
            switch (requestCode) {
                case 1001: //Export all
                    try {
                        Uri result = data.getData();

                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything email
                        cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
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
                            Toast.makeText(getActivity(), "No email found for contact.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            if(!email.equals("") && !name.equals(""))
                            {
                                Cursor cs = librarydb.getAvailData(index);
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
