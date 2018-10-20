package  com.ss.automaticrecorder.callrecorder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ss.automaticrecorder.R;


public class WhitelistActivity extends AppCompatActivity implements WhitelistFragment.OnListFragmentInteractionListener {

    Menu optionsMenu;
    WhitelistFragment fragment;

    private static final int PERMISSION_REQUEST_CODE = 200;
    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissionStorage()) {

            requestPermission();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_whitelist, menu);
        fragment = (WhitelistFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        optionsMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            fragment.addContact();
            return true;
        }

        if (id == R.id.action_delete) {
            fragment.removeSelectedContacts();
            return true;
        }

        return false;
    }

    @Override
    public void onListFragmentInteraction(WhitelistRecord[] item) {
        MenuItem menuItem = optionsMenu.findItem(R.id.action_delete);
        menuItem.setVisible(item.length > 0);
    }

    public boolean checkPermissionStorage() {

        int result = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS);


        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {


                    boolean readStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                    if (readStorage) {


                    } else {


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                                showMessageOKCancel("You need to allow access to this permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(WhitelistActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
