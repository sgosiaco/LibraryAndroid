package comsgosiaco.github.library;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class libraryFragment extends SwipeRefreshListFragment {

    private DBHelper librarydb;
    private ArrayList array_list;
    private ArrayAdapter<String> arrayAdapter;

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
        array_list = librarydb.getAllBooks();
        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, array_list);
        setListAdapter(arrayAdapter);
        view.setBackgroundColor(Color.parseColor("#FAFAFA"));

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                array_list = librarydb.getAllBooks();
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();
                setRefreshing(false);
            }
        });
        setColorScheme(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorAccent);
    }

    @Override
    public void onResume() {
        super.onResume();
        array_list = librarydb.getAllBooks();
        arrayAdapter.clear();
        arrayAdapter.addAll(array_list);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Library");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(63, 81, 181)));
        MainActivity.toggle.syncState();

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Cursor cursor = librarydb.getDataExact((String) array_list.get(pos));
                cursor.moveToFirst();
                librarydb.deleteBook(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                array_list = librarydb.getAllBooks();
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = librarydb.getDataExact((String) array_list.get(position));
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE));
        String loaned = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOANED));
        if (loaned.equals("TRUE")) {
            String loanee = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOANEE));
            String email = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EMAIL));
            String date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE));
            showToast(title + " loaned to " + loanee + " <" + email + "> on " + date);
        } else {
            showToast(title + " is not loaned out");
        }
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
                //showToast("Main library: " + query);
                Cursor cursor = librarydb.getData(query);
                if (cursor.getCount() == 0) {
                    showToast(query + " doesn't exist!");
                    if (!searchView.isIconified()) {
                        searchView.setIconified(true);
                    }
                    searchItem.collapseActionView();
                    return false;
                }

                array_list = librarydb.getAllBooks(query);
                arrayAdapter.clear();
                arrayAdapter.addAll(array_list);
                arrayAdapter.notifyDataSetChanged();

                if (!searchView.isIconified()) {
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

    public void showToast(CharSequence text) {
        Context context = getActivity();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
