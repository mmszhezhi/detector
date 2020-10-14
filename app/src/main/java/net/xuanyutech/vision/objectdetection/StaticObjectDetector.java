package net.xuanyutech.vision.objectdetection;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.io.IOException;
import java.util.List;

public class StaticObjectDetector {

    private FirebaseVisionObjectDetector objectDetector;

    public StaticObjectDetector() {
        FirebaseVisionObjectDetectorOptions options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .build();
        objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
    }

    public Task<List<FirebaseVisionObject>> detect(Context context, Uri uri) throws IOException {
        FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(context, uri);
        return objectDetector.processImage(image);
    }
}
