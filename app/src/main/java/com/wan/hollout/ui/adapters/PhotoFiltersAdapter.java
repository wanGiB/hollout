//package com.wan.hollout.ui.adapters;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.Point;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.mukesh.image_processing.ImageProcessingConstants;
//import com.mukesh.image_processing.ImageProcessor;
//import com.parse.ParseObject;
//import com.wan.hollout.R;
//import com.wan.hollout.bean.PhotoFilter;
//import com.wan.hollout.utils.AppConstants;
//import com.wan.hollout.utils.AuthUtil;
//import com.wan.hollout.utils.FiltersLoader;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.Arrays;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//
///**
// * @author Wan Clem
// */
//
//public class PhotoFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private List<PhotoFilter> photoFilters;
//    private LayoutInflater layoutInflater;
//    private Bitmap inputBitmap;
//
//    public PhotoFiltersAdapter(Context context, Bitmap inputBitmap) {
//        photoFilters = Arrays.asList(FiltersLoader.getFilters());
//        this.inputBitmap = inputBitmap;
//        layoutInflater = LayoutInflater.from(context);
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = layoutInflater.inflate(R.layout.photo_filter_template_item, parent, false);
//        return new FilterablePhotoItemHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        FilterablePhotoItemHolder filterablePhotoItemHolder = (FilterablePhotoItemHolder) holder;
//        filterablePhotoItemHolder.bindData(inputBitmap, photoFilters.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return photoFilters.size();
//    }
//
//    static class FilterablePhotoItemHolder extends RecyclerView.ViewHolder {
//
//        @BindView(R.id.filter_image)
//        ImageView filterImageView;
//
//        @BindView(R.id.filter_name)
//        TextView filterNameView;
//
//        FilterablePhotoItemHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//
//        void bindData(Bitmap inputBitmap, PhotoFilter photoFilter) {
//            ParseObject signedInUserObject = AuthUtil.getCurrentUser();
//            String userName = "HolloutUser";
//            if (signedInUserObject != null) {
//                userName = signedInUserObject.getString(AppConstants.APP_USER_DISPLAY_NAME);
//            }
//            String waterMark = "Awesome " + userName;
//
//            filterNameView.setText(photoFilter.getFilterName());
//            ImageProcessor imageProcessor = new ImageProcessor();
//            Bitmap outputBitmap = null;
//
//            switch (photoFilter.getFilterType()) {
//                case BLACK_FILTER:
//                    outputBitmap = imageProcessor.applyBlackFilter(inputBitmap);
//                    break;
//                case FLEA:
//                    outputBitmap = imageProcessor.applyFleaEffect(inputBitmap);
//                    break;
//                case GAUSSIAN_BLUR:
//                    outputBitmap = imageProcessor.applyGaussianBlur(inputBitmap);
//                    break;
//                case HUE:
//                    outputBitmap = imageProcessor.applyHueFilter(inputBitmap, 1);
//                    break;
//                case FLIP:
//                    outputBitmap = imageProcessor.flip(inputBitmap, ImageProcessingConstants.FLIP_VERTICAL);
//                    break;
//                case TINT:
//                    outputBitmap = imageProcessor.tintImage(inputBitmap, 50);
//                    break;
//                case BOOST:
//                    outputBitmap = imageProcessor.boost(inputBitmap, ImageProcessingConstants.RED, 1.5);
//                    break;
//                case GAMMA:
//                    outputBitmap = imageProcessor.doGamma(inputBitmap, 0.6, 0.6, 0.6);
//                    break;
//                case SEPIA:
//                    outputBitmap = imageProcessor.createSepiaToningEffect(inputBitmap, 150, 0.7, 0.3, 0.12);
//                    break;
//                case EMBOSS:
//                    outputBitmap = imageProcessor.emboss(inputBitmap);
//                    break;
//                case INVERT:
//                    outputBitmap = imageProcessor.doInvert(inputBitmap);
//                    break;
//                case ROTATE:
//                    outputBitmap = imageProcessor.rotate(inputBitmap, 340);
//                    break;
//                case SMOOTH:
//                    outputBitmap = imageProcessor.smooth(inputBitmap, 100);
//                    break;
//                case ENGRAVE:
//                    outputBitmap = imageProcessor.engrave(inputBitmap);
//                    break;
//                case SHARPEN:
//                    outputBitmap = imageProcessor.sharpen(inputBitmap, 11);
//                    break;
//                case CONTRAST:
//                    outputBitmap = imageProcessor.createContrast(inputBitmap, 50);
//                    break;
//                case HIGHLIGHT:
//                    outputBitmap = imageProcessor.doHighlightImage(inputBitmap, 16, Color.RED);
//                    break;
//                case BRIGHTNESS:
//                    outputBitmap = imageProcessor.doBrightness(inputBitmap, 30);
//                    break;
//                case GRAY_SCALE:
//                    outputBitmap = imageProcessor.doGreyScale(inputBitmap);
//                    break;
//                case REFLECTION:
//                    outputBitmap = imageProcessor.applyReflection(inputBitmap);
//                    break;
//                case SATURATION:
//                    outputBitmap = imageProcessor.applySaturationFilter(inputBitmap, 5);
//                    break;
//                case WATER_MARK:
//                    outputBitmap = imageProcessor.waterMark(inputBitmap, waterMark,
//                            new Point(inputBitmap.getWidth() / 2, inputBitmap.getHeight() / 2),
//                            Color.RED, 1, 10, false);
//                    break;
//                case COLOR_DEPTH:
//                    outputBitmap = imageProcessor.decreaseColorDepth(inputBitmap, 32);
//                    break;
//                case SNOW_EFFECT:
//                    outputBitmap = imageProcessor.applySnowEffect(inputBitmap);
//                    break;
//                case COLOR_FILTER:
//                    outputBitmap = imageProcessor.doColorFilter(inputBitmap, 0.6, 0.6, 0.6);
//                    break;
//                case MEAN_REMOVAL:
//                    outputBitmap = imageProcessor.applyMeanRemoval(inputBitmap);
//                    break;
//                case ROUND_CORNER:
//                    outputBitmap = imageProcessor.roundCorner(inputBitmap, 0.2);
//                    break;
//                case REPLACE_COLOR:
//                    outputBitmap = imageProcessor.replaceColor(inputBitmap, 1, 0);
//                    break;
//                case SHADOW_EFFECT:
//                    outputBitmap = imageProcessor.createShadow(inputBitmap);
//                    break;
//                case SHADING_EFFECT:
//                    outputBitmap = imageProcessor.applyShadingFilter(inputBitmap, Color.RED);
//                    break;
//            }
//            if (outputBitmap != null) {
//                filterImageView.setImageBitmap(outputBitmap);
//                final Bitmap finalOutputBitmap = outputBitmap;
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        EventBus.getDefault().post(finalOutputBitmap);
//                    }
//                });
//            }
//        }
//
//    }
//
//}
