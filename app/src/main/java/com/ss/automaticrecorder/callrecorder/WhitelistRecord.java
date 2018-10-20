package  com.ss.automaticrecorder.callrecorder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import com.ss.automaticrecorder.database.Database;
import com.ss.automaticrecorder.database.Whitelist;


public class WhitelistRecord {

    Whitelist whitelist;
    private Drawable draw;
     WhitelistRecord(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    public void setContactId(String contactId) {
        whitelist.setContactId(contactId);
    }

     String getContactId() {
        return whitelist.getContactId();
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(Drawable photo) {
        draw = photo;
    }



    /**
     * Get the Contact image from the cache...
     *
     * @return NULL if there isn't an Image in the cache
     */

    public Drawable getImage() {
        return draw;
    }

    public static boolean recordCaller(Context context, String phoneNumber, boolean defaultValue) {
        String contactId = null;

        // define the columns the query should return
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                // Get values from contacts database:
                contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                Whitelist contact = Database.getInstance(context).getContact(contactId);
                if (null != contact) return false;
            }
            cursor.close();

        }catch(Exception e){}
        return defaultValue;
        }

}
