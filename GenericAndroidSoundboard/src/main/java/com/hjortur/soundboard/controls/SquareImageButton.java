package com.hjortur.soundboard.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * The SquareImageButton class is an extension of the ImageButton. It contains
 * extra properties to be able to hold a sound clip ID and a title
 * @author Hjörtur Líndal Stefánsson
 */
public class SquareImageButton extends ImageButton {
    /**
     * The ID of the sound clip
     */
    private int soundClipId;

    /**
     * The title of the sound clip
     */
	private String title;

    /**
     * Public constructor.
     * @param context The context
     */
    public SquareImageButton(Context context) {
        super(context);
    }

    /**
     * Public constructor
     * @param context The context
     * @param attrs The attribute set
     */
    public SquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Public constructor
     * @param context The context
     * @param attrs The attribute set
     * @param defStyle An attribute in the current theme that contains a reference to a style resource to apply to this view
     */
    public SquareImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Override the onMeasure function. This is used to resize the images according to
     * the screen size
     * @param widthMeasureSpec The width
     * @param heightMeasureSpec The height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Snap to width
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    /**
     * Gets the sound clip id
     * @return The value of the soundClipId variable
     */
    public int getSoundClipId(){
        return this.soundClipId;
    }

    /**
     * Sets the sound clip id
     * @param clip The id of the sound clip that is assigned to the soundClipId variable
     */
    public void setSoundClipId(int clip){
    	this.soundClipId = clip;
    }

    /**
     * Gets the sound clip title
     * @return The value of the title variable
     */
    public String getTitle(){
    	return this.title;
    }

    /**
     * Sets the sound clip title
     * @param title The title of the sound clip that is assigned to the title variable
     */
    public void setTitle(String title){
    	this.title = title;
    }
}
