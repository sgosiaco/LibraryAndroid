package comsgosiaco.github.library;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class returnFragment extends ListFragment{

    private DBHelper librarydb;
    private ArrayList array_list;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        librarydb = new DBHelper(inflater.getContext());
        array_list = librarydb.getAllLoanedBooks();
        arrayAdapter= new ArrayAdapter(inflater.getContext(),android.R.layout.simple_list_item_1, array_list);
        setListAdapter(arrayAdapter);
        view.setBackgroundColor(Color.parseColor("#FAFAFA"));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState)
    {
        super.onActivityCreated(savedState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Select a book to return");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(35,57,91))); //https://coolors.co/3f51b5-161925-23395b-8ea8c3-cbf7ed

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Cursor cursor = librarydb.getLoanData(pos);
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = librarydb.getLoanData(position);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE));
        String loanee = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOANEE));
        String email = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_EMAIL));
        String date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE));
        showToast(title + " loaned to " + loanee + " <" + email + "> on " + date);
    }


    public void showToast(CharSequence text)
    {
        Context context = getActivity();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
