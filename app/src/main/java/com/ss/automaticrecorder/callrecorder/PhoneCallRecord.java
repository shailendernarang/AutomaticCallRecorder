package  com.ss.automaticrecorder.callrecorder;

import android.graphics.drawable.Drawable;

import com.ss.automaticrecorder.database.CallLog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class PhoneCallRecord {

    // Cache of Contact Pictures to minimize image memory use...
    private static Map<String, Drawable> synchronizedMap = Collections.synchronizedMap(new HashMap<String, Drawable>());

    private CallLog phoneCall;

     PhoneCallRecord(CallLog phoneCall) {
        this.phoneCall = phoneCall;
    }

    public void setImage(Drawable photo) {
        synchronizedMap.put(phoneCall.getPhoneNumber(), photo);
    }

    /**
     * Get the Contact image from the cache...
     *
     * @return NULL if there isn't an Image in the cache
     */
    public Drawable getImage() {
        Drawable draw = synchronizedMap.get(phoneCall.getPhoneNumber());
        return draw;
    }

    private String contactId;

    void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactId() {
        return contactId;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if(null==name){
            return phoneCall.getPhoneNumber();
        }
        return name;
    }

    CallLog getPhoneCall() {
        return phoneCall;
    }
}
