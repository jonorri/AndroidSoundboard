package com.hjortur.soundboard.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.hjortur.soundboard.R;
import com.hjortur.soundboard.controls.SquareImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A class that is used to save a ringtone to the device
 * @author Hjörtur Líndal Stefánsson
 */
public class RingtoneUtilities {
    /**
     * Saves a ringtone to the device storage
     * @param button The SquareImageButton instance that is being used
     * @param toneType The type of tone we are setting (alert or ringtone)
     */
    public static void setTone(SquareImageButton button, int toneType) {
        // Get the context for the image button
        Context context = button.getContext();
        // The internal uri for our resource
        Uri newUri = null;
        String path = getDirectoryPath();

        Resources resources = (context != null) ? context.getResources() : null;
        if(resources != null) {
            String entryName = context.getResources().getResourceEntryName(button.getSoundClipId());
            File file = new File(path + "/", entryName + ".mp3");

            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + entryName);
            ContentResolver resolver = context.getContentResolver();
            if(!file.exists()){
                newUri = saveFileAndAddToMediaStore(button, context, file, uri, resolver);
            }
            try {
                if(newUri == null){
                    newUri = getAudioUriFromFilePath(file.getAbsolutePath(), resolver);
                }
                if(newUri != null){
                    RingtoneManager.setActualDefaultRingtoneUri(context,
                            toneType, newUri);
                    Settings.System.putString(resolver, getStringByType(toneType),
                            newUri.toString());
                    //show the message to the user
                    CharSequence text = "";
                    switch(toneType){
                        case RingtoneManager.TYPE_NOTIFICATION:
                            text = "Notification tone set";
                            break;
                        case RingtoneManager.TYPE_RINGTONE:
                            text = "Ringtone set";
                            break;
                    }
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

            } catch (Throwable t) {
                Toast toast = Toast.makeText(context, "An error occurred: " + t.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /**
     * Saves the sound file to the device storage
     * @param button The image button that was clicked
     * @param context The context we are working in
     * @param file The file that needs to be saved
     * @param uri Uri to the sound file in our application
     * @param resolver The ContentResolver
     * @return An Uri to the newly saved file
     */
    private static Uri saveFileAndAddToMediaStore(SquareImageButton button, Context context, File file, Uri uri, ContentResolver resolver) {
        // This will hold the uri of the newly saved file
        Uri newUri;
        // A file descriptor of an entry in the AssetManager
        AssetFileDescriptor assetFileDescriptor;

        try {
            // obtain read access to the file descriptor in the Asset manager
            assetFileDescriptor = resolver.openAssetFileDescriptor(uri, "r");
        }
        catch (FileNotFoundException e) {
            // The sound file could not be found and needs to be saved.
            assetFileDescriptor = null;
        }

        saveFileToDevice(context, file, assetFileDescriptor);

        newUri = insertSoundFileToMediaStore(button, file, resolver);
        return newUri;
    }

    /**
     * Reads a file that is embedded in our application and writes it to the device storage
     * @param context
     * @param file
     * @param assetFileDescriptor
     */
    private static void saveFileToDevice(Context context, File file, AssetFileDescriptor assetFileDescriptor) {
        // The output stream is used to write the new file to the device storage
        FileOutputStream outputStream = null;
        // The input stream is used for reading the file that is embedded in our application
        FileInputStream inputStream = null;
        try {
            byte[] buffer = new byte[1024];
            // Create the input stream
            inputStream = (assetFileDescriptor != null) ? assetFileDescriptor.createInputStream() : null;
            // Create the output stream
            outputStream = new FileOutputStream(file, false);
            // Read the file into buffer
            int i = (inputStream != null) ? inputStream.read(buffer) : 0;
            // Continue writing and reading the file until we reach the end
            while (i != -1) {
                outputStream.write(buffer, 0, i);
                i = (inputStream != null) ? inputStream.read(buffer) : 0;
            }

            outputStream.flush();
        } catch (IOException io) {
            // Display a message to the user
            Toast toast = Toast.makeText(context, "Could not save the file", Toast.LENGTH_SHORT);
            toast.show();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    // We should really never get this far, but we might consider adding a
                    // warning/log entry here...
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    // We should really never get this far, but we might consider adding a
                    // warning/log entry here...
                }
            }
        }
    }

    /**
     * Inserts the sound file into the Android Media Store. This is needed so we can use it as
     * a ringtone, alert or notification
     * @param button
     * @param file
     * @param contentResolver
     * @return
     */
    private static Uri insertSoundFileToMediaStore(SquareImageButton button, File file, ContentResolver contentResolver) {
        Uri newUri;
        ContentValues values = new ContentValues();

        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, button.getTitle());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.MediaColumns.SIZE, file.length());
        values.put(MediaStore.Audio.AudioColumns.ARTIST, R.string.app_name);
        values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, true);
        values.put(MediaStore.Audio.AudioColumns.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.AudioColumns.IS_ALARM, true);
        values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        newUri = contentResolver.insert(uri, values);
        return newUri;
    }

    /**
     * Takes care of creating a directory for our application on the device's storage and
     * then returns the full path to it
     * @return The full path to the directory where our sound files will be stored
     */
    private static String getDirectoryPath() {
        // Replace the path here with your own
        File file = new File(Environment.getExternalStorageDirectory(),
                "/GenericAndroidSoundboard/Audio/");
        // Create the directory if it doesn't exist
        if (!file.exists()) {
            file.mkdirs();
        }

        // Get the path to the newly created directory
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/GenericAndroidSoundboard/Audio/";
    }

    /**
     * Gets the Uri to a specific audio file
     * @param filePath The path of the file that we are looking up
     * @param contentResolver The content resolver that is used to perform the query
     * @return The Uri of the sound file
     */
    private static Uri getAudioUriFromFilePath(String filePath, ContentResolver contentResolver) {
        long audioId;
        Uri uri = MediaStore.Audio.Media.getContentUri("external");
        String[] projection = {BaseColumns._ID};
        Cursor cursor = contentResolver.query(uri, projection, MediaStore.MediaColumns.DATA + " LIKE ?", new String[] { filePath }, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            audioId = cursor.getLong(columnIndex);
            cursor.close();
            return Uri.parse(uri.toString() + "/"+ audioId);
        }
        return null;
    }

    /**
     * Gets a specific system string based on a ringtone type
     * @param type The ringtone type
     * @return A string representing the ringtone type
     */
    private static String getStringByType(int type)
    {
        switch(type){
            case RingtoneManager.TYPE_ALARM:
                return Settings.System.ALARM_ALERT;
            case RingtoneManager.TYPE_NOTIFICATION:
                return Settings.System.NOTIFICATION_SOUND;
            case RingtoneManager.TYPE_RINGTONE:
                return Settings.System.RINGTONE;
            default:
                return null;
        }
    }
}
