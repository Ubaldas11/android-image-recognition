package mariannelinhares.mnistandroid.models;

import android.graphics.RectF;

/**
 * Created by marianne-linhares on 20/04/17.
 */

public class Classification {

    private float conf;
    private String label;
    private RectF location;

    Classification(float conf, String label, RectF location) {
        this.conf = conf;
        this.label = label;
        this.location = location;
    }

    public String getLabel() {
        return label;
    }

    public float getConf() {
        return conf;
    }

    public RectF getLocation() {
        return new RectF(location);
    }
}
