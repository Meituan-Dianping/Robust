package com.meituan.sample.robusttest;

/**
 * Created by mivanzhang on 16/10/24.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.lang.ref.WeakReference;

/**
 * 根据网络环境获取图片质量所对应的API中的字段
 * <p>
 *
 */
public class ImageQualityUtil {
    private static final String LARGE_SIZE = "/440.267/";
    private static final String LARGE_SIZE_POI_HEADER_IMAGE = "/800.480/";
    private static final String SMALL_SIZE = "/120.76/";
    private static final String MIDDLE_SIZE = "/200.120/";
    private static final String MEDIUM_SIZE = "/0.160/";
    private static final String DEFAULT_SIZE = "/w.h/";

    private static final String MORE_LARGE_SIZE = "/600.160/";
    private static final String MORE_MIDDLE_SIZE = "/290.140/";
    private static final String INN_POI_LIST_SIZE = "/640.0/";

    public static String getQualityUrl(String oldUrl, String quality) {
        String url = oldUrl;
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        url = url.replace(DEFAULT_SIZE, quality);
        return getWebpUrl(url);
    }


    public static String getLargeUrl(String oldUrl) {
        return getQualityUrl(oldUrl, LARGE_SIZE);
    }

    public static String getPoiHeaderImageLargeUrl(String oldUrl) {
        return getQualityUrl(oldUrl, LARGE_SIZE_POI_HEADER_IMAGE);
    }


    public static String getSmallUrl(String oldUrl) {
        return getQualityUrl(oldUrl, SMALL_SIZE);
    }

    public static String getMiddleUrl(String oldUrl) {
        return getQualityUrl(oldUrl, MIDDLE_SIZE);
    }


    public static String getMediumSize(String oldUrl) {
        return getQualityUrl(oldUrl, MEDIUM_SIZE);
    }


    public static String getIndexDefaultSize(String oldUrl) {
        return getQualityUrl(oldUrl, "/");
    }

    // 分享用的图片，去掉url中的宽高参数，避免有时候分享不成功
    public static String getShareUrl(String oldUrl) {
        String url = oldUrl;
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        url = url.replace(DEFAULT_SIZE, "/");
        return getWebpUrl(url);
    }

    public static String getDefaultSize(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        url = url.replace(DEFAULT_SIZE, "/");
        return getWebpUrl(url);
    }

    public static String getWebpUrl(String oldUrl) {
        /** 4.2.1以下用原图，以上用webp格式 **/
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return oldUrl;
        }
        // 哎，兼容Nokia X不支持Webp格式的问题
        if ("Nokia_X".equals(Build.MODEL)) {
            return oldUrl;
        }
        String url = oldUrl;
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        if (url.toLowerCase().endsWith(".webp")) {
            return oldUrl;
        }

        String host = Uri.parse(url).getHost();

//        if (!TextUtils.isEmpty(host) && (host.contains(Consts.IMAGE_SERVICE_HOST_1) || host.contains(Consts.IMAGE_SERVICE_HOST_2))) {
//            return url + ".webp";
//        }

