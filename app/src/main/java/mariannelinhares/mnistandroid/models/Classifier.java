package mariannelinhares.mnistandroid.models;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Piasy{github.com/Piasy} on 29/05/2017.
 */
public interface Classifier {
    List<Classification> recognize(final Bitmap image);
}
