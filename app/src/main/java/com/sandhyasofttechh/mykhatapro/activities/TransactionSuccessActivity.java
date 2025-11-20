//package com.sandhyasofttechh.mykhatapro.activities;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.view.animation.BounceInterpolator;
//import android.view.animation.OvershootInterpolator;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//
//import com.google.android.material.button.MaterialButton;
//import com.sandhyasofttechh.mykhatapro.R;
//
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class TransactionSuccessActivity extends AppCompatActivity {
//
//    public static final String EXTRA_AMOUNT = "extra_amount";
//    public static final String EXTRA_CUSTOMER = "extra_customer";
//    public static final String EXTRA_TRANSACTION_TYPE = "extra_transaction_type";
//
//    private CardView cardSuccessIcon;
//    private ImageView imgSuccess;
//    private TextView tvTitle;
//    private TextView tvAmount;
//    private TextView tvCustomer;
//    private TextView tvTransactionId;
//    private CardView cardDetails;
//    private LinearLayout containerButtons;
//    private MaterialButton btnAddAnother;
//    private MaterialButton btnViewHistory;
//    private MaterialButton btnClose;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_transaction_success);
//
//        initViews();
//        getTransactionData();
//        startAnimations();
//    }
//
//    private void initViews() {
//        cardSuccessIcon = findViewById(R.id.card_success_icon);
//        imgSuccess = findViewById(R.id.img_success);
//        tvTitle = findViewById(R.id.tv_title);
//        tvAmount = findViewById(R.id.tv_amount);
//        tvCustomer = findViewById(R.id.tv_customer);
//        tvTransactionId = findViewById(R.id.tv_transaction_id);
//        cardDetails = findViewById(R.id.card_details);
//        containerButtons = findViewById(R.id.container_buttons);
//        btnAddAnother = findViewById(R.id.btn_add_another);
//        btnViewHistory = findViewById(R.id.btn_view_history);
//        btnClose = findViewById(R.id.btn_close);
//
//        // Set initial visibility
//        cardSuccessIcon.setAlpha(0f);
//        cardSuccessIcon.setScaleX(0f);
//        cardSuccessIcon.setScaleY(0f);
//
//        tvTitle.setAlpha(0f);
//        tvTitle.setTranslationY(-50f);
//
//        cardDetails.setAlpha(0f);
//        cardDetails.setTranslationY(50f);
//
//        containerButtons.setAlpha(0f);
//        containerButtons.setTranslationY(50f);
//    }
//
//    private void getTransactionData() {
//        // Get transaction info from intent
//        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
//        String customer = getIntent().getStringExtra(EXTRA_CUSTOMER);
//        String transactionType = getIntent().getStringExtra(EXTRA_TRANSACTION_TYPE);
//
//        // Format amount
//        DecimalFormat df = new DecimalFormat("#,##,##0.00");
//        tvAmount.setText("₹" + df.format(amount));
//
//        // Set customer name
//        tvCustomer.setText(customer != null ? customer : "Customer");
//
//        // Generate transaction ID with timestamp
//        String transactionId = generateTransactionId();
//        tvTransactionId.setText("Transaction ID: " + transactionId);
//
//        // Set button click listeners
//        setupButtonListeners();
//    }
//
//    private void startAnimations() {
//        // Animation sequence with proper timing
//
//        // Step 1: Animate success icon (300ms delay, 600ms duration)
//        new Handler().postDelayed(() -> animateSuccessIcon(), 300);
//
//        // Step 2: Animate title (900ms delay)
//        new Handler().postDelayed(() -> animateTitle(), 900);
//
//        // Step 3: Animate details card (1200ms delay)
//        new Handler().postDelayed(() -> animateDetailsCard(), 1200);
//
//        // Step 4: Animate buttons (1500ms delay)
//        new Handler().postDelayed(() -> animateButtons(), 1500);
//    }
//
//    private void animateSuccessIcon() {
//        // Scale and fade in animation with bounce effect
//        AnimatorSet animatorSet = new AnimatorSet();
//
//        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardSuccessIcon, "scaleX", 0f, 1f);
//        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardSuccessIcon, "scaleY", 0f, 1f);
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardSuccessIcon, "alpha", 0f, 1f);
//
//        animatorSet.playTogether(scaleX, scaleY, alpha);
//        animatorSet.setDuration(600);
//        animatorSet.setInterpolator(new OvershootInterpolator(1.5f));
//        animatorSet.start();
//
//        // Rotate the check icon slightly
//        new Handler().postDelayed(() -> {
//            ObjectAnimator rotation = ObjectAnimator.ofFloat(imgSuccess, "rotation", 0f, 360f);
//            rotation.setDuration(800);
//            rotation.setInterpolator(new AccelerateDecelerateInterpolator());
//            rotation.start();
//        }, 300);
//    }
//
//    private void animateTitle() {
//        // Fade in and slide down animation
//        AnimatorSet animatorSet = new AnimatorSet();
//
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);
//        ObjectAnimator translateY = ObjectAnimator.ofFloat(tvTitle, "translationY", -50f, 0f);
//
//        animatorSet.playTogether(alpha, translateY);
//        animatorSet.setDuration(500);
//        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
//        animatorSet.start();
//    }
//
//    private void animateDetailsCard() {
//        // Fade in and slide up animation
//        AnimatorSet animatorSet = new AnimatorSet();
//
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardDetails, "alpha", 0f, 1f);
//        ObjectAnimator translateY = ObjectAnimator.ofFloat(cardDetails, "translationY", 50f, 0f);
//
//        animatorSet.playTogether(alpha, translateY);
//        animatorSet.setDuration(500);
//        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
//        animatorSet.start();
//
//        // Animate amount with scale pulse
//        new Handler().postDelayed(() -> {
//            ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvAmount, "scaleX", 1f, 1.2f, 1f);
//            ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvAmount, "scaleY", 1f, 1.2f, 1f);
//
//            AnimatorSet pulseSet = new AnimatorSet();
//            pulseSet.playTogether(scaleX, scaleY);
//            pulseSet.setDuration(600);
//            pulseSet.setInterpolator(new BounceInterpolator());
//            pulseSet.start();
//        }, 200);
//    }
//
//    private void animateButtons() {
//        // Fade in and slide up animation for button container
//        AnimatorSet animatorSet = new AnimatorSet();
//
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(containerButtons, "alpha", 0f, 1f);
//        ObjectAnimator translateY = ObjectAnimator.ofFloat(containerButtons, "translationY", 50f, 0f);
//
//        animatorSet.playTogether(alpha, translateY);
//        animatorSet.setDuration(500);
//        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
//        animatorSet.start();
//    }
//
//    private void setupButtonListeners() {
//        // Add Another Transaction button
//        btnAddAnother.setOnClickListener(v -> {
//            animateButtonClick(v, () -> {
//                Intent intent = new Intent(this, AddTransactionActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                finish();
//            });
//        });
//
//        // View History button
//        btnViewHistory.setOnClickListener(v -> {
//            animateButtonClick(v, () -> {
//                // TODO: Start your transaction list activity
//                // Intent intent = new Intent(this, TransactionHistoryActivity.class);
//                // startActivity(intent);
//                // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                finish();
//            });
//        });
//
//        // Close button
//        btnClose.setOnClickListener(v -> {
//            animateButtonClick(v, this::finish);
//        });
//    }
//
//    private void animateButtonClick(View button, Runnable action) {
//        // Scale down and up animation on button click
//        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
//        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
//
//        AnimatorSet scaleDown = new AnimatorSet();
//        scaleDown.playTogether(scaleDownX, scaleDownY);
//        scaleDown.setDuration(100);
//
//        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
//        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);
//
//        AnimatorSet scaleUp = new AnimatorSet();
//        scaleUp.playTogether(scaleUpX, scaleUpY);
//        scaleUp.setDuration(100);
//
//        scaleDown.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                scaleUp.start();
//            }
//        });
//
//        scaleUp.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (action != null) {
//                    action.run();
//                }
//            }
//        });
//
//        scaleDown.start();
//    }
//
//    private String generateTransactionId() {
//        // Generate transaction ID with timestamp
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
//        String timestamp = sdf.format(new Date());
//        return "#TXN" + timestamp;
//    }
//
//    @Override
//    public void onBackPressed() {
//        // Disable back button or handle it gracefully
//        super.onBackPressed();
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//    }
//}




