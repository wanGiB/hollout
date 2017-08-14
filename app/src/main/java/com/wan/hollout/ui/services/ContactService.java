package com.wan.hollout.ui.services;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.HolloutVCFParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Wan Clem
 */
public class ContactService {

    private Context context;

    public ContactService(Context context) {
        this.context = context;
    }

    @SuppressWarnings("ignored")
    public File vCard(Uri contactData) throws Exception {
        Cursor cursor = context.getContentResolver().query(contactData, null, null, null, null);
        cursor.moveToFirst();
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

        AssetFileDescriptor fd;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "CONTACT_" + timeStamp + "_" + ".vcf";

        File outputFile = HolloutUtils.getFilePath(imageFileName, context, "text/x-vcard");
        fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");

        FileInputStream fis = null;
        if (fd != null) {
            fis = fd.createInputStream();

            byte[] buf = new byte[(int) fd.getDeclaredLength()];
            fis.read(buf);
            String cvFdata = new String(buf);
            if (!HolloutVCFParser.validateData(cvFdata)) {
                HolloutLogger.d("vCard ::", cvFdata);
                throw new Exception("contact exported is not in proper format");
            }
            HolloutLogger.d(" data:", new String(buf));
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile.getAbsoluteFile());
            fileOutputStream.write(buf);
            fileOutputStream.close();
        }
        return outputFile;
    }

}
