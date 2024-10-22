/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.wan.hollout.animations.deserializers;

import android.util.JsonReader;


import com.wan.hollout.animations.model.KFAnimation;

import java.io.IOException;
import java.util.Locale;

/**
 * Deserializer for {@link KFAnimation}.
 *
 * Root deserializer starts at {@link KFImageDeserializer}.
 */
public class KFAnimationDeserializer {

  static final AbstractListDeserializer<KFAnimation> LIST_DESERIALIZER =
      new AbstractListDeserializer<KFAnimation>() {
    @Override
    KFAnimation readObjectImpl(JsonReader reader) throws IOException {
      return readObject(reader);
    }
  };

  public static KFAnimation readObject(JsonReader reader) throws IOException {
    reader.beginObject();
    KFAnimation.Builder builder = new KFAnimation.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case KFAnimation.PROPERTY_TYPE_JSON_FIELD:
          builder.propertyType = KFAnimation.PropertyType.valueOf(
              reader.nextString().toUpperCase(Locale.US));
          break;
        case KFAnimation.ANIMATION_FRAMES_JSON_FIELD:
          builder.animationFrames =
              KFAnimationFrameDeserializer.LIST_DESERIALIZER.readList(reader);
          break;
        case KFAnimation.TIMING_CURVES_JSON_FIELD:
          builder.timingCurves = CommonDeserializerHelper.read3DFloatArray(reader);
          break;
        case KFAnimation.ANCHOR_JSON_FIELD:
          builder.anchor = CommonDeserializerHelper.readFloatArray(reader);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();
    return builder.build();
  }
}
