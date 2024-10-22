/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.wan.hollout.animations.model;

import com.wan.hollout.animations.util.ArgCheckUtil;
import com.wan.hollout.animations.util.ListHelper;

import java.util.List;

/**
 * The class which contains information about an animated gradient, including the key frame
 * information and timing curves.
 */
public class KFGradientColor {

  /**
   * A list of {@link KFColorFrame}s, each describing a color given a key frame.
   */
  public static final String KEY_VALUES_JSON_FIELD = "key_values";
  private final List<KFColorFrame> mKeyValues;

  /**
   * A list of timing curves which describes how to interpolate between two
   * {@link KFColorFrame}s.
   */
  public static final String TIMING_CURVES_JSON_FIELD = "timing_curves";
  private final float[][][] mTimingCurves;

  public static class Builder {
    public List<KFColorFrame> keyValues;
    public float[][][] timingCurves;

    public KFGradientColor build() {
      return new KFGradientColor(keyValues, timingCurves);
    }
  }

  public KFGradientColor(List<KFColorFrame> keyValues, float[][][] timingCurves) {
    mKeyValues = ListHelper.immutableOrEmpty(keyValues);
    mTimingCurves = ArgCheckUtil.checkArg(
        timingCurves,
        ArgCheckUtil.checkTimingCurveObjectValidity(timingCurves, mKeyValues.size()),
        TIMING_CURVES_JSON_FIELD);
  }

  public List<KFColorFrame> getKeyValues() {
    return mKeyValues;
  }

  public float[][][] getTimingCurves() {
    return mTimingCurves;
  }
}
