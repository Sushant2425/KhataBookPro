package com.sandhyasofttechh.mykhatapro.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sandhyasofttechh.mykhatapro.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FullScreenImageActivity extends AppCompatActivity {

    private PhotoView photoView;
    private TextView tvBusinessName;
    private FloatingActionButton fabDownload;
    private String imageUrl;
    private String businessName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        photoView = findViewById(R.id.photo_view);
        tvBusinessName = findViewById(R.id.tv_business_name);
        fabDownload = findViewById(R.id.fab_download);

        imageUrl = getIntent().getStringExtra("image_url");
        businessName = getIntent().getStringExtra("business_name");

        tvBusinessName.setText(businessName != null ? businessName : "MyKhata Pro");

        Glide.with(this)
                .load(imageUrl != null && !imageUrl.equals("default") ? imageUrl : R.drawable.img)
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(photoView);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        fabDownload.setOnClickListener(v -> downloadImage());
    }

    private void downloadImage() {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("default")) {
            Toast.makeText(this, "No image to download", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        saveImage(bitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void saveImage(Bitmap bitmap) {
        String fileName = "MyKhata_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getExternalFilesDir("Downloads"), fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Saved to Downloads", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
}