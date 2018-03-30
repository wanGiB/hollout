package com.wan.hollout.ui.widgets.sharesheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.wan.hollout.R;
import com.wan.hollout.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Class that provides a chooser dialog with customised share options to share a link.
 * Class provides customised and easy way of sharing a deep link with other applications. </p>
 */
class ShareLinkManager {
    /* The custom chooser dialog for selecting an application to share the link. */
    BottomSheetDialog shareDlg_;
    ShareSheet.BranchLinkShareListener callback_;

    /* List of apps available for sharing. */
    private List<ResolveInfo> appList_;
    /* Intent for sharing with selected application.*/
    private Intent shareLinkIntent_;
    /* Background color for the list view in enabled state. */
    private final int BG_COLOR_ENABLED = Color.argb(60, 17, 4, 56);
    /* Background color for the list view in disabled state. */
    private final int BG_COLOR_DISABLED = Color.argb(20, 17, 4, 56);
    /* Current activity context.*/
    Context context_;
    /* Default height for the list item.*/
    private static int viewItemMinHeight_ = 100;
    /* Indicates whether a sharing is in progress*/
    private boolean isShareInProgress_ = false;
    /* Styleable resource for share sheet
    .*/
    private int shareDialogThemeID_ = -1;

    private ShareSheet.ShareLinkBuilder builder_;
    final int padding = 5;
    final int leftMargin = 100;
    private List<String> includeInShareSheet = new ArrayList<>();
    private List<String> excludeFromShareSheet = new ArrayList<>();

    /**
     * Creates an application selector and shares a link on user selecting the application.
     *
     * @return Instance of the {@link Dialog} holding the share view. Null if sharing dialog is not created due to any error.
     */
    public Dialog shareLink(ShareSheet.ShareLinkBuilder builder) {
        builder_ = builder;
        context_ = builder.getActivity();
        callback_ = builder.getCallback();
        shareLinkIntent_ = new Intent(Intent.ACTION_SEND);
        shareLinkIntent_.setType("text/plain");
        shareDialogThemeID_ = builder.getStyleResourceID();
        includeInShareSheet = builder.getIncludedInShareSheet();
        excludeFromShareSheet = builder.getExcludedFromShareSheet();

        try {
            createShareDialog(builder.getPreferredOptions());
        } catch (Exception e) {
            e.printStackTrace();
            if (callback_ != null) {
                callback_.onLinkShareResponse(null, null, new Exception("Trouble sharing link", new Exception("No share option")));
            } else {
                Log.i("BranchSDK", "Unable create share options. Couldn't find applications on device to share the link.");
            }
        }
        return shareDlg_;
    }

    /**
     * Dismiss the share dialog if showing. Should be called on activity stopping.
     *
     * @param animateClose A {@link Boolean} to specify whether to close the dialog with an animation.
     *                     A value of true will close the dialog with an animation. Setting this value
     *                     to false will close the Dialog immediately.
     */
    public void cancelShareLinkDialog(boolean animateClose) {
        if (shareDlg_ != null && shareDlg_.isShowing()) {
            if (animateClose) {
                // Cancel the dialog with animation
                shareDlg_.cancel();
            } else {
                // Dismiss the dialog immediately
                shareDlg_.dismiss();
            }
        }
    }

