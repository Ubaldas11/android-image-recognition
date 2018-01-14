package mariannelinhares.mnistandroid;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mariannelinhares.mnistandroid.camera.CameraPreview;
import mariannelinhares.mnistandroid.models.Classification;
import mariannelinhares.mnistandroid.models.Classifier;
import mariannelinhares.mnistandroid.models.TensorFlowClassifier;

public class MainActivity  extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private static final int CAMERA_ID = 0;

    private CameraPreview mPreview;
    private Camera mCamera;

    private static final int INPUT_SIZE = 300;

    private static final String MODEL_FILE = "ssd_mobilenet_v1_android_export.pb";
    private static final String LABEL_FILE = "coco_labels_list.txt";

    private TextView resText;
    private Classifier classifier;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showCameraPreview();

        resText = (TextView) findViewById(R.id.tfRes);
        loadModel();

        button = (Button) findViewById(R.id.takePic);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        //classifyLoop();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

    private void loadModel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void takePicture() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.startPreview();
                if (data != null && data.length > 0) {
                    Bitmap bitmapFromCamera = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmapFromCamera, INPUT_SIZE, INPUT_SIZE, true);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap bitmap = Bitmap.createBitmap(bitmapScaled , 0, 0, bitmapScaled .getWidth(), bitmapScaled .getHeight(), matrix, true);
                    classify(bitmap);
                }
            }
        });
    }

    private void classifyLoop() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
            if (mCamera != null) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        if (b){
                            camera.takePicture(null, null, new Camera.PictureCallback() {
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    camera.startPreview();
                                    if (data != null && data.length > 0) {
                                        Bitmap bitmapFromCamera = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmapFromCamera, INPUT_SIZE, INPUT_SIZE, true);
                                        Matrix matrix = new Matrix();
                                        matrix.postRotate(90);
                                        Bitmap bitmap = Bitmap.createBitmap(bitmapScaled , 0, 0, bitmapScaled .getWidth(), bitmapScaled .getHeight(), matrix, true);
                                        classify(bitmap);
                                    }
                                }
                            });
                        }
                    }
                });

            }

            }
        }, 5000, 5000);

    }
    public void classify(Bitmap src) {
        String text = "";
        List<RectF> rects = new ArrayList<RectF>();
        Bitmap bit = src.copy(Bitmap.Config.ARGB_8888, true);

        final List<Classification> res = classifier.recognize(src);
        if (res == null)
            return;
        for (int i = 0; i < res.size(); i++)
            if (res.get(i).getConf() > 0.1f){
                // čia šitas listas yra visų atpažintų objektų nuotraukoje
                text += String.format("%s - %f\n", res.get(i).getLabel(), res.get(i).getConf());
                rects.add(res.get(i).getLocation());
            }
        resText.setText(text);
        drawOverlay(bit, rects);

    }
    private void drawOverlay(Bitmap bitmap, List<RectF> rects)
    {
        Bitmap bitmap2;
        ImageView myImage;

        if(findViewById(R.id.overlay_preview).findViewWithTag("ImageView") != null)
        {
            myImage = (ImageView) findViewById(R.id.overlay_preview).findViewWithTag("ImageView");
            bitmap2 = ((BitmapDrawable) myImage.getDrawable()).getBitmap();

        }else{
            bitmap2 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            myImage = new ImageView(this);
        }
        FrameLayout frr = (FrameLayout) findViewById(R.id.camera_preview);
        int w = frr.getWidth();
        int h = frr.getHeight();
        float n1 = w/300;
        float n2 = h/300;
        System.out.println(w + " " + h);
        Bitmap cbit = Bitmap.createScaledBitmap(bitmap2, w, h, true);
        Canvas canvas = new Canvas(cbit);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        for (RectF rec:rects) {
            canvas.drawRect(rec.left*n1,rec.top*n2,rec.right*n1+150,rec.bottom*n2,paint);
        }
        canvas.drawBitmap(cbit, new Matrix(), null);
        myImage.setImageBitmap(cbit);
        FrameLayout fr = (FrameLayout) findViewById(R.id.overlay_preview);
        fr.addView(myImage);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }

    private void showCameraPreview() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    private void startCamera() {
        mCamera = getCameraInstance(CAMERA_ID);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_ID, cameraInfo);
        if (mCamera == null || cameraInfo == null) {
            Toast.makeText(this, "Camera is not available.", Toast.LENGTH_SHORT).show();
        } else {
            final int displayRotation = getWindowManager().getDefaultDisplay()
                    .getRotation();
            mCamera.setDisplayOrientation(CameraPreview.calculatePreviewOrientation(cameraInfo, displayRotation));
            mPreview = new CameraPreview(this, mCamera, cameraInfo, displayRotation);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }

    private Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Toast.makeText(this, "Camera " + cameraId + " is not available: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        return c;
    }

}