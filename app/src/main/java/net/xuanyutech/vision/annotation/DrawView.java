package net.xuanyutech.vision.annotation;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.ListAdapter;

import com.google.android.material.textfield.TextInputLayout;
import com.steelkiwi.cropiwa.CropIwaOverlayView;
import com.steelkiwi.cropiwa.CropIwaView;

import net.xuanyutech.vision.R;

public class DrawView extends CropIwaView implements CropIwaOverlayView.OnDrawListener {

    private TextInputLayout inputLayout;
    private AutoCompleteTextView autoCompleteTextView;

    public DrawView(Context context) {
        super(context);
        initViews();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        inputLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutInputView();
    }

    private void initViews() {
        inputLayout = (TextInputLayout) LayoutInflater.from(getContext()).inflate(R.layout.dropdown_text_input_view, this, false);
        autoCompleteTextView = inputLayout.findViewById(R.id.filled_exposed_dropdown);
        addView(inputLayout);
        overlayView.setOnDrawListener(this);
    }

    public <T extends ListAdapter & Filterable> void setClassAdapter(T adapter) {
        autoCompleteTextView.setAdapter(adapter);
    }

    public String getSelectedClass() {
        return autoCompleteTextView.getText().toString();
    }

    public void setClass(String name) {
        autoCompleteTextView.setText(name, false);
    }

    private void layoutInputView() {
        int margin = 10;
        RectF cropRect = overlayView.getCropRect();
        if (cropRect.isEmpty()) {
            inputLayout.setVisibility(GONE);
            return;
        }
        inputLayout.setVisibility(VISIBLE);
        double center_x = (cropRect.left + cropRect.right) / 2;
        int y = (int) (cropRect.bottom + margin);
        int x = (int) (center_x - inputLayout.getMeasuredWidth() / 2);
        if (y + inputLayout.getMeasuredHeight() > getMeasuredHeight()) {
            y = (int) (cropRect.top - inputLayout.getMeasuredHeight() - margin);
        }
        inputLayout.layout(x, y,x + inputLayout.getMeasuredWidth(), y + inputLayout.getMeasuredHeight());
    }

    @Override
    public void onDrawCompleted() {
        layoutInputView();
    }

    public boolean isOnInputLayout(float x, float y) {
        return x >= inputLayout.getLeft() && x <= inputLayout.getRight() && y >= inputLayout.getTop() && y <= inputLayout.getBottom();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean r = super.onInterceptTouchEvent(ev);
        if (r) {
            if (isOnInputLayout(ev.getX(), ev.getY()))
                return false;
        }
        return r;
    }
}
