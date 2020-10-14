package com.steelkiwi.cropiwa.image;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * @author yarolegovich
 * 25.02.2017.
 */
public class CropArea {

    public static CropArea create(RectF coordinateSystem, RectF imageRect, RectF cropRect) {
        return new CropArea(
                moveRectToCoordinateSystem(coordinateSystem, imageRect),
                moveRectToCoordinateSystem(coordinateSystem, cropRect), coordinateSystem);
    }

    private static Rect moveRectToCoordinateSystem(RectF system, RectF rect) {
        float originX = system.left, originY = system.top;
        return new Rect(
                Math.round(rect.left - originX), Math.round(rect.top - originY),
                Math.round(rect.right - originX), Math.round(rect.bottom - originY));
    }

    private final Rect imageRect;
    private final Rect cropRect;
    private final RectF system;

    public CropArea(Rect imageRect, Rect cropRect, RectF system) {
        this.imageRect = imageRect;
        this.cropRect = cropRect;
        this.system = system;
    }

    public Rect getCropRect(Bitmap bitmap) {
        return new Rect(findRealCoordinate(bitmap.getWidth(), cropRect.left, imageRect.width()),
                findRealCoordinate(bitmap.getHeight(), cropRect.top, imageRect.height()),
                findRealCoordinate(bitmap.getWidth(), cropRect.right, imageRect.width()),
                findRealCoordinate(bitmap.getHeight(), cropRect.bottom, imageRect.height()));
    }

    public Bitmap applyCropTo(Bitmap bitmap) {
        Bitmap immutableCropped = Bitmap.createBitmap(bitmap,
                findRealCoordinate(bitmap.getWidth(), cropRect.left, imageRect.width()),
                findRealCoordinate(bitmap.getHeight(), cropRect.top, imageRect.height()),
                findRealCoordinate(bitmap.getWidth(), cropRect.width(), imageRect.width()),
                findRealCoordinate(bitmap.getHeight(), cropRect.height(), imageRect.height()));
        return immutableCropped.copy(immutableCropped.getConfig(), true);
    }


    private int findRealCoordinate(int imageRealSize, int cropCoordinate, float cropImageSize) {
        return Math.round((imageRealSize * cropCoordinate) / cropImageSize);
    }

    public RectF convertFromOrigin(RectF rect, Bitmap bitmap) {
        return new RectF(rect.left * imageRect.width() / bitmap.getWidth() + system.left,
                rect.top * imageRect.height() / bitmap.getHeight() + system.top,
                rect.right * imageRect.width() / bitmap.getWidth() + system.left,
                rect.bottom * imageRect.height() / bitmap.getHeight() + system.top);
    }

}
