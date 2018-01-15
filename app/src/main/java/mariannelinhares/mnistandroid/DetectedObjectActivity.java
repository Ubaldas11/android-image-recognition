package mariannelinhares.mnistandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mariannelinhares.mnistandroid.models.Classification;
import mariannelinhares.mnistandroid.models.Classifier;
import mariannelinhares.mnistandroid.models.TensorFlowClassifier;
import mariannelinhares.mnistandroid.utils.DetectedHelper;
import mariannelinhares.mnistandroid.utils.Tuple;

public class DetectedObjectActivity extends AppCompatActivity {
    private static final int INPUT_SIZE = 300;
    private static final int HEIGHT_ADJUSTMENT = 40;

    private static final String MODEL_FILE = "ssd_mobilenet_v1_android_export.pb";
    private static final String LABEL_FILE = "coco_labels_list.txt";

    private TextView resText;
    private Classifier classifier;

    public ImageView myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_object);

        loadModel();

        try {
            myImage = (ImageView) findViewById(R.id.imageView);

            Intent intent = getIntent();
            File imageFile = (File) intent.getExtras().get("BitmapImageFile");
            final Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            Bitmap bitmapScaled = Bitmap.createScaledBitmap(imageBitmap, 300, 300, true);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bitmap = Bitmap.createBitmap(bitmapScaled , 0, 0, bitmapScaled.getWidth(), bitmapScaled.getHeight(), matrix, true);

            DetectedHelper detectedHelper = classify(bitmap);

            myImage.setImageBitmap(detectedHelper.getData());

            resText = (TextView) findViewById(R.id.textView);
            resText.setText(detectedHelper.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadModel() {
        try {
            classifier = TensorFlowClassifier.create(
                    getAssets(),
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE);
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing classifiers!", e);
        }
    }

    public DetectedHelper classify(Bitmap src) {
        String text = "";
        List<RectF> rects = new ArrayList<RectF>();
        Bitmap bit = src.copy(Bitmap.Config.ARGB_8888, true);

        final List<Classification> res = classifier.recognize(src);
        if (res == null) {
            return new DetectedHelper("EMPTY", bit);
        }
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).getConf() > 0.1f) {
                // čia šitas listas yra visų atpažintų objektų nuotraukoje
                text += String.format("%s - %f\n", res.get(i).getLabel(), res.get(i).getConf());
                rects.add(res.get(i).getLocation());
            }
        }
        Tuple bitmapAndColors = produceOverlay(bit, rects);
        return new DetectedHelper(text, (Bitmap)bitmapAndColors.x);
    }

    private Tuple<Bitmap, List<Integer>> produceOverlay(Bitmap bitmap, List<RectF> rects) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        float n1 = w/300;
        float n2 = h/300;
        Bitmap cbit = Bitmap.createScaledBitmap(bitmap, w, h, true);
        Canvas canvas = new Canvas(cbit);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        List<Integer> colorList = new ArrayList<>();
        for(RectF rec:rects) {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            paint.setColor(color);
            colorList.add(color);
            canvas.drawRect(rec.left*n1,rec.top*n2 - HEIGHT_ADJUSTMENT,rec.right*n1,rec.bottom*n2 - HEIGHT_ADJUSTMENT,paint);
        }
        canvas.drawBitmap(cbit, new Matrix(), null);
        Tuple result = new Tuple(cbit, colorList);
        return result;
    }
}