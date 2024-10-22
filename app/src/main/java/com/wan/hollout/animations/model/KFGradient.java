/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.wan.hollout.animations.model;

import com.wan.hollout.animations.model.keyframedmodels.KeyFramedGradient;
import com.wan.hollout.animations.util.ArgCheckUtil;

import static com.wan.hollout.animations.model.keyframedmodels.KeyFramedGradient.Position.END;
import static com.wan.hollout.animations.model.keyframedmodels.KeyFramedGradient.Position.START;

/**
 * An object which wraps information for a gradient effect.  Currently only supports simple linear
 * gradients.
 */
public class KFGradient {

  /**
   * To prevent allocating a lot of LinearGradient shaders during animation, and because it looks
   * looks like LinearGradient shader params can't be modified after instantiation, we cache the
   * shaders needed once at a precision of shaders per second listed here.
   */

  /**
   * The start color of the gradient, the top color of the linear gradient.
   */
  public static final String COLOR_START_JSON_FIELD = "color_start";
  private final KeyFramedGradient mStartGradient;

  /**
   * The end color of the gradient, the bottom color of the linear gradient.
   */
  public static final String COLOR_END_JSON_FIELD = "color_end";
  private final KeyFramedGradient mEndGradient;

  public static class Builder {
    public KFGradientColor colorStart;
    public KFGradientColor colorEnd;

    public KFGradient build() {
      return new KFGradient(colorStart, colorEnd);
    }
  }

  public KFGradient(KFGradientColor colorStart, KFGradientColor colorEnd) {
    mStartGradient = KeyFramedGradient.fromGradient(
        ArgCheckUtil.checkArg(
            colorStart,
            colorStart != null,
            COLOR_START_JSON_FIELD),
        START);
    mEndGradient = KeyFramedGradient.fromGradient(
        ArgCheckUtil.checkArg(
            colorEnd,
            colorEnd != null,
            COLOR_END_JSON_FIELD),
        END);
  }

  public KeyFramedGradient getStartGradient() {
    return mStartGradient;
  }

  public KeyFramedGradient getEndGradient() {
    return mEndGradient;
  }
}