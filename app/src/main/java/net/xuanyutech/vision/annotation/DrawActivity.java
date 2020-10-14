package net.xuanyutech.vision.annotation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.steelkiwi.cropiwa.image.CropIwaBitmapManager;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.objectdetection.StaticObjectDetector;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DrawActivity extends AppCompatActivity implements Callback, CropIwaBitmapManager.BitmapLoadListener {

    private DrawView drawView;
    private View progressView, errorView;
    private String originalClassName, objId;
    private Uri imageUri;
    private Menu options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.appBar).bringToFront();
        progressView = findViewById(R.id.draw_progress_view);
        errorView = findViewById(R.id.draw_error_view);
        errorView.findViewById(R.id.retry_txt).setOnClickListener(view -> {
            errorView.setVisibility(View.GONE);
            setupDrawView(imageUri);
        });

        String[] classes = getIntent().getStringArrayExtra("classes");
        int[] box = getIntent().getIntArrayExtra("box");
        originalClassName = getIntent().getStringExtra("class");
        objId = getIntent().getStringExtra("id");
        if (objId != null && !objId.isEmpty())
            if (originalClassName == null || originalClassName.isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        String uri = getIntent().getStringExtra("uri");
        if (uri == null || uri.isEmpty()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        imageUri = Uri.parse(uri);

        drawView = findViewById(R.id.draw_view);
        if (box == null || box.length < 4) {
            try {
                new StaticObjectDetector().detect(this, imageUri).addOnSuccessListener(firebaseVisionObjects -> {
                    setupDrawView(imageUri);
                    if (firebaseVisionObjects.size() > 0) {
                        Rect rect = firebaseVisionObjects.get(0).getBoundingBox();
                        drawView.setImageCropRect(new RectF(rect));
                    }
                }).addOnFailureListener(e -> setupDrawView(imageUri));
            } catch (IOException e) {
                e.printStackTrace();
                setupDrawView(imageUri);
            }
        } else {
            setupDrawView(imageUri);
            RectF rect = new RectF(box[0], box[1], box[2], box[3]);
            if (!rect.isEmpty())
                drawView.setImageCropRect(rect);
        }

        if (classes != null) {
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(
                            this,
                            R.layout.dropdown_menu_popup_item,
                            classes);
            drawView.setClassAdapter(adapter);
            drawView.setClass(originalClassName);
        }
    }

    private void setupDrawView(Uri imageUri) {
        progressView.setVisibility(View.VISIBLE);
        drawView.setImageUri(imageUri, this);
        drawView.configureImage()
                .setMinScale(0.1f)
                .setMaxScale(3f)
                .apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        options = menu;
        for (int i = 0; i < options.size(); i++) {
            Drawable drawable = options.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    private byte[] getImageDataFromURI(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] file = stream.toByteArray();
        stream.close();
        inputStream.close();
        return file;
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        onBackPressed();
        return true;
    }

    private void save() throws IOException {
        String name = drawView.getSelectedClass();
        OkHttpClient client = new OkHttpClient();
        Rect rect = drawView.getCropRect();
        if (name.isEmpty())
            return;
        String url;
        RequestBody requestBody;
        if (objId == null || objId.isEmpty()) {
            // For new sample
            url = Utils.makeUrl(this, "/data/" + name);
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", "img.jpg", RequestBody.create(getImageDataFromURI(imageUri), MediaType.parse("image/jpg")))
                    .addFormDataPart("box", String.valueOf(rect.left))
                    .addFormDataPart("box", String.valueOf(rect.top))
                    .addFormDataPart("box", String.valueOf(rect.right))
                    .addFormDataPart("box", String.valueOf(rect.bottom))
                    .build();
        } else {
            // Edit sample
            url = Utils.makeUrl(this, "/data/" + originalClassName + "/" + objId);
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("new_name", name)
                    .addFormDataPart("new_box", String.valueOf(rect.left))
                    .addFormDataPart("new_box", String.valueOf(rect.top))
                    .addFormDataPart("new_box", String.valueOf(rect.right))
                    .addFormDataPart("new_box", String.valueOf(rect.bottom))
                    .build();
        }
        Request request = new Request.Builder()
                .url(url).post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.draw_save:
                if (drawView.getSelectedClass().isEmpty()) {
                    Snackbar.make(drawView, R.string.error_select_class, Snackbar.LENGTH_LONG).show();
                    break;
                }
                progressView.setVisibility(View.VISIBLE);
                try {
                    save();
                } catch (IOException e) {
                    e.printStackTrace();
                    progressView.setVisibility(View.GONE);
                    Snackbar.make(drawView, R.string.file_not_found, Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.draw_delete:
                new MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.text_delete_object)
                        .setPositiveButton(R.string.text_ok, (dialogInterface, i) -> {
                            progressView.setVisibility(View.VISIBLE);
                            delete();
                        })
                        .setNegativeButton(R.string.text_cancel, null)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnWithResult(int result) {
        Intent intent = new Intent();
        intent.putExtra("class", drawView.getSelectedClass());
        setResult(result, intent);
        finish();
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        runOnUiThread(() -> {
            progressView.setVisibility(View.GONE);
            Snackbar.make(drawView, R.string.network_error, Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        runOnUiThread(() -> progressView.setVisibility(View.GONE));
        if (response.code() != 200) {
            runOnUiThread(() -> Snackbar.make(drawView, R.string.server_error, Snackbar.LENGTH_LONG).show());
            return;
        }
        String ret = response.body().string();
        try {
            final JSONObject result = new JSONObject(ret);
            if (result.getInt("code") == 0) {
                returnWithResult(RESULT_OK);
            } else {
                runOnUiThread(() -> {
                    try {
                        Snackbar.make(drawView, result.getString("msg"), Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void delete() {
        if (objId != null && !objId.isEmpty()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Utils.makeUrl(this, "/data/" + originalClassName + "/" + objId)).delete()
                    .build();
            Call call = client.newCall(request);
            call.enqueue(this);
        } else {
            int ret = getContentResolver().delete(imageUri, null, null);
            if (ret > 0) {
                returnWithResult(RESULT_OK);
            } else {
                progressView.setVisibility(View.GONE);
                Snackbar.make(drawView, R.string.error_delete_file, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBitmapLoaded(Uri uri, Bitmap bitmap) {
        progressView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        for (int i = 0; i < options.size(); i++) {
            Drawable drawable = options.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.clearColorFilter();
            }
            options.getItem(i).setEnabled(true);
        }
    }

    @Override
    public void onLoadFailed(Throwable e) {
        progressView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }
}