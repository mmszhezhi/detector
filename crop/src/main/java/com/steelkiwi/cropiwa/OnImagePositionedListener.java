package com.steelkiwi.cropiwa;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

/**
 * @author Yaroslav Polyakov
 * 25.02.2017.
 */

interface OnImagePositionedListener {
    void onImagePositioned(RectF imageRect, RectF imageCropRect, RectF coordinateSystem, Bitmap bitmap);
}