    /**
     * Create a custom chooser dialog with available share options.
     */
    private void createShareDialog(List<SharingHelper.SHARE_WITH> preferredOptions) {
        final PackageManager packageManager = context_.getPackageManager();
        final List<ResolveInfo> preferredApps = new ArrayList<>();
        final List<ResolveInfo> matchingApps = packageManager.queryIntentActivities(shareLinkIntent_, PackageManager.MATCH_DEFAULT_ONLY);
        List<ResolveInfo> cleanedMatchingApps = new ArrayList<>();
        final List<ResolveInfo> cleanedMatchingAppsFinal = new ArrayList<>();
        ArrayList<SharingHelper.SHARE_WITH> packagesFilterList = new ArrayList<>(preferredOptions);

        /* Get all apps available for sharing and the available preferred apps. */
        for (ResolveInfo resolveInfo : matchingApps) {
            SharingHelper.SHARE_WITH foundMatching = null;
            String packageName = resolveInfo.activityInfo.packageName;
            for (SharingHelper.SHARE_WITH PackageFilter : packagesFilterList) {
                if (resolveInfo.activityInfo != null && packageName.toLowerCase().contains(PackageFilter.toString().toLowerCase())) {
                    foundMatching = PackageFilter;
                    break;
                }
            }
            if (foundMatching != null) {
                preferredApps.add(resolveInfo);
                preferredOptions.remove(foundMatching);
            }
        }
        /* Create all app list with copy link item. */
        matchingApps.removeAll(preferredApps);
        matchingApps.addAll(0, preferredApps);

        //if apps are explicitly being included, add only those, otherwise at the else statement add them all
        if (includeInShareSheet.size() > 0) {
            for (ResolveInfo r : matchingApps) {
                if (includeInShareSheet.contains(r.activityInfo.packageName)) {
                    cleanedMatchingApps.add(r);
                }
            }
        } else {
            cleanedMatchingApps = matchingApps;
        }

        //does our list contain explicitly excluded items? do not carry them into the next list
        for (ResolveInfo r : cleanedMatchingApps) {
            if (!excludeFromShareSheet.contains(r.activityInfo.packageName)) {
                cleanedMatchingAppsFinal.add(r);
            }
        }

        //make sure our "show more" option includes preferred apps
        for (ResolveInfo r : matchingApps) {
            for (SharingHelper.SHARE_WITH shareWith : packagesFilterList)
                if (shareWith.toString().equalsIgnoreCase(r.activityInfo.packageName)) {
                    cleanedMatchingAppsFinal.add(r);
                }
        }

        cleanedMatchingAppsFinal.add(new CopyLinkItem());
        matchingApps.add(new CopyLinkItem());
        preferredApps.add(new CopyLinkItem());

        if (preferredApps.size() > 1) {
            if (matchingApps.size() > preferredApps.size()) {
                preferredApps.add(new MoreShareItem());
            }
            appList_ = preferredApps;
        } else {
            appList_ = cleanedMatchingAppsFinal;
        }

        /* Copy link option will be always there for sharing. */
        final ChooserArrayAdapter adapter = new ChooserArrayAdapter(context_, appList_);

        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(adapter);

        View shareSheetView = LayoutInflater.from(context_).inflate(R.layout.share_sheet, null);
        RecyclerView inviteFriendsRecyclerView = shareSheetView.findViewById(R.id.invite_friends_recycler_view);
        inviteFriendsRecyclerView.setHorizontalFadingEdgeEnabled(false);
        inviteFriendsRecyclerView.setBackgroundColor(Color.WHITE);

        TextView sharingTitleView = shareSheetView.findViewById(R.id.sharing_title);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context_, 3);
        inviteFriendsRecyclerView.setLayoutManager(gridLayoutManager);
        inviteFriendsRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        sharingTitleView.setText(builder_.getSharingTitle());

