package mariannelinhares.mnistandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class DetectedObjectActivity extends AppCompatActivity {

    public ImageView myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_object);

        Bitmap bmp;
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        myImage = (ImageView) findViewById(R.id.imageView);

        myImage.setImageBitmap(bmp);
        System.out.println("yest");
    }

}
