package comsgosiaco.github.library;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements exportEmailDialogFragment.exportEmailDialogListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private Class<?> mClss;
    public static ActionBarDrawerToggle toggle;
    private DBHelper librarydb;
    private static String LOANED_DIR = Environment.getExternalStorageDirectory()
            + File.separator + DBHelper.FILE_DIR
            + File.separator + "loaned.csv";
    private static String AVAIL_DIR = Environment.getExternalStorageDirectory()
            + File.separator + DBHelper.FILE_DIR
            + File.separator + "available.csv";
    //private ArrayList array_list;
    //private ArrayAdapter<String> arrayAdapter;
    //private ListView obj;

    //TO DO List
    //add search to all tabs
    //alert if adding duplicate book

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(addBook.class);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        librarydb = new DBHelper(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment, new libraryFragment());
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        <item
android:id="@+id/action_settings"
android:orderInCategory="100"
android:title="@string/action_settings"
app:showAsAction="never" />
        */
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.library) {
            //showToast("Library");
            FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment, new libraryFragment());
            ft.commit();
        } else if (id == R.id.loanbook) {
            //showToast("Loan");
            FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment, new loanFragment());
            ft.commit();
        } else if (id == R.id.returnbook) {
            //showToast("Return");
            FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment, new returnFragment());
            ft.commit();
        } else if (id == R.id.exportall) {
            //DialogFragment fragment = new exportEmailDialogFragment();
            //fragment.show(getFragmentManager(), "exportall");
            doLaunchContactPicker(this.findViewById(android.R.id.content), 1001);
            //showToast("Export all");
        } else if (id == R.id.exportcheckedout) {
            doLaunchContactPicker(this.findViewById(android.R.id.content), 1002);
            //showToast("Export loaned");
        } else if (id == R.id.exportavailable) {
            doLaunchContactPicker(this.findViewById(android.R.id.content), 1003);
            //showToast("Export available");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, CharSequence email) {
        showToast(email);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZBAR_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String email = "";
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
                        }
                    } catch (Exception e) {
                        showToast("Failed to get email data");
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
                            //do something with email here
                            String[] address = {email};
                            composeEmail(address, "Export all books", Uri.fromFile(new File(Environment.getExternalStorageDirectory()
                                    + File.separator + DBHelper.FILE_DIR
                                    + File.separator + DBHelper.DATABASE_NAME)));
                        }
                    }

                    break;
                case 1002: //Export loaned
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
                        }
                    } catch (Exception e) {
                        showToast("Failed to get email data");
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
                            //do something with email here
                            try {
                                Cursor cs = librarydb.getAllLoanedBooksCursor();
                                FileWriter fw = new FileWriter(LOANED_DIR);
                                
                                fw.append("id");
                                fw.append(',');

                                fw.append("title");
                                fw.append(',');

                                fw.append("author");
                                fw.append(',');

                                fw.append("publisher");
                                fw.append(',');

                                fw.append("year");
                                fw.append(',');

                                fw.append("isbn");
                                fw.append(',');

                                fw.append("isbn13");
                                fw.append(',');

                                fw.append("loaned");
                                fw.append(',');

                                fw.append("loanee");
                                fw.append(',');

                                fw.append("email");
                                fw.append(',');

                                fw.append("date");

                                fw.append('\n');

                                if (cs.moveToFirst()) {
                                    do {
                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ID)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_TITLE)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_AUTHOR)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_PUBLISHER)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_YEAR)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN13)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_LOANED)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_LOANEE)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_EMAIL)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_DATE)));

                                        fw.append('\n');

                                    } while (cs.moveToNext());
                                }
                                if (cs != null && !cs.isClosed()) {
                                    cs.close();
                                }
                                fw.close();

                            } catch (Exception e) {
                            }

                            String[] address = {email};
                            composeEmail(address, "Export loaned books", Uri.fromFile(new File(LOANED_DIR)));
                        }
                    }

                    break;
                case 1003: //Export available
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
                        }
                    } catch (Exception e) {
                        showToast("Failed to get email data");
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
                            try {
                                Cursor cs = librarydb.getAllAvailableBooksCursor();
                                FileWriter fw = new FileWriter(AVAIL_DIR);

                                fw.append("id");
                                fw.append(',');

                                fw.append("title");
                                fw.append(',');

                                fw.append("author");
                                fw.append(',');

                                fw.append("publisher");
                                fw.append(',');

                                fw.append("year");
                                fw.append(',');

                                fw.append("isbn");
                                fw.append(',');

                                fw.append("isbn13");
                                fw.append(',');

                                fw.append("loaned");
                                fw.append(',');

                                fw.append("loanee");
                                fw.append(',');

                                fw.append("email");
                                fw.append(',');

                                fw.append("date");

                                fw.append('\n');

                                if (cs.moveToFirst()) {
                                    do {
                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ID)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_TITLE)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_AUTHOR)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_PUBLISHER)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_YEAR)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_ISBN13)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_LOANED)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_LOANEE)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_EMAIL)));
                                        fw.append(',');

                                        fw.append(cs.getString(cs.getColumnIndex(DBHelper.COLUMN_DATE)));

                                        fw.append('\n');

                                    } while (cs.moveToNext());
                                }
                                if (cs != null && !cs.isClosed()) {
                                    cs.close();
                                }
                                fw.close();

                            } catch (Exception e) {
                            }

                            String[] address = {email};
                            composeEmail(address, "Export available books", Uri.fromFile(new File(AVAIL_DIR)));
                        }
                    }

                    break;
            }

        } else {
            //showToast("Warning: activity result not ok");
        }
    }

    private boolean isValidEmail(CharSequence email)
    {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public void composeEmail(String[] addresses, String subject, Uri attachment) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
