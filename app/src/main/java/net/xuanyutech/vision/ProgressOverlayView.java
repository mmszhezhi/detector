package net.xuanyutech.vision;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.progressindicator.ProgressIndicator;

public class ProgressOverlayView extends FrameLayout {
    public ProgressOverlayView(@NonNull Context context) {
        super(context);
        initViews();
    }

    public ProgressOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ProgressOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public ProgressOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    private void initViews() {
        setBackgroundResource(R.color.overlay_black);
        ProgressIndicator indicator = new ProgressIndicator(getContext());
        indicator.setIndeterminate(true);
        indicator.setIndicatorType(ProgressIndicator.CIRCULAR);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        addView(indicator, params);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
