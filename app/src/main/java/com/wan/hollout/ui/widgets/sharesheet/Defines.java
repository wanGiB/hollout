package com.wan.hollout.ui.widgets.sharesheet;

/**
 * <p>
 * Defines all Json keys associated with branch request parameters.
 * </p>
 */
public class Defines {

    public enum Jsonkey {

        IdentityID("identity_id"),
        Identity("identity"),
        DeviceFingerprintID("device_fingerprint_id"),
        SessionID("session_id"),
        LinkClickID("link_click_id"),
        GoogleSearchInstallReferrer("google_search_install_referrer"),
        GooglePlayInstallReferrer("install_referrer_extras"),
        FaceBookAppLinkChecked("facebook_app_link_checked"),
        BranchLinkUsed("branch_used"),
        ReferringBranchIdentity("referring_branch_identity"),
        BranchIdentity("branch_identity"),
        BranchKey("branch_key"),

        Bucket("bucket"),
        DefaultBucket("default"),
        Amount("amount"),
        CalculationType("calculation_type"),
        Location("location"),
        Type("type"),
        CreationSource("creation_source"),
        Prefix("prefix"),
        Expiration("expiration"),
        Event("event"),
        Metadata("metadata"),
        CommerceData("commerce_data"),
        ReferralCode("referral_code"),
        Total("total"),
        Unique("unique"),
        Length("length"),
        Direction("direction"),
        BeginAfterID("begin_after_id"),
        Link("link"),
        ReferringData("referring_data"),
        ReferringLink("referring_link"),
        IsFullAppConv("is_full_app_conversion"),
        Data("data"),
        OS("os"),
        HardwareID("hardware_id"),
        HardwareIDType("hardware_id_type"),
        HardwareIDTypeVendor("vendor_id"),
        HardwareIDTypeRandom("random"),
        IsHardwareIDReal("is_hardware_id_real"),
        AppVersion("app_version"),
        OSVersion("os_version"),
        Country("country"),
        Language("language"),
        IsReferrable("is_referrable"),
        Update("update"),
        URIScheme("uri_scheme"),
        AppIdentifier("app_identifier"),
        LinkIdentifier("link_identifier"),
        GoogleAdvertisingID("google_advertising_id"),
        LATVal("lat_val"),
        Debug("debug"),
        Brand("brand"),
        Model("model"),
        ScreenDpi("screen_dpi"),
        ScreenHeight("screen_height"),
        ScreenWidth("screen_width"),
        WiFi("wifi"),
        LocalIP("local_ip"),

        Clicked_Branch_Link("+clicked_branch_link"),
        IsFirstSession("+is_first_session"),
        AndroidDeepLinkPath("$android_deeplink_path"),

        AndroidAppLinkURL("android_app_link_url"),
        AndroidPushNotificationKey("branch"),
        AndroidPushIdentifier("push_identifier"),
        ForceNewBranchSession("branch_force_new_session"),

        CanonicalIdentifier("$canonical_identifier"),
        ContentTitle("og:title"),
        ContentDesc("og:description"),
        ContentImgUrl("og:image_url"),
        CanonicalUrl("$canonical_url"),

        ContentType("$content_type"),
        PublicallyIndexable("$publicly_indexable"),
        ContentKeyWords("$keywords"),
        ContentExpiryTime("$exp_date"),
        Params("params"),
        SharedLink("$shared_link"),
        ShareError("$share_error"),


        External_Intent_URI("external_intent_uri"),
        External_Intent_Extra("external_intent_extra"),
        Last_Round_Trip_Time("lrtt"),
        Branch_Round_Trip_Time("brtt"),
        Branch_Instrumentation("instrumentation"),
        Queue_Wait_Time("qwt"),

        BranchViewData("branch_view_data"),
        BranchViewID("id"),
        BranchViewAction("action"),
        BranchViewNumOfUse("number_of_use"),
        BranchViewUrl("url"),
        BranchViewHtml("html"),

        Path("path"),
        ViewList("view_list"),
        ContentActionView("view"),
        ContentPath("content_path"),
        ContentNavPath("content_nav_path"),
        ReferralLink("referral_link"),
        ContentData("content_data"),
        ContentEvents("events"),
        ContentAnalyticsMode("content_analytics_mode"),
        ContentDiscovery("cd"),
        Environment("environment"),
        InstantApp("INSTANT_APP"),
        NativeApp("FULL_APP");

        private String key = "";

        Jsonkey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * <p>
     * Defines all server path for the requests
     * </p>
     */
    public enum RequestPath {
        RedeemRewards("v1/redeem"),
        GetURL("v1/url"),
        RegisterInstall("v1/install"),
        RegisterClose("v1/close"),
        RegisterOpen("v1/open"),
        RegisterView("v1/register-view"),
        Referrals("v1/referrals/"),
        SendAPPList("v1/applist"),
        GetCredits("v1/credits/"),
        GetCreditHistory("v1/credithistory"),
        CompletedAction("v1/event"),
        IdentifyUser("v1/profile"),
        Logout("v1/logout"),
        GetReferralCode("v1/referralcode"),
        ValidateReferralCode("v1/referralcode/"),
        ApplyReferralCode("v1/applycode/"),
        DebugConnect("v1/debug/connect"),
        ContentEvent("v1/content-events");

        private String key = "";

        RequestPath(String key) {
            this.key = key;
        }

        public String getPath() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * <p>
     * Defines link parameter keys
     * </p>
     */
    public enum LinkParam {
        Tags("tags"),
        Alias("alias"),
        Type("type"),
        Duration("duration"),
        Channel("channel"),
        Feature("feature"),
        Stage("stage"),
        Campaign("campaign"),
        Data("data"),
        URL("url");

        private String key = "";

        LinkParam(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }

    }

}
