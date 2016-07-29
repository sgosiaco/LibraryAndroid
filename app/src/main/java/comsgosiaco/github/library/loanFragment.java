package comsgosiaco.github.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class loanFragment extends SwipeRefreshListFragment{

    private DBHelper librarydb;
    private ArrayList array_list;
    private ArrayAdapter<String> arrayAdapter;
    private String name = "";
    private String email = "";
    private int index = -1;
    private int RESULT_OK = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        librarydb = new DBHelper(getActivity());
        array_list = librarydb.getAllAvailableBooks();
        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, array_list);
        setListAdapter(arrayAdapter);
        view.setBackgroundColor(Color.parseColor("#FAFAFA"));

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                array_list = librarydb.getAllAvailableBooks();
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();
                setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        array_list = librarydb.getAllAvailableBooks();
        arrayAdapter.clear();
        arrayAdapter.addAll(array_list);
        arrayAdapter.notifyDataSetChanged();
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
                index = pos;
                doLaunchContactPicker(getView().findViewById(android.R.id.content), 1001);
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = librarydb.getAvailData((String) array_list.get(position));
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE));
        String author = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_AUTHOR));
        String isbn = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ISBN));
        String isbn13 = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ISBN13));
        showToast(title + " <" + isbn + ", " + isbn13 + "> by " + author);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //showToast( "Loan: " + query);
                Cursor cursor = librarydb.getData(query, "FALSE");
                if(cursor.getCount() == 0)
                {
                    showToast(query + " doesn't exist!");
                    if( ! searchView.isIconified()) {
                        searchView.setIconified(true);
                    }
                    searchItem.collapseActionView();
                    return false;
                }
                array_list = librarydb.getAllAvailableBooks(query);
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
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
                                Cursor cs = librarydb.getAvailData((String) array_list.get(index));
                                cs.moveToFirst();
                                int id = cs.getInt(cs.getColumnIndex(DBHelper.COLUMN_ID));
                                String title = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_TITLE));
                                String author = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_AUTHOR));
                                String publisher = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_PUBLISHER));
                                String year = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_YEAR));
                                String isbn = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN));
                                String isbn13 = cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN13));
                                String loaned = "TRUE";
                                String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
                                librarydb.updateBook(id, title, author, publisher, year, isbn, isbn13, loaned, name, email, date);
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
