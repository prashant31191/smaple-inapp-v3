package com.eliteinfo.wallpaper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eliteinfo.temp.ActPurchase;
import com.example.android.trivialdrivesample.util.IabBroadcastReceiver;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.patel on 6/9/2017.
 */

public class ActWallpaper extends Activity  implements IabBroadcastReceiver.IabBroadcastListener,DialogInterface.OnClickListener
{
    String TAG = "ActWallpaper";

    Button btnApply1,
            btnApply2,
            btnApply3,
            btnApply4,
            btnApply5,
            btnApply6;

    Button btnSubscribe;

    int selectedDrawable = R.drawable.image1;













    // The helper object
    IabHelper mHelper;
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;


    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteGas = false;

    // Will the subscription auto-renew?
    boolean mAutoRenewEnabled = false;

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";

    // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";

    static final String SKU_INFINITE_GAS_MONTHLY = "com.subscription.monthly10";
    static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_wallpaper);

        initialization();
        setClickEvent();

        setupInApp();

    }

    private void setupInApp() {
        try
        {
            String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";

            base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAokMPsCx06fEFAiMk80fgUZLLAo/kGUml39WHfrvQFzhNKk9VA8uFXspZQi58xD2E34by6Mv6kYCsw2zJRpi+rQ37FLLd+VU5vFngYwUffYTLn6j/gE7dc+3iodaUAAxjlbwhVyC2dSg/JsQN163JHGTWAmiuC7fNFc05wXtNCqQhetgwe6YXm11zE5+nZp45eE/ppRi+Ow4vnkGe3fvfwxTXw1/x6kDJv9w2SCFDzXdMMRmwiuvWXc6YDWcqqJzzcTkgUWItKboZRGaLbBR0GQioMbfEEJzL6299iig2HAoEA/V9DBf0scVuNGE1Kq6+PkhRkrlXnKtK5hPFbooH6wIDAQAB";
            // Some sanity checks to see if the developer (that's you!) really followed the
            // instructions to run this sample (don't put these checks on your app!)
            if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
                throw new RuntimeException("Please put your app's public key in MainActivity.java. See README.");
            }
            if (getPackageName().startsWith("com.example")) {
                throw new RuntimeException("Please change the sample's package name! See README.");
            }

            // Create the helper, passing it our context and the public key to verify signatures with
            Log.d(TAG, "Creating IAB helper.");
            mHelper = new IabHelper(this, base64EncodedPublicKey);

            // enable debug logging (for a production application, you should set this to false).
            mHelper.enableDebugLogging(true);

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
            Log.d(TAG, "Starting setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    Log.d(TAG, "Setup finished.");

                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        complain("Problem setting up in-app billing: " + result);
                        return;
                    }

                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null) return;

                    // Important: Dynamically register for broadcast messages about updated purchases.
                    // We register the receiver here instead of as a <receiver> in the Manifest
                    // because we always call getPurchases() at startup, so therefore we can ignore
                    // any broadcasts sent while the app isn't running.
                    // Note: registering this listener in an Activity is a bad idea, but is done here
                    // because this is a SAMPLE. Regardless, the receiver must be registered after
                    // IabHelper is setup, but before first call to getPurchases().
                    mBroadcastReceiver = new IabBroadcastReceiver(ActWallpaper.this);
                    IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                    registerReceiver(mBroadcastReceiver, broadcastFilter);

                    // IAB is fully set up. Now, let's get an inventory of stuff we own.
                    Log.d(TAG, "Setup successful. Querying inventory.");
                    try {
                        mHelper.queryInventoryAsync(mGotInventoryListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        complain("Error querying inventory. Another async operation in progress.");
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initialization() {
        try {
            btnSubscribe = (Button) findViewById(R.id.btnSubscribe);

            btnApply1 = (Button) findViewById(R.id.btnApply1);
            btnApply2 = (Button) findViewById(R.id.btnApply2);
            btnApply3 = (Button) findViewById(R.id.btnApply3);
            btnApply4 = (Button) findViewById(R.id.btnApply4);
            btnApply5 = (Button) findViewById(R.id.btnApply5);
            btnApply6 = (Button) findViewById(R.id.btnApply6);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setClickEvent() {
        try {





            btnSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onInfiniteGasButtonClicked(v);
                }
            });
            btnApply1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDrawable = R.drawable.image1;
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask();
                    setWallpaperTask.execute();
                }
            });

            btnApply2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDrawable = R.drawable.image2;
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask();
                    setWallpaperTask.execute();
                }
            });

            btnApply3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDrawable = R.drawable.image3;
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask();
                    setWallpaperTask.execute();
                }
            });

            btnApply4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDrawable = R.drawable.image4;
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask();
                    setWallpaperTask.execute();
                }
            });

            btnApply5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDrawable = R.drawable.image5;
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask();
                    setWallpaperTask.execute();
                }
            });

            btnApply6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDrawable = R.drawable.image6;
                    SetWallpaperTask setWallpaperTask = new SetWallpaperTask();
                    setWallpaperTask.execute();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }


    private class SetWallpaperTask extends AsyncTask<Void, Void, Void> {

        //Drawable wallpaperDrawable;
        Bitmap mBitmap;

        @Override
        protected void onPreExecute() {
            // Runs on the UI thread
            // Do any pre-executing tasks here, for example display a progress bar
            Log.d(TAG, "About to set wallpaper...");
            Toast.makeText(ActWallpaper.this, "Applying New Wallpaper....", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Runs on the background thread

             mBitmap = BitmapFactory.decodeResource(getResources(), selectedDrawable);

            /*
            WallpaperManager wallpaperManager = WallpaperManager.getInstance
                    (getApplicationContext());*/
            //wallpaperDrawable = wallpaperManager.getDrawable();
       //     wallpaperDrawable = getResources().getDrawable(selectedDrawable);
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            // Runs on the UI thread
            // Here you can perform any post-execute tasks, for example remove the
            // progress bar (if you set one).

            try{

                WallpaperManager myWallpaperManager = WallpaperManager
                        .getInstance(getApplicationContext());

                try {
                    if(mBitmap !=null) {
                        myWallpaperManager.setBitmap(mBitmap);
                        Toast.makeText(ActWallpaper.this, "Wallpaper set", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ActWallpaper.this,
                            "Error setting wallpaper", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }




          /*  if (wallpaperDrawable != null) {
               // wallpaperDrawable.setAlpha(50);
                getWindow().setBackgroundDrawable(wallpaperDrawable);
                Log.d(TAG, "New wallpaper set");
            } else {
                Log.d(TAG, "Wallpaper was null");
            }*/
        }

    }







    // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
    // flow for subscription.
    public void onInfiniteGasButtonClicked(View view) {
        if (!mHelper.subscriptionsSupported()) {
            complain("Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        CharSequence[] options;
        if (!mSubscribedToInfiniteGas || !mAutoRenewEnabled) {
            // Both subscription options should be available
            options = new CharSequence[2];
            options[0] = getString(R.string.subscription_period_monthly);
            options[1] = getString(R.string.subscription_period_yearly);
            mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
            mSecondChoiceSku = SKU_INFINITE_GAS_YEARLY;
        } else {
            // This is the subscription upgrade/downgrade path, so only one option is valid
            options = new CharSequence[1];
            if (mInfiniteGasSku.equals(SKU_INFINITE_GAS_MONTHLY)) {
                // Give the option to upgrade to yearly
                options[0] = getString(R.string.subscription_period_yearly);
                mFirstChoiceSku = SKU_INFINITE_GAS_YEARLY;
            } else {
                // Give the option to downgrade to monthly
                options[0] = getString(R.string.subscription_period_monthly);
                mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
            }
            mSecondChoiceSku = "";
        }

        int titleResId;
        if (!mSubscribedToInfiniteGas) {
            titleResId = R.string.subscription_period_prompt;
        } else if (!mAutoRenewEnabled) {
            titleResId = R.string.subscription_resignup_prompt;
        } else {
            titleResId = R.string.subscription_update_prompt;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResId)
                .setSingleChoiceItems(options, 0 /* checkedItem */,this)
                .setPositiveButton(R.string.subscription_prompt_continue, this)
                .setNegativeButton(R.string.subscription_prompt_cancel, this);
        AlertDialog dialog = builder.create();
        dialog.show();
    }




    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");



            // First find out which subscription is auto renewing
            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
                    || (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedToInfiniteGas)
            {
                Log.d(TAG, "------mSubscribedToInfiniteGas-----true--");
                //mTank = TANK_MAX;
            }
            else
            {
                Log.d(TAG, "------mSubscribedToInfiniteGas-----false--");
            }

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };







    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }













    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (id == 0 /* First choice item */) {
            mSelectedSubscriptionPeriod = mFirstChoiceSku;
        } else if (id == 1 /* Second choice item */) {
            mSelectedSubscriptionPeriod = mSecondChoiceSku;
        } else if (id == DialogInterface.BUTTON_POSITIVE /* continue button */) {
            /* TODO: for security, generate your payload here for verification. See the comments on
             *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
             *        an empty string, but on a production app you should carefully generate
             *        this. */
            String payload = "";

            if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
                // The user has not changed from the default selection
                mSelectedSubscriptionPeriod = mFirstChoiceSku;
            }

            List<String> oldSkus = null;
            if (!TextUtils.isEmpty(mInfiniteGasSku)
                    && !mInfiniteGasSku.equals(mSelectedSubscriptionPeriod)) {
                // The user currently has a valid subscription, any purchase action is going to
                // replace that subscription
                oldSkus = new ArrayList<String>();
                oldSkus.add(mInfiniteGasSku);
            }


            Log.d(TAG, "Launching purchase flow for gas subscription.");
            try {
                mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
            } catch (IabHelper.IabAsyncInProgressException e) {
                complain("Error launching purchase flow. Another async operation in progress.");
            }
            // Reset the dialog options
            mSelectedSubscriptionPeriod = "";
            mFirstChoiceSku = "";
            mSecondChoiceSku = "";
        } else if (id != DialogInterface.BUTTON_NEGATIVE) {
            // There are only four buttons, this should not happen
            Log.e(TAG, "Unknown button clicked in subscription dialog: " + id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    /** Verifies the developer payload of a purchase. */
    @SuppressLint("LongLogTag")
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        Log.d("==verifyDeveloperPayload==","===payload="+payload);

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }





    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);

                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");

                return;
            }

            Log.d(TAG, "Purchase successful.");
            if (purchase.getSku().equals(SKU_INFINITE_GAS_MONTHLY) || purchase.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
             //   mTank = TANK_MAX;
            }
        }
    };

}
