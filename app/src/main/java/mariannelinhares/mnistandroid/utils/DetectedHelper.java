package mariannelinhares.mnistandroid.utils;

import android.graphics.Bitmap;

/**
 * Created by justsaul on 18.1.15.
 */

public class DetectedHelper {
    private String text;
    private Bitmap data;

    public DetectedHelper(String text, Bitmap data) {
        this.text = text;
        this.data = data;
    }

    public Bitmap getData() {
        return data;
    }

    public void setData(Bitmap data) {
        this.data = data;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