package com.sandhyasofttechh.mykhatapro.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.sandhyasofttechh.mykhatapro.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CUSTOMER = "extra_customer";
    public static final String EXTRA_TRANSACTION_TYPE = "extra_transaction_type";

    private CardView cardSuccessIcon;
    private ImageView imgSuccess;
    private TextView tvTitle;
    private TextView tvAmount;
    private TextView tvCustomer;
    private TextView tvTransactionId;
    private CardView cardDetails;
    private LinearLayout containerButtons;
    private MaterialButton btnAddAnother;
    private MaterialButton btnViewHistory;
    private MaterialButton btnClose;

    private MediaPlayer successSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_success);

        initViews();
        initSound();
        getTransactionData();
        playSuccessSound();
        startAnimations();
    }

    private void initViews() {
        cardSuccessIcon = findViewById(R.id.card_success_icon);
        imgSuccess = findViewById(R.id.img_success);
        tvTitle = findViewById(R.id.tv_title);
        tvAmount = findViewById(R.id.tv_amount);
        tvCustomer = findViewById(R.id.tv_customer);
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        cardDetails = findViewById(R.id.card_details);
        containerButtons = findViewById(R.id.container_buttons);
        btnAddAnother = findViewById(R.id.btn_add_another);
        btnViewHistory = findViewById(R.id.btn_view_history);
        btnClose = findViewById(R.id.btn_close);

        // Set initial visibility
        cardSuccessIcon.setAlpha(0f);
        cardSuccessIcon.setScaleX(0f);
        cardSuccessIcon.setScaleY(0f);

        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(-50f);

        cardDetails.setAlpha(0f);
        cardDetails.setTranslationY(50f);

        containerButtons.setAlpha(0f);
        containerButtons.setTranslationY(50f);
    }

    private void initSound() {
        try {
            // Initialize MediaPlayer with success sound
            successSound = MediaPlayer.create(this, R.raw.success_sound);
            if (successSound != null) {
                successSound.setVolume(0.7f, 0.7f); // Set volume (0.0 to 1.0)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playSuccessSound() {
        try {
            if (successSound != null) {
                successSound.start();

                // Release media player after completion
                successSound.setOnCompletionListener(mp -> {
                    mp.release();
                    successSound = null;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTransactionData() {
        // Get transaction info from intent
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        String customer = getIntent().getStringExtra(EXTRA_CUSTOMER);
        String transactionType = getIntent().getStringExtra(EXTRA_TRANSACTION_TYPE);

        // Format amount
        DecimalFormat df = new DecimalFormat("#,##,##0.00");
        tvAmount.setText("₹" + df.format(amount));

        // Set customer name
        tvCustomer.setText(customer != null ? customer : "Customer");

        // Generate transaction ID with timestamp
        String transactionId = generateTransactionId();
        tvTransactionId.setText("Transaction ID: " + transactionId);

        // Set button click listeners
        setupButtonListeners();
    }

    private void startAnimations() {
        // Animation sequence with proper timing

        // Step 1: Animate success icon (300ms delay, 600ms duration)
        new Handler().postDelayed(() -> animateSuccessIcon(), 300);

        // Step 2: Animate title (900ms delay)
        new Handler().postDelayed(() -> animateTitle(), 900);

        // Step 3: Animate details card (1200ms delay)
        new Handler().postDelayed(() -> animateDetailsCard(), 1200);

        // Step 4: Animate buttons (1500ms delay)
        new Handler().postDelayed(() -> animateButtons(), 1500);
    }

    private void animateSuccessIcon() {
        // Scale and fade in animation with bounce effect
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardSuccessIcon, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardSuccessIcon, "scaleY", 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardSuccessIcon, "alpha", 0f, 1f);

        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(600);
        animatorSet.setInterpolator(new OvershootInterpolator(1.5f));
        animatorSet.start();

        // Rotate the check icon slightly
        new Handler().postDelayed(() -> {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(imgSuccess, "rotation", 0f, 360f);
            rotation.setDuration(800);
            rotation.setInterpolator(new AccelerateDecelerateInterpolator());
            rotation.start();
        }, 300);
    }

    private void animateTitle() {
        // Fade in and slide down animation
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(tvTitle, "translationY", -50f, 0f);

        animatorSet.playTogether(alpha, translateY);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void animateDetailsCard() {
        // Fade in and slide up animation
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardDetails, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(cardDetails, "translationY", 50f, 0f);

        animatorSet.playTogether(alpha, translateY);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();

        // Animate amount with scale pulse
        new Handler().postDelayed(() -> {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvAmount, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvAmount, "scaleY", 1f, 1.2f, 1f);

            AnimatorSet pulseSet = new AnimatorSet();
            pulseSet.playTogether(scaleX, scaleY);
            pulseSet.setDuration(600);
            pulseSet.setInterpolator(new BounceInterpolator());
            pulseSet.start();
        }, 200);
    }

    private void animateButtons() {
        // Fade in and slide up animation for button container
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(containerButtons, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(containerButtons, "translationY", 50f, 0f);

        animatorSet.playTogether(alpha, translateY);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void setupButtonListeners() {
        // Add Another Transaction button
        btnAddAnother.setOnClickListener(v -> {
            animateButtonClick(v, () -> {
                Intent intent = new Intent(this, AddTransactionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        });

        // View History button
        btnViewHistory.setOnClickListener(v -> {
            animateButtonClick(v, () -> {
                // TODO: Start your transaction list activity
                // Intent intent = new Intent(this, TransactionHistoryActivity.class);
                // startActivity(intent);
                // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        });

        // Close button
        btnClose.setOnClickListener(v -> {
            animateButtonClick(v, this::finish);
        });
    }

    private void animateButtonClick(View button, Runnable action) {
        // Scale down and up animation on button click
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setDuration(100);

        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleUp.start();
            }
        });

        scaleUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (action != null) {
                    action.run();
                }
            }
        });

        scaleDown.start();
    }

    private String generateTransactionId() {
        // Generate transaction ID with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return "#TXN" + timestamp;
    }

    @Override
    public void onBackPressed() {
        // Disable back button or handle it gracefully
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media player resources
        if (successSound != null) {
            if (successSound.isPlaying()) {
                successSound.stop();
            }
            successSound.release();
            successSound = null;
        }
    }
}