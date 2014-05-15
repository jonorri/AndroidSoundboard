package com.hjortur.soundboard.adapters;

import android.content.Context;
import android.content.OperationApplicationException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.hjortur.soundboard.R;
import com.hjortur.soundboard.controls.SquareImageButton;
import com.hjortur.soundboard.utilities.RingtoneUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * The GridAdapter class is used to build the layout for the soundboard.
 * @author Hjörtur Líndal Stefánsson
 */
public class GridAdapter extends BaseAdapter {
    /**
     * A list of all the SoundItem instances that contain our data for each button
     */
    private final List<SoundItem> soundItems = new ArrayList<SoundItem>();
    /**
     * The layout inflater
     */
    private final LayoutInflater inflater;
    /**
     * The SoundPool is used for playing the audio
     */
    private final SoundPool soundPool;

    /**
     * Public constructor. Adds the data to our soundItems list and initialize the SoundPool
     * @param context
     */
    public GridAdapter(Context context) {
        inflater = LayoutInflater.from(context);

        soundItems.add(new SoundItem("Emergency", R.drawable.sirene1, R.raw.emergency));
        soundItems.add(new SoundItem("European", R.drawable.sirene2, R.raw.european));
        soundItems.add(new SoundItem("Fast Police",R.drawable.sirene3, R.raw.fast_police));
        soundItems.add(new SoundItem("Fire Truck", R.drawable.sirene1, R.raw.fire_truck));
        soundItems.add(new SoundItem("Police2", R.drawable.sirene2, R.raw.police_2));
        soundItems.add(new SoundItem("Police", R.drawable.sirene3, R.raw.police));
        soundItems.add(new SoundItem("Tornado", R.drawable.tornadosirene, R.raw.tornado));
        // Just add the same items again so we'll have enough to work with
        soundItems.add(new SoundItem("Emergency", R.drawable.sirene1, R.raw.emergency));
        soundItems.add(new SoundItem("European", R.drawable.sirene2, R.raw.european));
        soundItems.add(new SoundItem("Fast Police",R.drawable.sirene3, R.raw.fast_police));
        soundItems.add(new SoundItem("Fire Truck", R.drawable.sirene1, R.raw.fire_truck));
        soundItems.add(new SoundItem("Police2", R.drawable.sirene2, R.raw.police_2));
        soundItems.add(new SoundItem("Police", R.drawable.sirene1, R.raw.police));
        soundItems.add(new SoundItem("Tornado", R.drawable.tornadosirene, R.raw.tornado));

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    }

    /**
     * Get the count of all our sound buttons
     * @return The count of the soundItems list
     */
    @Override
    public int getCount() {
        return soundItems.size();
    }

    /**
     * Returns a specific item at a given position
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @return A SoundItem instance at a given position
     */
    @Override
    public Object getItem(int position) {
        return soundItems.get(position);
    }

    /**
     * Returns an item id for a given position
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @return The drawableId of the SoundItem instance (long)
     */
    @Override
    public long getItemId(int position) {
        return soundItems.get(position).drawableId;
    }

    /**
     * Overrides the getView method for the adapter. Here we set the data for the layout
     * as well as set up events for the onClick and onLongClick events
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param view The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Context context = null;
        SquareImageButton picture;
        TextView name;
        try
        {
            if (view == null) {
                view = inflater.inflate(R.layout.square_image_button, parent, false);
                if (view == null) {
                    throw new OperationApplicationException("Could not load view");
                }
                view.setTag(R.id.picture, view.findViewById(R.id.picture));
                view.setTag(R.id.text, view.findViewById(R.id.text));
            }
            // Initialize the controls
            picture = (SquareImageButton)view.getTag(R.id.picture);
            name = (TextView)view.getTag(R.id.text);
            context = view.getContext();
            SoundItem item = (SoundItem)getItem(position);

            if (picture != null) {
                picture.setImageResource(item.drawableId);
                picture.setFocusable(true);
                picture.setSoundClipId(item.soundFile);
                picture.setTitle(item.name);
                picture.setClickable(true);
                picture.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        playSound(arg0);
                    }
                });
                picture.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showPopupMenu(v);
                        return false;
                    }
                });
            }
            if (name != null) {
                name.setText(item.name);
            }
        }
        catch(Exception ex)
        {
            if(context != null) {
                Toast toast = Toast.makeText(context, "An error occurred: " + ex.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        return view;
    }

    /**
     * Plays a sound using the AudioManager class
     * @param view The square image button view that was clicked
     */
    private void playSound(View view) {
        Context context = view.getContext();
        SquareImageButton button = (SquareImageButton)view;
        final int soundId = soundPool.load(button.getContext(), button.getSoundClipId(), 1);
        AudioManager audioManager = null;
        if (context != null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        float actualVolume = 0;
        if (audioManager != null) {
            actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        float maxVolume = 0;
        if (audioManager != null) {
            maxVolume = (float) audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        final float volume = actualVolume / maxVolume;

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i2) {
                soundPool.play(soundId, volume, volume, 1, 0, 1f);
            }
        });
    }

    /**
     * Display the pop up menu when the image is pressed for a short while
     * @param view The square image button view that was clicked
     */
    private void showPopupMenu(View view){
        Context context = view.getContext();
    	final SquareImageButton imageView = (SquareImageButton)view;
        if(context != null){
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId()){
                        case R.id.saveNotification:
                            RingtoneUtilities.setTone(imageView, RingtoneManager.TYPE_NOTIFICATION);
                            break;
                        case R.id.saveRingtone:
                            RingtoneUtilities.setTone(imageView, RingtoneManager.TYPE_RINGTONE);
                            break;
                        default:
                            break;
                    }
                    return false;
                }

            });
            popupMenu.show();
        }
    }

    /**
     * A small wrapper class for each sound item
     */
    private class SoundItem {
        /**
         * The name
         */
        final String name;
        /**
         * The drawable ID
         */
        final int drawableId;
        /**
         * The sound file
         */
        final int soundFile;

        /**
         * public constructor
         * @param name The name
         * @param drawableId The drawable ID
         * @param soundFile The sound file
         */
        public SoundItem(String name, int drawableId, int soundFile) {
            this.name = name;
            this.drawableId = drawableId;
            this.soundFile = soundFile;
        }
    }
}
