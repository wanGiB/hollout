package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.wan.hollout.R;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * @author Wan Clem
 */
@SuppressWarnings("RedundantCast")
public class LinkPreview extends RelativeLayout {
    private static String TAG = LinkPreview.class.getSimpleName();
    private ImageView mImgViewImage;
    private TextView mTxtViewTitle;
    private TextView mTxtViewDescription;
    private TextView mTxtViewSiteName;
    private Context mContext;
    private Handler mHandler;
    private String mTitle = null;
    private String mDescription = null;
    private String mImageLink = null;
    private String mSiteName = null;
    private String mLink;
    private PreviewListener mListener;

    public LinkPreview(Context context) {
        super(context);
        initialize(context);
    }

    public LinkPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public LinkPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        inflate(context, R.layout.preview_layout, this);
        mImgViewImage = (ImageView) findViewById(R.id.imgViewImage);
        mTxtViewTitle = (TextView) findViewById(R.id.txtViewTitle);
        mTxtViewDescription = (TextView) findViewById(R.id.txtViewDescription);
        mTxtViewSiteName = (TextView) findViewById(R.id.txtViewSiteName);
        mHandler = new Handler(mContext.getMainLooper());
    }

    public void setListener(PreviewListener listener) {
        this.mListener = listener;
    }

    public void setData(final String url) {
        if (!TextUtils.isEmpty(url)) {
            clear();
            String cachedDocString = HolloutPreferences.getDocumentString(url);
            if (StringUtils.isNotEmpty(cachedDocString)) {
                Document document = Jsoup.parse(cachedDocString);
                if (document != null) {
                    try {
                        processDocument(document, url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                fetchDocFromNet(url);
            }
        }
    }

    private void fetchDocFromNet(final String url) {
        new AsyncTask<String, Void, Document>() {

            @Override
            protected Document doInBackground(String... strings) {
                try {
                    Document document = Jsoup.connect(strings[0].startsWith("www") ? "http://" + strings[0] : strings[0]).get();
                    HolloutPreferences.saveDocument(strings[0], document.toString());
                    return document;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Document document) {
                super.onPostExecute(document);
                if (document != null) {
                    try {
                        processDocument(document, url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    private void processDocument(final Document doc, final String url) throws IOException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Elements titleElements;
                Elements descriptionElements;
                Elements imageElements;
                Elements siteElements;
                Elements linkElements;
                String site = "";
                titleElements = doc.select("title");
                descriptionElements = doc.select("meta[name=description]");
                if (url.contains("bhphotovideo")) {
                    imageElements = doc.select("image[id=mainImage]");
                    site = "bhphotovideo";
                } else if (url.contains("www.amazon.com")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.com";
                } else if (url.contains("www.amazon.co.uk")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.co.uk";
                } else if (url.contains("www.amazon.de")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.de";
                } else if (url.contains("www.amazon.fr")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.fr";
                } else if (url.contains("www.amazon.it")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.it";
                } else if (url.contains("www.amazon.es")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.es";
                } else if (url.contains("www.amazon.ca")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.ca";
                } else if (url.contains("www.amazon.co.jp")) {
                    imageElements = doc.select("img[data-old-hires]");
                    site = "www.amazon.co.jp";
                } else if (url.contains("m.clove.co.uk")) {
                    imageElements = doc.select("img[id]");
                    site = "m.clove.co.uk";
                } else if (url.contains("www.clove.co.uk")) {
                    imageElements = doc.select("li[data-thumbnail-path]");
                    site = "www.clove.co.uk";
                } else {
                    imageElements = doc.select("img");
                }
                mImageLink = getImageLinkFromSource(imageElements, site);
                siteElements = doc.select("meta[property=og:site_name]");
                linkElements = doc.select("meta[property=og:url]");

                if (titleElements != null && titleElements.size() > 0) {
                    mTitle = titleElements.get(0).text();
                }
                if (descriptionElements != null && descriptionElements.size() > 0) {
                    mDescription = descriptionElements.get(0).attr("content");
                }
                if (linkElements != null && linkElements.size() > 0) {
                    mLink = linkElements.get(0).attr("content");
                } else {
                    linkElements = doc.select("link[rel=canonical]");
                    if (linkElements != null && linkElements.size() > 0) {
                        mLink = linkElements.get(0).attr("href");
                    }
                }
                if (siteElements != null && siteElements.size() > 0) {
                    mSiteName = siteElements.get(0).attr("content");
                }

                if (getTitle() != null) {
                    Log.v(TAG, getTitle());
                    if (getTitle().length() >= 50)
                        mTitle = getTitle().substring(0, 49) + "...";
                    mTxtViewTitle.setText(getTitle());
                }
                if (getDescription() != null) {
                    Log.v(TAG, getDescription());
                    if (getDescription().length() >= 100)
                        mDescription = getDescription().substring(0, 99) + "...";
                    mTxtViewDescription.setText(getDescription());
                }
                if (getImageLink() != null && !getImageLink().equals("")) {
                    Log.v(TAG, getImageLink());
                    UiUtils.loadImage((Activity) mContext, getImageLink(), mImgViewImage);
                } else {
                    mImgViewImage.setImageResource(R.drawable.ease_default_image);
                }
                if (url.toLowerCase().contains("amazon"))

                    if (getSiteName() == null || getSiteName().equals(""))
                        mSiteName = "Amazon";
                if (getSiteName() != null) {
                    Log.v(TAG, getSiteName());
                    if (getSiteName().length() >= 30)
                        mSiteName = getSiteName().substring(0, 29) + "...";
                    mTxtViewSiteName.setText(getSiteName());
                }
                Log.v(TAG, "Link: " + getLink());
                if (mListener != null) {
                    mListener.onDataReady(LinkPreview.this);
                }
            }
        });

    }


    private String getImageLinkFromSource(Elements elements, String site) {
        String imageLink = null;
        if (elements != null && elements.size() > 0) {
            switch (site) {
                case "m.clove.co.uk":
                case "bhphotovideo":
                    imageLink = elements.get(0).attr("src");
                    break;
                case "www.amazon.com":
                case "www.amazon.co.uk":
                case "www.amazon.de":
                case "www.amazon.fr":
                case "www.amazon.it":
                case "www.amazon.es":
                case "www.amazon.ca":
                case "www.amazon.co.jp":
                    imageLink = elements.get(0).attr("data-old-hires");
                    if (TextUtils.isEmpty(imageLink)) {
                        imageLink = elements.get(0).attr("src");
                        if (imageLink.contains("data:image/jpeg;base64,")) {
                            imageLink = elements.get(0).attr("data-a-dynamic-image");
                            if (!TextUtils.isEmpty(imageLink)) {
                                String[] array = imageLink.split(":\\[");
                                if (array.length > 1) {
                                    imageLink = array[0];
                                    if (!TextUtils.isEmpty(imageLink)) {
                                        imageLink = imageLink.replace("{\"", "");
                                        imageLink = imageLink.replace("\"", "");
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "www.clove.co.uk":
                    imageLink = "https://www.clove.co.uk" + elements.get(0).attr("data-thumbnail-path");
                    break;
                default:
                    imageLink = elements.first().attr("src");
                    break;
            }

        }
        return imageLink;
    }

    private void clear() {
        mImgViewImage.setImageResource(0);
        mTxtViewTitle.setText("");
        mTxtViewDescription.setText("");
        mTxtViewSiteName.setText("");
        mTitle = null;
        mDescription = null;
        mImageLink = null;
        mSiteName = null;
        mLink = null;
    }

    public interface PreviewListener {
        void onDataReady(LinkPreview linkPreview);
    }

    private void runOnUiThread(Runnable r) {
        mHandler.post(r);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public String getSiteName() {
        return mSiteName;
    }

    public String getLink() {
        return mLink;
    }

}
