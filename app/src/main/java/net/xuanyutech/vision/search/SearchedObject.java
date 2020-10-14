/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xuanyutech.vision.search;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.annotation.Nullable;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.objectdetection.DetectedObject;

import java.util.List;

/** Hosts the detected object info and its search result. */
public class SearchedObject {

  private final DetectedObject object;
  private final List<Product> productList;
  private final String errorMsg;
  private final int objectThumbnailCornerRadius;

  @Nullable
  private Bitmap objectThumbnail;

  public SearchedObject(Resources resources, DetectedObject object, List<Product> productList) {
    this(resources, object, productList, "");
  }

  public SearchedObject(Resources resources, DetectedObject object, List<Product> productList, String errorMsg) {
    this.object = object;
    this.productList = productList;
    this.objectThumbnailCornerRadius = resources.getDimensionPixelOffset(R.dimen.bounding_box_corner_radius);
    this.errorMsg = errorMsg;
  }

  public int getObjectIndex() {
    return object.getObjectIndex();
  }

  public List<Product> getProductList() {
    return productList;
  }

  public Rect getBoundingBox() {
    return object.getBoundingBox();
  }

  public String getErrorMsg() {
    return this.errorMsg;
  }

  public synchronized Bitmap getObjectThumbnail() {
    if (objectThumbnail == null) {
      objectThumbnail =
          Utils.getCornerRoundedBitmap(object.getBitmap(), objectThumbnailCornerRadius);
    }
    return objectThumbnail;
  }
}
