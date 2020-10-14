package net.xuanyutech.vision.training;

import android.os.Bundle;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ModelInfoActivity extends AppCompatActivity {
    private String[] classes = null;
    private TextInputEditText namev = null;
    private TextInputEditText briefv = null;
    private TextInputEditText detailv = null;
    private FloatingActionButton run = null;
    private String isSuccess = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_info);
        ChipGroup reflowGroup = findViewById(R.id.reflow_group);
        classes = getIntent().getStringArrayExtra("classes");
        initChipGroup(reflowGroup);
        namev = findViewById(R.id.model_name);
        briefv = findViewById(R.id.brief_des);
        detailv = findViewById(R.id.detail_des);
//
//        FloatingActionButton fab = findViewById(R.id.cat_chip_group_refresh);
//        fab.setOnClickListener(
//                v -> {
//                    initChipGroup(reflowGroup);
//                });
        run = findViewById(R.id.fab2);

        run.setOnClickListener(
                v -> {
                    String name = namev.getText().toString();
                    if(name!=null &&!name.equals("")&&name.equals(isSuccess)){
                        Toast.makeText(this, R.string.training_post_error_tip, Toast.LENGTH_SHORT).show();
                    }
                    String brief = briefv.getText().toString();
                    String detail = detailv.getText().toString();
                    String[] classes = getcheckedchips(reflowGroup);
                    if(name!=null &&!name.equals("")&&classes!=null&&classes.length>0){
                        tariningModel(classes,name,brief,detail);
                    }
                });
    }



    public String[] getcheckedchips(ChipGroup reflowGroup) {

        ArrayList<String> strList = new ArrayList<String>();
        int chipsCount = reflowGroup.getChildCount();

        int i = 0;
        while (i < chipsCount) {
            Chip chip = (Chip) reflowGroup.getChildAt(i);
            if (chip.isChecked() ) {
                String cls = (String) chip.getText();
                strList.add(cls);
            }
            i++;
        };
        return strList.toArray(new String[strList.size()]);
    }



    private String[] getCheckedClass(ChipGroup reflowGroup){
        List<Integer> ls =  reflowGroup.getCheckedChipIds();
        ArrayList<String> strList = new ArrayList<String>();
        for(Integer i :ls){
            Chip c = (Chip) reflowGroup.getChildAt(i);
            String cls = (String) c.getText();
            strList.add(cls);
        }
        return strList.toArray(new String[strList.size()]);
    }




    private void initChipGroup(ChipGroup chipGroup) {
        chipGroup.removeAllViews();
        for (String text : classes) {
            Chip chip =
                    (Chip) getLayoutInflater().inflate(R.layout.cat_chip_group_item_filter, chipGroup, false);
            chip.setText(text);
            chip.setCheckable(true);
            chip.setCloseIconVisible(false);
            chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
            chipGroup.addView(chip);
        }
    }

    private void tariningModel(String[] classes,String name,String brief,String detail) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("brief", brief)
                .addFormDataPart("detail", detail);
        for(String i:classes){
            mb.addFormDataPart("classes",i);
        }
        RequestBody requestBody = mb.build();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(this, "/training"))
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String raw = response.body().string();
                try {
                    JSONObject jo = new JSONObject(raw);
                    if(jo.getInt("code")==0){
                        runOnUiThread(() -> {
                            run.setImageResource(R.drawable.ic_baseline_check_24);
                            isSuccess = namev.getText().toString();
                        });
                    }else {
                        runOnUiThread(() -> {
                            run.setImageResource(R.drawable.ic_baseline_redo_24);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}