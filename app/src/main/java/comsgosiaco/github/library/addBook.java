package comsgosiaco.github.library;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class addBook extends AppCompatActivity implements addBookDialogFragment.addBookDialogListener {

    private DBHelper librarydb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add a new book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Button scan = (Button) findViewById(R.id.scanButton);
        scan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                manualSearch();
                return true;
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scan(v);
            }
        });
        librarydb = new DBHelper(this);
    }

    public void scan(View view)
    {
            resetFields();
            Intent i = new Intent(addBook.this, FullScannerActivity.class);
            startActivityForResult(i, 1);
    }

    public void add(View view)
    {
        TextView titleText = (TextView) findViewById(R.id.title);
        TextView authorText = (TextView) findViewById(R.id.author);
        TextView isbnText = (TextView) findViewById(R.id.isbn);
        TextView isbn13Text = (TextView) findViewById(R.id.isbn13);
        TextView publisherText = (TextView) findViewById(R.id.publisher);
        TextView dateText = (TextView) findViewById(R.id.year);
        if(isbnText.getText().toString().equals("") || isbn13Text.getText().toString().equals(""))
        {
            showToast("Please enter an isbn!");
        }
        else
        {
            if(librarydb.getDataISBN(Integer.parseInt(isbnText.getText().toString())).getCount() == 0 || librarydb.getDataExact(titleText.getText().toString()).getCount() == 0)
            {
                if(librarydb.insertBook(titleText.getText().toString(), authorText.getText().toString(), publisherText.getText().toString(), dateText.getText().toString(), isbnText.getText().toString(), isbn13Text.getText().toString(),"FALSE", "", "", ""))
                {
                    DialogFragment fragment = new addBookDialogFragment();
                    fragment.show(getFragmentManager(), "add_book");
                }
                else
                {
                    showToast("FATAL ERROR HAS OCCURED IN ADDING NEW BOOK!");
                }
            }
            else
            {
                showToast("Adding duplicate book!");
                String temp = titleText.getText().toString();
                titleText.setText(temp+" ("+librarydb.getDataISBN(Integer.parseInt(isbnText.getText().toString())).getCount()+")");
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        resetFields();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        finish();
    }

    public void resetFields()
    {
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setText("");
        TextView authorText = (TextView) findViewById(R.id.author);
        authorText.setText("");
        TextView isbnText = (TextView) findViewById(R.id.isbn);
        isbnText.setText("");
        TextView isbn13Text = (TextView) findViewById(R.id.isbn13);
        isbn13Text.setText("");
        TextView publisherText = (TextView) findViewById(R.id.publisher);
        publisherText.setText("");
        TextView dateText = (TextView) findViewById(R.id.year);
        dateText.setText("");
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("about:blank");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                getBook(data.getStringExtra("barcode"));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                showToast("Wrong type of barcode!");
            }
        }
    }//onActivityResult

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.addbook, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            manualSearch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getBook(final String barcode)
    {
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + barcode + "&key=AIzaSyA1s6tiC-9EniXZlcjYYorLymwbc854sKU";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(json);
                    if(object.getString("totalItems").equals("0"))
                    {
                        showToast("Invalid ISBN: No results found");
                        return;
                    }
                    JSONArray array = object.getJSONArray("items");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);

                        JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                        String title = volumeInfo.getString("title");
                        TextView titleText = (TextView) findViewById(R.id.title);
                        titleText.setText(title);

                        JSONArray authors = volumeInfo.getJSONArray("authors");
                        String author = authors.getString(0);
                        TextView authorText = (TextView) findViewById(R.id.author);
                        authorText.setText(author);

                        JSONArray identifiers = volumeInfo.getJSONArray("industryIdentifiers");
                        if(identifiers.length() == 2)
                        {
                            JSONObject id = identifiers.getJSONObject(1);
                            String isbn13 = id.getString("identifier");
                            TextView isbn13Text = (TextView) findViewById(R.id.isbn13);
                            isbn13Text.setText(isbn13);
                        }

                        String dateString = volumeInfo.getString("publishedDate");
                        String[] tempDate = dateString.split("-");
                        int date = Integer.valueOf(tempDate[0]);
                        TextView dateText = (TextView) findViewById(R.id.year);
                        dateText.setText("" + date);

                        String publisher = volumeInfo.getString("publisher");
                        TextView publisherText = (TextView) findViewById(R.id.publisher);
                        publisherText.setText(publisher);

                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        String imageLink = imageLinks.getString("smallThumbnail");
                        WebView cover = (WebView) findViewById(R.id.webView);
                        cover.setVisibility(View.GONE);
                        cover.loadUrl(imageLink);
                        cover.reload();
                        cover.setVisibility(View.VISIBLE);

                        TextView barcodeText = (TextView) findViewById(R.id.isbn);
                        barcodeText.setText(barcode);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                showToast("Couldn't access API!");
            }
        });
    }


    public void getBook13(final String barcode)
    {
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + barcode + "&key=AIzaSyA1s6tiC-9EniXZlcjYYorLymwbc854sKU";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(json);
                    if(object.getString("totalItems").equals("0"))
                    {
                        showToast("Invalid ISBN: No results found");
                        return;
                    }
                    JSONArray array = object.getJSONArray("items");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);

                        JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                        String title = volumeInfo.getString("title");
                        TextView titleText = (TextView) findViewById(R.id.title);
                        titleText.setText(title);

                        JSONArray authors = volumeInfo.getJSONArray("authors");
                        String author = authors.getString(0);
                        TextView authorText = (TextView) findViewById(R.id.author);
                        authorText.setText(author);

                        JSONArray identifiers = volumeInfo.getJSONArray("industryIdentifiers");
                        if(identifiers.length() == 2)
                        {
                            JSONObject id = identifiers.getJSONObject(0);
                            String isbn = id.getString("identifier");
                            TextView isbnText = (TextView) findViewById(R.id.isbn);
                            isbnText.setText(isbn);
                        }

                        String dateString = volumeInfo.getString("publishedDate");
                        String[] tempDate = dateString.split("-");
                        int date = Integer.valueOf(tempDate[0]);
                        TextView dateText = (TextView) findViewById(R.id.year);
                        dateText.setText("" + date);

                        String publisher = volumeInfo.getString("publisher");
                        TextView publisherText = (TextView) findViewById(R.id.publisher);
                        publisherText.setText(publisher);

                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        String imageLink = imageLinks.getString("smallThumbnail");
                        WebView cover = (WebView) findViewById(R.id.webView);
                        cover.setVisibility(View.GONE);
                        cover.loadUrl(imageLink);
                        cover.reload();
                        cover.setVisibility(View.VISIBLE);

                        TextView barcodeText = (TextView) findViewById(R.id.isbn13);
                        barcodeText.setText(barcode);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                showToast("Couldn't access API!");
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

    public void manualSearch()
    {
        EditText barcode = (EditText) findViewById(R.id.isbn);
        EditText isbn13 = (EditText) findViewById(R.id.isbn13);
        if(barcode.getText().toString().equals("") && isbn13.getText().toString().equals(""))
            showToast("ISBN fields empty!");
        else
        {
            if(barcode.getText().toString().equals(""))
            {
                String temp = isbn13.getText().toString();
                resetFields();
                getBook13(temp);
            }
            else
            {
                String temp = barcode.getText().toString();
                resetFields();
                getBook(temp);
            }
        }
    }
}
