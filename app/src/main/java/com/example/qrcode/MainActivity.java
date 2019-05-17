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

                            int xmin, ymin, xmax, ymax;
                            if(corners[3].x < corners[0].x)
                                xmin = corners[3].x;
                            else
                                xmin = corners[0].x;
                            if(corners[0].y < corners[1].y)
                                ymin = corners[0].y;
                            else
                                ymin = corners[1].y;
                            if(corners[1].x > corners[2].x)
                                xmax = corners[1].x;
                            else
                                xmax = corners[2].x;
                            if(corners[3].y > corners[2].y)
                                ymax = corners[3].y;
                            else
                                ymax = corners[2].y;




                            Bitmap resultingImage=Bitmap.createBitmap(bitmap,  xmin, ymin, xmax - xmin, ymax - ymin);

                            double angle = Math.toDegrees(Math.atan2(corners[3].x - corners[0].x, corners[3].y - corners[0].y));
                            // Keep angle between 0 and 360
                            angle = angle + Math.ceil( -angle / 360 ) * 360;

                            Matrix matrix = new Matrix();

                            double value = 0.0;
                            if(angle < 90){
                                value =  angle;
                            }else if(angle > 280){
                                value = -Math.round(360.0 - angle);
                            }

                            matrix.postRotate((float)value);

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultingImage, resultingImage.getWidth(), resultingImage.getHeight(), true);

                            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                            if(corners[0].y > corners[1].y)
                                rotatedBitmap = Bitmap.createBitmap(rotatedBitmap,  Integer.valueOf(corners[3].x) - Integer.valueOf(corners[0].x), Integer.valueOf(corners[0].y) - Integer.valueOf(corners[1].y), corners[1].x - corners[0].x, corners[3].y - corners[0].y);
                            else
                                rotatedBitmap = Bitmap.createBitmap(rotatedBitmap,  Integer.valueOf(corners[0].x) - Integer.valueOf(corners[3].x), Integer.valueOf(corners[1].y) - Integer.valueOf(corners[0].y), corners[1].x - corners[0].x, corners[3].y - corners[0].y);

                            imageView.setImageBitmap(rotatedBitmap);

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


}
