package com.example.qrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.myimg);
        FirebaseApp.initializeApp(MainActivity.this);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aaaa);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        scanBarcodes(image);
    }


    private void scanBarcodes(FirebaseVisionImage image) {
        // [START set_detector_options]
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_AZTEC)
                        .build();
        // [END set_detector_options]

        // [START get_detector]
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();
        // Or, to specify the formats to recognize:
        // FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
        //        .getVisionBarcodeDetector(options);
        // [END get_detector]

        // [START run_detector]
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            Bitmap resultingImage=Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                            int xmin, ymin, xmax, ymax;
                            xmin = corners[0].x;
                            ymin = corners[1].y;
                            xmax = corners[1].x;
                            ymax = corners[2].y;
                            //Bitmap cropedImg = Bitmap.createBitmap(bitmap, xmin, ymin, xmax - xmin, ymax - ymin);
                            Canvas canvas = new Canvas(resultingImage);

                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            Path path=new Path();
                            path.lineTo(corners[0].x, corners[0].y);
                            path.lineTo(corners[1].x, corners[1].y);
                            path.lineTo(corners[2].x, corners[2].y);
                            path.lineTo(corners[3].x, corners[3].y);
                            path.lineTo(corners[0].x, corners[0].y);

                            canvas.drawPath(path, paint);

                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                            canvas.drawBitmap(bitmap, 0, 0, paint);
                            //Bitmap cropedImg = Bitmap.createBitmap(resultingImage, xmin, ymin, xmax - xmin, ymax - ymin);
                            //imageView.setImageBitmap(resultingImage);
                            //imageView.setImageBitmap(resultingImage);


                            double angle = Math.toDegrees(Math.atan2(corners[3].x - corners[0].x, corners[3].y - corners[0].y));
                            // Keep angle between 0 and 360
                            angle = angle + Math.ceil( -angle / 360 ) * 360;

                            Toast.makeText(MainActivity.this, Math.round(360.0 - angle) + " : " + angle, Toast.LENGTH_LONG).show();
                            Matrix matrix = new Matrix();

                            String val = Math.round(360.0 - angle) + "";
                            double value = 0.0;
                            if(angle < 90){
                                value =  angle;
                            }else if(angle > 280){
                                value = -Math.round(360.0 - angle);
                            }

                            Toast.makeText(MainActivity.this, value + "", Toast.LENGTH_LONG).show();

                            matrix.postRotate((float)value);

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultingImage, resultingImage.getWidth(), resultingImage.getHeight(), true);

                            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                            //Bitmap cropedImg = Bitmap.createBitmap(rotatedBitmap, xmin, ymin, xmax - xmin, ymax - ymin);

                            imageView.setImageBitmap(rotatedBitmap);

                            /*
                            int xmin, ymin, xmax, ymax;
                            xmin = corners[0].x;
                            ymin = corners[1].y;
                            xmax = corners[1].x;
                            ymax = corners[2].y;

                            try {
                                Bitmap cropedImg = Bitmap.createBitmap(bitmap, xmin, ymin, xmax - xmin, ymax - ymin);
                                cropedImg = Bitmap.createScaledBitmap(cropedImg, 500, 500, true);
                                getWindow().setFormat(PixelFormat.RGBA_8888);
                                Toast.makeText(MainActivity.this, "Width" + cropedImg.getWidth() +"height" +cropedImg.getHeight(), Toast.LENGTH_SHORT).show();
                                imageView.setImageBitmap(cropedImg);
                            }catch (Exception ex){
                            }
                            */

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Toast.makeText(MainActivity.this, "Failed to read image", Toast.LENGTH_SHORT).show();
                    }
                });
        // [END run_detector]
    }

    public static double calculateAngle(double x1, double y1, double x2, double y2)
    {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil( -angle / 360 ) * 360;

        return angle;
    }


}
