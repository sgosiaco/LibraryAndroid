package comsgosiaco.github.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

public class addBook extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void scan(View view)
    {
        resetFields();
        Intent i = new Intent(addBook.this, FullScannerActivity.class);
        startActivityForResult(i, 1);
    }

    public void add(View view)
    {
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
                String barcode = data.getStringExtra("barcode");
                TextView text = (TextView) findViewById(R.id.isbn);
                text.setText(barcode);
                TextView temp = (TextView) findViewById(R.id.textView2);
                temp.setText(barcode);
                String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + barcode + "&key=AIzaSyA1s6tiC-9EniXZlcjYYorLymwbc854sKU";
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(url, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                        String json = new String(responseBody);
                        try {
                            JSONObject object = new JSONObject(json);
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

                                int date = volumeInfo.getInt("publishedDate");
                                TextView dateText = (TextView) findViewById(R.id.year);
                                dateText.setText("" + date);

                                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                String imageLink = imageLinks.getString("smallThumbnail");
                                WebView cover = (WebView) findViewById(R.id.webView);
                                cover.loadUrl(imageLink);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                        Context context = getApplicationContext();
                        CharSequence text = "Couldn't find the isbn!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Context context = getApplicationContext();
                CharSequence text = "Wrong type of barcode!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }//onActivityResult
}