        return url;
    }

    public static void loadImage(Context context, Picasso picasso, String url, int placeHolder, ImageView view) {
        loadImage(context, picasso, url, placeHolder, view, true);
    }

    public static void loadImage(Context context, Picasso picasso, String url, Drawable drawable, ImageView view) {
        loadImage(context, picasso, url, drawable, view, true);
    }

    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final int placeHolder, final ImageView view, final boolean noFade) {
        loadImage(context, picasso, url, placeHolder, view, noFade, false);
    }

    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final Drawable drawable, final ImageView view, final boolean noFade) {
        loadImage(context, picasso, url, drawable, view, noFade, false);
    }


    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final int placeHolder, final ImageView view, final int imageSize, final boolean centerCrop) {
        picasso.cancelRequest(view);
        view.setTag(1, null);
        if (TextUtils.isEmpty(url)) {
            if (placeHolder != 0) {
                view.setImageResource(placeHolder);
            }
        } else {
            final boolean displayPic = true;//true;//
            new ImageViewLoader(view, picasso, url, placeHolder, true, !displayPic, imageSize, centerCrop).load();
        }
    }

    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final int placeHolder, final ImageView view, final int imageWidth, final int imageHeight, final boolean centerCrop) {
        picasso.cancelRequest(view);
        view.setTag(1, null);
        if (TextUtils.isEmpty(url)) {
            if (placeHolder != 0) {
                view.setImageResource(placeHolder);
            }
        } else {
            final boolean displayPic = true;//true;//
            new ImageViewLoader(view, picasso, url, placeHolder, true, !displayPic, imageWidth, imageHeight, centerCrop).load();
        }
    }


    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final int placeHolder, final ImageView view, final boolean noFade,
                                 final boolean loadImageImmediately) {
        picasso.cancelRequest(view);
        view.setTag(1, null);
        if (TextUtils.isEmpty(url)) {
            if (placeHolder != 0) {
                view.setImageResource(placeHolder);
            }
        } else {
            final boolean displayPic = true;//
            new ImageViewLoader(view, picasso, url, placeHolder, noFade, false).load();
        }
    }

    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final Drawable drawable, final ImageView view, final boolean noFade,
                                 final boolean loadImageImmediately) {
        picasso.cancelRequest(view);
        view.setTag(1, null);
        if (TextUtils.isEmpty(url)) {
            if (drawable != null) {
                view.setImageDrawable(drawable);
            }
        } else {
            final boolean displayPic = true;//
            new ImageViewLoader(view, picasso, url, drawable, noFade, false).load();
        }
    }

    public static void loadImage(Context context, final Picasso picasso, final String url,
                                 final Drawable drawable, final ImageView view, final boolean noFade,
                                 final boolean loadImageImmediately, OnImageLoadFinishedListener onImageLoadFinishedListener) {
        picasso.cancelRequest(view);
        view.setTag(1, null);
        if (TextUtils.isEmpty(url)) {
            if (drawable != null) {
                view.setImageDrawable(drawable);
            }
        } else {
            final boolean displayPic = true;//
            new ImageViewLoader(view, picasso, url, drawable, noFade, false)
                    .setOnImageLoadFinishedListener(onImageLoadFinishedListener).load();
        }
    }

    public interface OnImageLoadFinishedListener {
        void onImageLoadFinished();
    }

    private static class ImageViewLoader implements Callback, View.OnTouchListener {
        final WeakReference<ImageView> imageViewRef;
        final Picasso picasso;
        final String url;
        final int placeHolder;
        final boolean noFade;
        final boolean localOnly;
        boolean isForceLoading;
        final boolean centerCrop;
        final int imageSize;
        final Drawable drawable;
        int imageWith;
        int imageHeight;
        private OnImageLoadFinishedListener onImageLoadFinishedListener;

        public ImageViewLoader setOnImageLoadFinishedListener(OnImageLoadFinishedListener onImageLoadFinishedListener) {
            this.onImageLoadFinishedListener = onImageLoadFinishedListener;
            return this;
        }

        private ImageViewLoader(ImageView imageView, Picasso picasso, String url, int placeHolder, boolean noFade, boolean localOnly) {
            this(imageView, picasso, url, placeHolder, noFade, localOnly, -1, false);
        }

        private ImageViewLoader(ImageView imageView, Picasso picasso, String url, Drawable image, boolean noFade, boolean localOnly) {
            this(imageView, picasso, url, image, noFade, localOnly, -1, false);
        }

        public ImageViewLoader(ImageView imageView, Picasso picasso, String url, int placeHolder, boolean noFade, boolean localOnly, int imageSize, boolean centerCrop) {
            imageViewRef = new WeakReference<ImageView>(imageView);
            this.picasso = picasso;
            this.url = url;
            this.placeHolder = placeHolder;
            this.noFade = noFade;
            this.localOnly = localOnly;
            this.centerCrop = centerCrop;
            this.imageSize = imageSize;
            this.drawable = null;
        }

        public ImageViewLoader(ImageView imageView, Picasso picasso, String url, int placeHolder, boolean noFade, boolean localOnly, int imageWith, int imageHeight, boolean centerCrop) {
            imageViewRef = new WeakReference<ImageView>(imageView);
            this.picasso = picasso;
            this.url = url;
            this.placeHolder = placeHolder;
            this.noFade = noFade;
            this.localOnly = localOnly;
            this.centerCrop = centerCrop;
            this.imageHeight = imageHeight;
            this.imageWith = imageWith;
            this.imageSize = -1;
            this.drawable = null;
        }

        public ImageViewLoader(ImageView imageView, Picasso picasso, String url, Drawable drawable, boolean noFade, boolean localOnly, int imageSize, boolean centerCrop) {
            imageViewRef = new WeakReference<ImageView>(imageView);
            this.picasso = picasso;
            this.url = url;
            this.drawable = drawable;
            this.noFade = noFade;
            this.localOnly = localOnly;
            this.centerCrop = centerCrop;
            this.imageSize = imageSize;
            this.placeHolder = 0;
        }

        void load() {
            load(false);
        }

        void load(boolean forceLoad) {
            ImageView imageView = imageViewRef.get();
            if (imageView == null) {
                return;
            }
            RequestCreator requestCreator = picasso.load(url);
            if (localOnly && !forceLoad) {
                requestCreator.networkPolicy(NetworkPolicy.OFFLINE);
            }

            if (centerCrop) {
                requestCreator.centerCrop();
            }

            if (imageSize > 0) {
                requestCreator.resize(imageSize, imageSize);
            }
            if (imageWith > 0 || imageHeight > 0) {
                requestCreator.resize(imageWith, imageHeight);
            }

            if (noFade) {
                requestCreator.noFade();
            }
            if (placeHolder != 0) {
                requestCreator.placeholder(placeHolder);
            } else {
                if (drawable != null) {
                    requestCreator.placeholder(drawable);
                }
            }

//            requestCreator.error(R.drawable.list_thumbnail_none_m);
            requestCreator.into(imageView, this);
        }

        @Override
        public void onSuccess() {
            ImageView imageView = imageViewRef.get();
            if (imageView != null) {
                imageView.setOnTouchListener(null);
            }
            isForceLoading = false;
            if (null != onImageLoadFinishedListener) {
                onImageLoadFinishedListener.onImageLoadFinished();
            }
        }

        @Override
        public void onError() {
            ImageView imageView = imageViewRef.get();
            if (imageView != null) {
                imageView.setOnTouchListener(this);
            }
            isForceLoading = false;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!isForceLoading) {
                    isForceLoading = true;
                    load(true);
                    return true;
                }
            }
            return false;
        }
    }

    public static String getMoreLargeUrl(String oldUrl) {
        return getQualityUrl(oldUrl, MORE_LARGE_SIZE);
    }

    public static String getMoreMiddleUrl(String oldUrl) {
        return getQualityUrl(oldUrl, MORE_MIDDLE_SIZE);
    }

    public static String getInnListUrl(String oldUrl) {
        return getQualityUrl(oldUrl, INN_POI_LIST_SIZE);
    }

    public static String getFeatureFoodUrl(String oldUrl) {
        return getQualityUrl(oldUrl, "/200.200/");
    }

    public static String getPoiAlbumUrl(String oldUrl) {
        return getQualityUrl(oldUrl, "/300.0/");
    }

}