        shareDlg_ = new BottomSheetDialog(context_);
        shareDlg_.setContentView(shareSheetView);
        shareDlg_.show();
        if (callback_ != null) {
            callback_.onShareLinkDialogLaunched();
        }
        shareDlg_.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (callback_ != null) {
                    callback_.onShareLinkDialogDismissed();
                    callback_ = null;
                }
                // Release  context to prevent leaks
                if (!isShareInProgress_) {
                    context_ = null;
                    builder_ = null;
                }
                shareDlg_ = null;
            }
        });
    }


    /**
     * Invokes a sharing client with a link created by the given json objects.
     *
     * @param selectedResolveInfo The {@link ResolveInfo} corresponding to the selected sharing client.
     */
    private void invokeSharingClient(final ResolveInfo selectedResolveInfo) {
        isShareInProgress_ = true;
        final String channelName = selectedResolveInfo.loadLabel(context_.getPackageManager()).toString();
        BranchShortLinkBuilder shortLinkBuilder = builder_.getShortLinkBuilder();
        shortLinkBuilder.setChannel(channelName);
        String shareUrl = shortLinkBuilder.getShortUrl();
        shareWithClient(selectedResolveInfo, shareUrl, channelName);
        if (callback_ != null) {
            callback_.onLinkShareResponse(shareUrl, channelName, null);
        }
    }

    private void shareWithClient(ResolveInfo selectedResolveInfo, String url, String channelName) {
        if (callback_ != null) {
            callback_.onLinkShareResponse(url, channelName, null);
        } else {
            Log.i("BranchSDK", "Shared link with " + channelName);
        }
        if (selectedResolveInfo instanceof CopyLinkItem) {
            addLinkToClipBoard(url, builder_.getShareMsg());
        } else {
            shareLinkIntent_.setPackage(selectedResolveInfo.activityInfo.packageName);
            String shareSub = builder_.getShareSub();
            String shareMsg = builder_.getShareMsg();
            if (shareSub != null && shareSub.trim().length() > 0) {
                shareLinkIntent_.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            }
            shareLinkIntent_.putExtra(Intent.EXTRA_TEXT, shareMsg + "\n" + url);
            context_.startActivity(shareLinkIntent_);
        }
    }

    /**
     * Adds a given link to the clip board.
     *
     * @param url   A {@link String} to add to the clip board
     * @param label A {@link String} label for the adding link
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void addLinkToClipBoard(String url, String label) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context_.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(url);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context_.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, url);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context_, builder_.getUrlCopiedMessage(), Toast.LENGTH_SHORT).show();
    }

    /*
     * Adapter class for creating list of available share options
     */
    private class ChooserArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ResolveInfo> appList;
        private Context context;

        ChooserArrayAdapter(Context context, List<ResolveInfo> appList) {
            this.context = context;
            this.appList = appList;
        }

        @Override
        public int getItemCount() {
            return appList_.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_sheet_item_layout, parent, false);
            return new ChooserItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ChooserItemHolder chooserItemHolder = (ChooserItemHolder) holder;
            chooserItemHolder.bindData(context, appList.get(position));
        }

        class ChooserItemHolder extends RecyclerView.ViewHolder {

            private ImageView appIconView;
            private AppCompatTextView appNameTextView;

            ChooserItemHolder(View itemView) {
                super(itemView);
                appIconView = itemView.findViewById(R.id.app_icon);
                appNameTextView = itemView.findViewById(R.id.app_name);
            }

            void bindData(Context context, final ResolveInfo resolveInfo) {
                appNameTextView.setText(resolveInfo.loadLabel(context.getPackageManager()).toString());
                appIconView.setImageDrawable(resolveInfo.loadIcon(context.getPackageManager()));
                itemView.setTag(resolveInfo);
                itemView.setClickable(true);
                itemView.setBackgroundColor(Color.WHITE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UiUtils.blinkView(view);
                        if (callback_ != null) {
                            String selectedChannelName;
                            selectedChannelName = resolveInfo.loadLabel(context_.getPackageManager()).toString();
                            callback_.onChannelSelected(selectedChannelName);
                        }
                        invokeSharingClient(resolveInfo);
                        if (shareDlg_ != null) {
                            shareDlg_.cancel();
                        }
                    }
                });
            }
        }

    }

    /**
     * Class for sharing item more
     */
    private class MoreShareItem extends ResolveInfo {
        @SuppressWarnings("NullableProblems")
        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return builder_.getMoreOptionText();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Drawable loadIcon(PackageManager pm) {
            return builder_.getMoreOptionIcon();
        }
    }

    /**
     * Class for Sharing Item copy URl
     */
    private class CopyLinkItem extends ResolveInfo {
        @SuppressWarnings("NullableProblems")
        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return builder_.getCopyURlText();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Drawable loadIcon(PackageManager pm) {
            return builder_.getCopyUrlIcon();
        }

    }

}
