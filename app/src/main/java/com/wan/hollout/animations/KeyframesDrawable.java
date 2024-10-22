/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.wan.hollout.animations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;


import com.wan.hollout.animations.model.KFAnimationGroup;
import com.wan.hollout.animations.model.KFFeature;
import com.wan.hollout.animations.model.KFGradient;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.animations.model.keyframedmodels.KeyFramedGradient;
import com.wan.hollout.animations.model.keyframedmodels.KeyFramedOpacity;
import com.wan.hollout.animations.model.keyframedmodels.KeyFramedPath;
import com.wan.hollout.animations.model.keyframedmodels.KeyFramedStrokeWidth;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This drawable will render a KFImage model by painting paths to the supplied canvas in
 * {@link #draw(Canvas)}.  There are methods to begin and end animation playback here, which need to
 * be managed carefully so as not to leave animation callbacks running indefinitely.  At each
 * animation callback, the next frame's matrices and paths are calculated and the drawable is then
 * invalidated.
 */
public class KeyframesDrawable extends Drawable
        implements KeyframesDrawableAnimationCallback.FrameListener, KeyframesDirectionallyScalingDrawable {

  private static final float GRADIENT_PRECISION_PER_SECOND = 30;

  /**
   * The KFImage object to render.
   */
  private final KFImage mKFImage;
  /**
   * A recyclable {@link Paint} object used to draw all of the features.
   */
  private final Paint mDrawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  /**
   * The list of all {@link FeatureState}s, containing all information needed to render a feature
   * for the current progress of animation.
   */
  private final List<FeatureState> mFeatureStateList;
  /**
   * The current state of animation layer matrices for this animation, keyed by animation group id.
   */
  private final SparseArray<Matrix> mAnimationGroupMatrices;
  /**
   * The animation callback object used to start and stop the animation.
   */
  private final KeyframesDrawableAnimationCallback mKeyframesDrawableAnimationCallback;
  /**
   * A recyclable matrix that can be reused.
   */
  private final Matrix mRecyclableTransformMatrix;

  /**
   * The scale matrix to be applied for the final size of this drawable.
   */
  private final Matrix mScaleMatrix;
  private final Matrix mInverseScaleMatrix;

  /**
   * The currently set width and height of this drawable.
   */
  private int mSetWidth;
  private int mSetHeight;
  /**
   * The X and Y scales to be used, calculated from the set dimensions compared with the exported
   * canvas size of the image.
   */
  private float mScale;
  private float mScaleFromCenter;
  private float mScaleFromEnd;
  private final Map<String, Bitmap> mBitmaps;
  private boolean mClipToAECanvas;

  private boolean mHasInitialized = false;

  /**
   * Create a new KeyframesDrawable with the supplied values from the builder.
   * @param builder
   */
  KeyframesDrawable(KeyframesDrawableBuilder builder) {
    mKFImage = builder.getImage();
    mBitmaps = builder.getExperimentalFeatures().getBitmaps() == null ?
        null :
        Collections.unmodifiableMap(builder.getExperimentalFeatures().getBitmaps());

    mRecyclableTransformMatrix = new Matrix();
    mScaleMatrix = new Matrix();
    mInverseScaleMatrix = new Matrix();
    mKeyframesDrawableAnimationCallback = KeyframesDrawableAnimationCallback.create(this, mKFImage);

    mDrawingPaint.setStrokeCap(Paint.Cap.ROUND);

    // Setup feature state list
    List<FeatureState> featureStateList = new ArrayList<>();
    for (int i = 0, len = mKFImage.getFeatures().size(); i < len; i++) {
      featureStateList.add(new FeatureState(mKFImage.getFeatures().get(i)));
    }
    mFeatureStateList = Collections.unmodifiableList(featureStateList);

    // Setup animation layers
    mAnimationGroupMatrices = new SparseArray<>();
    List<KFAnimationGroup> animationGroups = mKFImage.getAnimationGroups();
    for (int i = 0, len = animationGroups.size(); i < len; i++) {
      mAnimationGroupMatrices.put(animationGroups.get(i).getGroupId(), new Matrix());
    }

    setMaxFrameRate(builder.getMaxFrameRate());
    mClipToAECanvas = builder.getExperimentalFeatures().getClipToAECanvas();
  }

  /**
   * Sets the bounds of this drawable.  Here, we calculate values needed to scale the image from the
   * size it was when exported to a size to be drawn on the Android canvas.
   */
  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    super.setBounds(left, top, right, bottom);
    mSetWidth = right - left;
    mSetHeight = bottom - top;

    float idealXScale = (float) mSetWidth / mKFImage.getCanvasSize()[0];
    float idealYScale = (float) mSetHeight / mKFImage.getCanvasSize()[1];

    mScale = Math.min(idealXScale, idealYScale);
    calculateScaleMatrix(1, 1, ScaleDirection.UP);
    if (!mHasInitialized) {
      // Call this at least once or else nothing will render. But if this is called this every time
      // setBounds is called then the animation will reset when resizing.
      setFrameProgress(0);
    }
  }

  @Override
  public void setDirectionalScale(
          float scaleFromCenter,
          float scaleFromEnd,
          ScaleDirection direction) {
    calculateScaleMatrix(scaleFromCenter, scaleFromEnd, direction);
  }

  /**
   * Iterates over the current state of mPathsForDrawing and draws each path, applying properties
   * of the feature to a recycled Paint object.
   */
  @Override
  public void draw(Canvas canvas) {
    Rect currBounds = getBounds();
    canvas.translate(currBounds.left, currBounds.top);
    if (mClipToAECanvas) {
      canvas.clipRect(
          0,
          0,
          mKFImage.getCanvasSize()[0] * mScale * mScaleFromEnd * mScaleFromCenter,
          mKFImage.getCanvasSize()[1] * mScale * mScaleFromEnd * mScaleFromCenter);
    }
    KFPath pathToDraw;
    FeatureState featureState;
    for (int i = 0, len = mFeatureStateList.size(); i < len; i++) {
      featureState = mFeatureStateList.get(i);
      if (!featureState.isVisible()) {
        continue;
      }

      final Bitmap backedImage = featureState.getBackedImageBitmap();
      final Matrix uniqueFeatureMatrix = featureState.getUniqueFeatureMatrix();
      if (backedImage != null && uniqueFeatureMatrix != null) {
        // This block is for the experimental bitmap supporting
        canvas.save();
        canvas.concat(mScaleMatrix);
        canvas.drawBitmap(backedImage, uniqueFeatureMatrix, null);

        canvas.restore();
        continue;
      }

      pathToDraw = featureState.getCurrentPathForDrawing();
      if (pathToDraw == null || pathToDraw.isEmpty()) {
        continue;
      }
      if (featureState.getCurrentMaskPath() != null) {
        canvas.save();
        applyScaleAndClipCanvas(canvas, featureState.getCurrentMaskPath(), Region.Op.INTERSECT);
      }
      mDrawingPaint.setShader(null);
      mDrawingPaint.setStrokeCap(featureState.getStrokeLineCap());
      if (featureState.getFillColor() != Color.TRANSPARENT) {
        mDrawingPaint.setStyle(Paint.Style.FILL);
        if (featureState.getCurrentShader() == null) {
          mDrawingPaint.setColor(featureState.getFillColor());
          mDrawingPaint.setAlpha(featureState.getAlpha());
          applyScaleAndDrawPath(canvas, pathToDraw, mDrawingPaint);
        } else {
          mDrawingPaint.setShader(featureState.getCurrentShader());
          applyScaleToCanvasAndDrawPath(canvas, pathToDraw, mDrawingPaint);
        }
      }
      if (featureState.getStrokeColor() != Color.TRANSPARENT && featureState.getStrokeWidth() > 0) {
        mDrawingPaint.setColor(featureState.getStrokeColor());
        mDrawingPaint.setAlpha(featureState.getAlpha());
        mDrawingPaint.setStyle(Paint.Style.STROKE);
        mDrawingPaint.setStrokeWidth(
                featureState.getStrokeWidth() * mScale * mScaleFromCenter * mScaleFromEnd);
        applyScaleAndDrawPath(canvas, pathToDraw, mDrawingPaint);
      }
      if (featureState.getCurrentMaskPath() != null) {
        canvas.restore();
      }
    }
    canvas.translate(-currBounds.left, -currBounds.top);
  }

  private void applyScaleAndClipCanvas(Canvas canvas, KFPath path, Region.Op op) {
    path.transform(mScaleMatrix);
    canvas.clipPath(path.getPath(), op);
    path.transform(mInverseScaleMatrix);
  }

  private void applyScaleAndDrawPath(Canvas canvas, KFPath path, Paint paint) {
    path.transform(mScaleMatrix);
    canvas.drawPath(path.getPath(), paint);
    path.transform(mInverseScaleMatrix);
  }

  /**
   * Note: This method is only necessary because of cached gradient shaders with a fixed size.  We
   * need to scale the canvas in this case rather than scaling the path.
   */
  private void applyScaleToCanvasAndDrawPath(Canvas canvas, KFPath path, Paint paint) {
    canvas.concat(mScaleMatrix);
    canvas.drawPath(path.getPath(), paint);
    canvas.concat(mInverseScaleMatrix);
  }

  /**
   * Unsupported for now
   */
  @Override
  public void setAlpha(int alpha) {
  }

  /**
   * Unsupported for now
   */
  @Override
  public void setColorFilter(ColorFilter cf) {
  }

  /**
   * Unsupported for now
   */
  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }

  /**
   * Starts the animation callbacks for this drawable.  A corresponding call to
   * {@link #stopAnimationAtLoopEnd()}, {@link #stopAnimation() or {@link #pauseAnimation()}
   * needs to be called eventually, or the callback will continue to post callbacks
   * for this drawable indefinitely.
   */
  public void startAnimation() {
    mKeyframesDrawableAnimationCallback.start();
  }

  /**
   * Starts the animation and plays it once
   */
  public void playOnce() {
    mKeyframesDrawableAnimationCallback.playOnce();
  }

  /**
   * Stops the animation callbacks for this drawable immediately.
   */
  public void stopAnimation() {
    mKeyframesDrawableAnimationCallback.stop();
  }

  /**
   * Pauses the animation callbacks for this drawable immediately.
   */
  public void pauseAnimation() {
    mKeyframesDrawableAnimationCallback.pause();
  }

  /**
   * Resumes the animation callbacks for this drawable.  A corresponding call to
   * {@link #stopAnimationAtLoopEnd()}, {@link #stopAnimation() or {@link #pauseAnimation()}
   * needs to be called eventually, or the callback will continue to post callbacks
   * for this drawable indefinitely.
   */
  public void resumeAnimation() {
    mKeyframesDrawableAnimationCallback.resume();
  }

  /**
   * Finishes the current playthrough of the animation and stops animating this drawable afterwards.
   */
  public void stopAnimationAtLoopEnd() {
    mKeyframesDrawableAnimationCallback.stopAtLoopEnd();
  }

  /**
   * Given a progress in terms of frames, calculates each of the paths needed to be drawn in
   * {@link #draw(Canvas)}.
   */
  public void setFrameProgress(float frameProgress) {
    mHasInitialized = true;
    mKFImage.setAnimationMatrices(mAnimationGroupMatrices, frameProgress);
    for (int i = 0, len = mFeatureStateList.size(); i < len; i++) {
      mFeatureStateList.get(i).setupFeatureStateForProgress(frameProgress);
    }
  }

  public void seekToProgress(float progress) {
    stopAnimation();
    onProgressUpdate(progress * mKFImage.getFrameCount());
  }

  /**
   * The callback used to update the frame progress of this drawable.  This leads to a recalculation
   * of the paths that need to be drawn before the Drawable invalidates itself.
   */
  @Override
  public void onProgressUpdate(float frameProgress) {
    setFrameProgress(frameProgress);
    invalidateSelf();
  }

  @Override
  public void onStop() {
    if (mOnAnimationEnd == null) {
      return;
    }
    final OnAnimationEnd onAnimationEnd = mOnAnimationEnd.get();
    if (onAnimationEnd == null) {
      return;
    }
    onAnimationEnd.onAnimationEnd();
    mOnAnimationEnd.clear();
  }

  private WeakReference<OnAnimationEnd> mOnAnimationEnd;

  public void setAnimationListener(OnAnimationEnd listener) {
    mOnAnimationEnd = new WeakReference<>(listener);
  }

  private void calculateScaleMatrix(
          float scaleFromCenter,
          float scaleFromEnd,
          ScaleDirection scaleDirection) {
    if (mScaleFromCenter == scaleFromCenter &&
            mScaleFromEnd == scaleFromEnd) {
      return;
    }

    mScaleMatrix.setScale(mScale, mScale);
    if (scaleFromCenter == 1 && scaleFromEnd == 1) {
      mScaleFromCenter = 1;
      mScaleFromEnd = 1;
      mScaleMatrix.invert(mInverseScaleMatrix);
      return;
    }

    float scaleYPoint = scaleDirection == ScaleDirection.UP ? mSetHeight : 0;
    mScaleMatrix.postScale(scaleFromCenter, scaleFromCenter, mSetWidth / 2, mSetHeight / 2);
    mScaleMatrix.postScale(scaleFromEnd, scaleFromEnd, mSetWidth / 2, scaleYPoint);

    mScaleFromCenter = scaleFromCenter;
    mScaleFromEnd = scaleFromEnd;
    mScaleMatrix.invert(mInverseScaleMatrix);
  }

  /**
   * Cap the frame rate to a specific FPS. Consider using this for low end devices.
   * Calls {@link KeyframesDrawableAnimationCallback#setMaxFrameRate}
   * @param maxFrameRate
   */
  public void setMaxFrameRate(int maxFrameRate) {
    mKeyframesDrawableAnimationCallback.setMaxFrameRate(maxFrameRate);
  }

  private class FeatureState {
    private final KFFeature mFeature;

    // Reuseable modifiable objects for drawing
    private final KFPath mPath;
    private final KFPath mFeatureMaskPath;
    private final KeyFramedStrokeWidth.StrokeWidth mStrokeWidth;
    private final KeyFramedOpacity.Opacity mOpacity;
    private final Matrix mFeatureMatrix;
    private final float[] mMatrixValueRecyclableArray = new float[9];
    private final Matrix mFeatureMaskMatrix;

    private boolean mIsVisible;

    public Matrix getUniqueFeatureMatrix() {
      if (mFeatureMatrix == mRecyclableTransformMatrix) {
        // Don't return a matrix unless it's known to be unique for this feature
        return null;
      }
      return mFeatureMatrix;
    }

    // Cached shader vars
    private Shader[] mCachedShaders;
    private Shader mCurrentShader;

    public FeatureState(KFFeature feature) {
      mFeature = feature;
      if (hasCustomDrawable()) {
        mPath = null;
        mStrokeWidth = null;
        // Bitmap features use the matrix later in draw()
        // so there's no way to reuse a globally cached matrix
        mFeatureMatrix = new Matrix();
      } else {
        mPath = new KFPath();
        mStrokeWidth = new KeyFramedStrokeWidth.StrokeWidth();
        // Path features use the matrix immediately
        // so there's no need to waste memory with a unique copy
        mFeatureMatrix = mRecyclableTransformMatrix;
      }
      mOpacity = new KeyFramedOpacity.Opacity();
      if (mFeature.getFeatureMask() != null) {
        mFeatureMaskPath = new KFPath();
        mFeatureMaskMatrix = new Matrix();
      } else {
        mFeatureMaskPath = null;
        mFeatureMaskMatrix = null;
      }
      assert mFeatureMatrix != null;
    }

    public void setupFeatureStateForProgress(float frameProgress) {
      if (frameProgress < mFeature.getFromFrame() || frameProgress > mFeature.getToFrame()) {
        mIsVisible = false;
        return;
      }
      mIsVisible = true;
      mFeature.setAnimationMatrix(mFeatureMatrix, frameProgress);
      Matrix layerTransformMatrix = mAnimationGroupMatrices.get(mFeature.getAnimationGroup());

      if (layerTransformMatrix != null && !layerTransformMatrix.isIdentity()) {
        mFeatureMatrix.postConcat(layerTransformMatrix);
      }
      KeyFramedPath path = mFeature.getPath();
      if (hasCustomDrawable() || path == null) {
        return; // skip all the path stuff
      }
      mPath.reset();
      path.apply(frameProgress, mPath);
      mPath.transform(mFeatureMatrix);

      mFeature.setStrokeWidth(mStrokeWidth, frameProgress);
      mStrokeWidth.adjustScale(extractScaleFromMatrix(mFeatureMatrix));
      mFeature.setOpacity(mOpacity, frameProgress);
      if (mFeature.getEffect() != null) {
        prepareShadersForFeature(mFeature);
      }
      mCurrentShader = getNearestShaderForFeature(frameProgress);

      if (mFeature.getFeatureMask() != null) {
        mFeature.getFeatureMask().setAnimationMatrix(mFeatureMaskMatrix, frameProgress);
        mFeatureMaskPath.reset();
        mFeature.getFeatureMask().getPath().apply(frameProgress, mFeatureMaskPath);
        mFeatureMaskPath.transform(mFeatureMaskMatrix);
      }
    }

    public KFPath getCurrentPathForDrawing() {
      return mPath;
    }

    public KFPath getCurrentMaskPath() {
      return mFeatureMaskPath;
    }

    public float getStrokeWidth() {
      return mStrokeWidth != null ? mStrokeWidth.getStrokeWidth() : 0;
    }

    public float getOpacity() {
      return mOpacity.getOpacity() / 100;
    }

    public int getAlpha() {
      return Math.round(0xFF * getOpacity());
    }

    public Shader getCurrentShader() {
      return mCurrentShader;
    }

    public int getStrokeColor() {
      return mFeature.getStrokeColor();
    }

    public int getFillColor() {
      return mFeature.getFillColor();
    }

    public Paint.Cap getStrokeLineCap() {
      return mFeature.getStrokeLineCap();
    }

    public boolean isVisible() {
      return mIsVisible;
    }

    private void prepareShadersForFeature(KFFeature feature) {
      if (mCachedShaders != null) {
        return;
      }

      int frameRate = mKFImage.getFrameRate();
      int numFrames = mKFImage.getFrameCount();
      int precision = Math.round(GRADIENT_PRECISION_PER_SECOND * numFrames / frameRate);
      mCachedShaders = new LinearGradient[precision + 1];
      float progress;
      KeyFramedGradient.GradientColorPair colorPair = new KeyFramedGradient.GradientColorPair();
      KFGradient gradient = feature.getEffect().getGradient();
      for (int i = 0; i < precision; i++) {
        progress = i / (float) (precision) * numFrames;
        gradient.getStartGradient().apply(progress, colorPair);
        gradient.getEndGradient().apply(progress, colorPair);
        mCachedShaders[i] = new LinearGradient(
                0,
                0,
                0,
                mKFImage.getCanvasSize()[1],
                colorPair.getStartColor(),
                colorPair.getEndColor(),
                Shader.TileMode.CLAMP);
      }
    }

    public Shader getNearestShaderForFeature(float frameProgress) {
      if (mCachedShaders == null) {
        return null;
      }
      int shaderIndex =
              (int) ((frameProgress / mKFImage.getFrameCount()) * (mCachedShaders.length - 1));
      return mCachedShaders[shaderIndex];
    }

    public final Bitmap getBackedImageBitmap() {
      if (mBitmaps == null) return null;
      return mBitmaps.get(mFeature.getBackedImageName());
    }

    private boolean hasCustomDrawable() {
      return getBackedImageBitmap() != null;
    }

    private float extractScaleFromMatrix(Matrix matrix) {
      matrix.getValues(mMatrixValueRecyclableArray);
      return (Math.abs(mMatrixValueRecyclableArray[0]) +
          Math.abs(mMatrixValueRecyclableArray[4])) / 2f;
    }
  }

  public interface OnAnimationEnd {
    void onAnimationEnd();
  }
}
