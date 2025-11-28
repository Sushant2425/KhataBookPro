package com.sandhyasofttechh.mykhatapro.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.sandhyasofttechh.mykhatapro.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class BusinessCardActivity extends AppCompatActivity {

    private static final String TAG = "BusinessCardActivity";
    private ViewPager2 vpTopCards, vpSteps;
    private TextView tvCardIndex;
    private Button btnPrev, btnNext, btnShareCard; // ← ADDED: Share Button
    private TopCardAdapter topCardAdapter;
    private StepsPagerAdapter stepsPagerAdapter;
    private final int TOTAL_CARDS = 10;
    private List<BusinessCardModel> cards = new ArrayList<>();
    private BusinessCardModel tempModel = new BusinessCardModel();
    private int selectedCardIndex = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_card);

        Toolbar tb = findViewById(R.id.toolbar_bc);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeViews();
        initializeCards();
        initializeLocationServices();
        setupViewPagers();
        setupNavigation();
        updateIndex();
    }

    private void initializeViews() {
        vpTopCards = findViewById(R.id.vp_top_cards);
        vpSteps = findViewById(R.id.vp_steps);
        tvCardIndex = findViewById(R.id.tv_card_index);
        btnPrev = findViewById(R.id.btn_prev_step);
        btnNext = findViewById(R.id.btn_next_step);
        btnShareCard = findViewById(R.id.btn_share_card); // ← Initialize Share Button
    }

    private void initializeCards() {
        for (int i = 0; i < TOTAL_CARDS; i++) {
            BusinessCardModel m = new BusinessCardModel();
            m.templateIndex = i + 1;
            cards.add(m);
        }
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupViewPagers() {
        topCardAdapter = new TopCardAdapter(cards);
        vpTopCards.setAdapter(topCardAdapter);
        vpTopCards.setOffscreenPageLimit(3);
        stepsPagerAdapter = new StepsPagerAdapter();
        vpSteps.setAdapter(stepsPagerAdapter);
        vpSteps.setUserInputEnabled(false); // Disable swipe, use buttons only

        vpTopCards.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                selectedCardIndex = pos;
                tempModel = new BusinessCardModel(cards.get(pos));
                stepsPagerAdapter.refreshAllViews();
                updateIndex();
            }
        });

        // ← SHARE BUTTON: Set listener once
        btnShareCard.setOnClickListener(v -> shareCurrentCardAsImage());

        // ← Show Share button ONLY on last page (Review page = index 4)
        vpSteps.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavigationButtons(position);

                if (position == 4) { // Last page
                    btnShareCard.setVisibility(View.VISIBLE);
                } else {
                    btnShareCard.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupNavigation() {
        btnPrev.setOnClickListener(v -> {
            int currentItem = vpSteps.getCurrentItem();
            if (currentItem > 0) {
                vpSteps.setCurrentItem(currentItem - 1, true);
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentItem = vpSteps.getCurrentItem();
            if (currentItem < stepsPagerAdapter.getItemCount() - 1) {
                vpSteps.setCurrentItem(currentItem + 1, true);
            } else {
                saveToSelectedCard();
                Toast.makeText(this, "Business Card Saved Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNavigationButtons(int position) {
        btnPrev.setEnabled(position > 0);
        if (position == stepsPagerAdapter.getItemCount() - 1) {
            btnNext.setText("Save Card");
        } else {
            btnNext.setText("Next");
        }
    }

    private void updateIndex() {
        tvCardIndex.setText("Card " + (selectedCardIndex + 1) + " / " + TOTAL_CARDS);
    }

    private void updateCardLive() {
        cards.set(selectedCardIndex, new BusinessCardModel(tempModel));
        topCardAdapter.notifyItemChanged(selectedCardIndex);
    }

    private void saveToSelectedCard() {
        cards.set(selectedCardIndex, new BusinessCardModel(tempModel));
        topCardAdapter.notifyItemChanged(selectedCardIndex);
    }

    // ← FULL SHARE FUNCTION (100% Working)
    private void shareCurrentCardAsImage() {
        View cardView = null;
        RecyclerView recyclerView = (RecyclerView) vpTopCards.getChildAt(0);
        if (recyclerView != null) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(selectedCardIndex);
            if (holder != null) {
                cardView = holder.itemView;
            }
        }

        if (cardView == null) {
            Toast.makeText(this, "Card is loading, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(cardView.getWidth(), cardView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cardView.draw(canvas);

        try {
            File cacheDir = new File(getCacheDir(), "shared_cards");
            cacheDir.mkdirs();
            File file = new File(cacheDir, "business_card_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

            Uri uri = FileProvider.getUriForFile(this,
                    "com.sandhyasofttechh.mykhatapro.fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here's my professional business card!");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Card"));

        } catch (Exception e) {
            Log.e(TAG, "Share failed", e);
            Toast.makeText(this, "Failed to share card", Toast.LENGTH_SHORT).show();
        }
    }

    private class TopCardAdapter extends RecyclerView.Adapter<TopCardAdapter.CardVH> {
        private final List<BusinessCardModel> list;

        TopCardAdapter(List<BusinessCardModel> l) {
            list = l;
        }

        @NonNull
        @Override
        public CardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_business_card, parent, false);
            return new CardVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CardVH holder, int pos) {
            holder.bind(list.get(pos));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class CardVH extends RecyclerView.ViewHolder {
            View bg;
            TextView tvName, tvOwner, tvType, tvLoc;

            CardVH(@NonNull View v) {
                super(v);
                bg = v.findViewById(R.id.card_bg_container);
                tvName = v.findViewById(R.id.tv_business_name);
                tvOwner = v.findViewById(R.id.tv_owner_name);
                tvType = v.findViewById(R.id.tv_business_type);
                tvLoc = v.findViewById(R.id.tv_business_location);
            }

            void bind(BusinessCardModel m) {
                int tpl = m.templateIndex;
                int bgId = getResources().getIdentifier(
                        "bg_card_template_" + tpl, "drawable", getPackageName());
                if (bgId != 0) {
                    bg.setBackgroundResource(bgId);
                }

                if (m.businessName != null && !m.businessName.isEmpty()) {
                    tvName.setText(m.businessName);
                    tvName.setAlpha(1.0f);
                } else {
                    tvName.setText("Business Name");
                    tvName.setAlpha(0.5f);
                }

                if (m.ownerName != null && !m.ownerName.isEmpty()) {
                    tvOwner.setText(m.ownerName);
                    tvOwner.setAlpha(1.0f);
                } else {
                    tvOwner.setText("Owner Name");
                    tvOwner.setAlpha(0.5f);
                }

                StringBuilder typeBuilder = new StringBuilder();
                if (m.businessType != null && !m.businessType.isEmpty()) {
                    typeBuilder.append(m.businessType);
                }
                if (m.businessCategory != null && !m.businessCategory.isEmpty()) {
                    if (typeBuilder.length() > 0) typeBuilder.append(" · ");
                    typeBuilder.append(m.businessCategory);
                }

                if (typeBuilder.length() > 0) {
                    tvType.setText(typeBuilder.toString());
                    tvType.setVisibility(View.VISIBLE);
                    tvType.setAlpha(1.0f);
                } else {
                    tvType.setText("Business Type");
                    tvType.setAlpha(0.5f);
                }

                StringBuilder locBuilder = new StringBuilder();
                if (m.street != null && !m.street.isEmpty()) {
                    locBuilder.append(m.street);
                }
                if (m.city != null && !m.city.isEmpty()) {
                    if (locBuilder.length() > 0) locBuilder.append(", ");
                    locBuilder.append(m.city);
                }
                if (m.pin != null && !m.pin.isEmpty()) {
                    if (locBuilder.length() > 0) locBuilder.append(" - ");
                    locBuilder.append(m.pin);
                }

                if (locBuilder.length() > 0) {
                    tvLoc.setText(locBuilder.toString());
                    tvLoc.setVisibility(View.VISIBLE);
                    tvLoc.setAlpha(1.0f);
                } else {
                    tvLoc.setText("Business Location");
                    tvLoc.setAlpha(0.5f);
                }
            }
        }
    }

    private class StepsPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int PAGES = 5;
        private final Map<Integer, RecyclerView.ViewHolder> boundHolders = new HashMap<>();

        @Override public int getItemCount() { return PAGES; }
        @Override public int getItemViewType(int pos) { return pos; }

        void refreshAllViews() {
            for (RecyclerView.ViewHolder h : boundHolders.values()) {
                if (h instanceof LocationVH) ((LocationVH) h).populateFromModel();
                else if (h instanceof TypeVH) ((TypeVH) h).populateFromModel();
                else if (h instanceof CategoryVH) ((CategoryVH) h).populateFromModel();
                else if (h instanceof NamesVH) ((NamesVH) h).populateFromModel();
                else if (h instanceof ReviewVH) ((ReviewVH) h).populateFromModel();
            }
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            View v;

            switch (type) {
                case 0:
                    v = inf.inflate(R.layout.fragment_step_location, parent, false);
                    return new LocationVH(v);
                case 1:
                    v = inf.inflate(R.layout.fragment_step_type, parent, false);
                    return new TypeVH(v);
                case 2:
                    v = inf.inflate(R.layout.fragment_step_category, parent, false);
                    return new CategoryVH(v);
                case 3:
                    v = inf.inflate(R.layout.fragment_step_names, parent, false);
                    return new NamesVH(v);
                default:
                    v = inf.inflate(R.layout.fragment_step_review, parent, false);
                    return new ReviewVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
            boundHolders.put(pos, h);
            if (h instanceof LocationVH) ((LocationVH) h).populateFromModel();
            else if (h instanceof TypeVH) ((TypeVH) h).populateFromModel();
            else if (h instanceof CategoryVH) ((CategoryVH) h).populateFromModel();
            else if (h instanceof NamesVH) ((NamesVH) h).populateFromModel();
            else if (h instanceof ReviewVH) ((ReviewVH) h).populateFromModel();
        }

        class LocationVH extends RecyclerView.ViewHolder {
            TextView tvStatus;
            Button btnFetch;
            EditText etStreet, etCity, etPin;
            private boolean isUpdating = false;

            LocationVH(@NonNull View v) {
                super(v);
                tvStatus = v.findViewById(R.id.tv_location_status);
                btnFetch = v.findViewById(R.id.btn_fetch_location);
                etStreet = v.findViewById(R.id.et_street);
                etCity = v.findViewById(R.id.et_city);
                etPin = v.findViewById(R.id.et_pin);

                btnFetch.setOnClickListener(x -> fetchLocation());
                setupTextWatchers();
            }

            private void setupTextWatchers() {
                etStreet.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (!isUpdating) {
                            tempModel.street = s.toString().trim();
                            updateCardLive();
                        }
                    }
                });

                etCity.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (!isUpdating) {
                            tempModel.city = s.toString().trim();
                            updateCardLive();
                        }
                    }
                });

                etPin.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (!isUpdating) {
                            tempModel.pin = s.toString().trim();
                            updateCardLive();
                        }
                    }
                });
            }

            void populateFromModel() {
                isUpdating = true;
                etStreet.setText(tempModel.street);
                etCity.setText(tempModel.city);
                etPin.setText(tempModel.pin);
                isUpdating = false;
            }

            private void fetchLocation() {
                if (ContextCompat.checkSelfPermission(BusinessCardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    Toast.makeText(BusinessCardActivity.this, "Please grant location permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvStatus.setText("Detecting location...");
                btnFetch.setEnabled(false);

                CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

                fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken()
                ).addOnSuccessListener(location -> {
                    btnFetch.setEnabled(true);
                    if (location == null) {
                        tvStatus.setText("Unable to detect location");
                        Toast.makeText(BusinessCardActivity.this, "Please check if GPS is enabled", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.d(TAG, "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                    getAddressFromLocation(location.getLatitude(), location.getLongitude());
                }).addOnFailureListener(e -> {
                    btnFetch.setEnabled(true);
                    tvStatus.setText("Location fetch failed");
                    Toast.makeText(BusinessCardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Location fetch error", e);
                });
            }

            private void getAddressFromLocation(double latitude, double longitude) {
                try {
                    Geocoder geocoder = new Geocoder(BusinessCardActivity.this, Locale.getDefault());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(latitude, longitude, 1, addresses -> runOnUiThread(() -> processAddress(addresses)));
                    } else {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        processAddress(addresses);
                    }
                } catch (IOException e) {
                    tvStatus.setText("Geocoder error");
                    Toast.makeText(BusinessCardActivity.this, "Error getting address: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Geocoder error", e);
                }
            }

            private void processAddress(List<Address> addresses) {
                if (addresses == null || addresses.isEmpty()) {
                    tvStatus.setText("No address found");
                    return;
                }

                Address address = addresses.get(0);
                isUpdating = true;

                String street = address.getThoroughfare();
                if (street == null || street.isEmpty()) street = address.getSubThoroughfare();
                if (street == null || street.isEmpty()) street = address.getAddressLine(0);
                tempModel.street = street != null ? street : "";

                String city = address.getLocality();
                if (city == null || city.isEmpty()) city = address.getSubAdminArea();
                tempModel.city = city != null ? city : "";

                tempModel.pin = address.getPostalCode() != null ? address.getPostalCode() : "";

                etStreet.setText(tempModel.street);
                etCity.setText(tempModel.city);
                etPin.setText(tempModel.pin);
                isUpdating = false;

                tvStatus.setText("Location detected successfully!");
                updateCardLive();
                Toast.makeText(BusinessCardActivity.this, "Address fetched successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Address: " + tempModel.street + ", " + tempModel.city + " - " + tempModel.pin);
            }
        }

        class TypeVH extends RecyclerView.ViewHolder {
            LinearLayout container;
            final String[] types = {"Retail", "Service", "Manufacturing", "Wholesale", "Food & Beverage", "Other"};

            TypeVH(@NonNull View v) {
                super(v);
                container = v.findViewById(R.id.type_container);
                setupButtons();
            }

            @SuppressLint("ResourceType")
            void setupButtons() {
                container.removeAllViews();
                for (String type : types) {
                    Button btn = new Button(BusinessCardActivity.this);
                    btn.setText(type);
                    btn.setTextSize(16);
                    btn.setPadding(32, 32, 32, 32);
                    btn.setBackgroundResource(R.drawable.button_selector);
                    btn.setTextColor(getResources().getColorStateList(R.drawable.button_text_color));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 16);
                    btn.setLayoutParams(params);

                    btn.setOnClickListener(v -> {
                        tempModel.businessType = type;
                        updateCardLive();
                        populateFromModel();
                    });

                    container.addView(btn);
                }
            }

            void populateFromModel() {
                for (int i = 0; i < container.getChildCount(); i++) {
                    Button btn = (Button) container.getChildAt(i);
                    btn.setSelected(btn.getText().toString().equals(tempModel.businessType));
                }
            }
        }

        class CategoryVH extends RecyclerView.ViewHolder {
            LinearLayout container;
            final String[] categories = {"Groceries", "Electronics", "Clothing", "Automotive", "Pharmacy", "Restaurant", "Other"};

            CategoryVH(@NonNull View v) {
                super(v);
                container = v.findViewById(R.id.category_container);
                setupButtons();
            }

            void setupButtons() {
                container.removeAllViews();
                for (String category : categories) {
                    Button btn = new Button(BusinessCardActivity.this);
                    btn.setText(category);
                    btn.setTextSize(16);
                    btn.setPadding(32, 32, 32, 32);
                    btn.setBackgroundResource(R.drawable.button_selector);
                    btn.setTextColor(getResources().getColorStateList(R.drawable.button_text_color));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 16);
                    btn.setLayoutParams(params);

                    btn.setOnClickListener(v -> {
                        tempModel.businessCategory = category;
                        updateCardLive();
                        populateFromModel();
                    });

                    container.addView(btn);
                }
            }

            void populateFromModel() {
                for (int i = 0; i < container.getChildCount(); i++) {
                    Button btn = (Button) container.getChildAt(i);
                    btn.setSelected(btn.getText().toString().equals(tempModel.businessCategory));
                }
            }
        }

        class NamesVH extends RecyclerView.ViewHolder {
            EditText etBusiness, etOwner;
            private boolean isUpdating = false;

            NamesVH(@NonNull View v) {
                super(v);
                etBusiness = v.findViewById(R.id.et_business_name);
                etOwner = v.findViewById(R.id.et_owner_name);
                setupTextWatchers();
            }

            private void setupTextWatchers() {
                etBusiness.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (!isUpdating) {
                            tempModel.businessName = s.toString().trim();
                            updateCardLive();
                        }
                    }
                });

                etOwner.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (!isUpdating) {
                            tempModel.ownerName = s.toString().trim();
                            updateCardLive();
                        }
                    }
                });
            }

            void populateFromModel() {
                isUpdating = true;
                etBusiness.setText(tempModel.businessName);
                etOwner.setText(tempModel.ownerName);
                isUpdating = false;
            }
        }

        class ReviewVH extends RecyclerView.ViewHolder {
            TextView tvReview;

            ReviewVH(@NonNull View v) {
                super(v);
                tvReview = v.findViewById(R.id.tv_review);
            }

            void populateFromModel() {
                BusinessCardModel m = tempModel;

                StringBuilder review = new StringBuilder();
                review.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");
                review.append("BUSINESS CARD PREVIEW\n");
                review.append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                review.append("LOCATION\n");
                if (!m.street.isEmpty() || !m.city.isEmpty() || !m.pin.isEmpty()) {
                    if (!m.street.isEmpty()) review.append("   ").append(m.street).append("\n");
                    if (!m.city.isEmpty()) review.append("   ").append(m.city);
                    if (!m.pin.isEmpty()) review.append(" - ").append(m.pin);
                    review.append("\n\n");
                } else {
                    review.append("   Not specified\n\n");
                }

                review.append("BUSINESS DETAILS\n");
                review.append("   Name: ").append(m.businessName.isEmpty() ? "Not specified" : m.businessName).append("\n");
                review.append("   Type: ").append(m.businessType.isEmpty() ? "Not specified" : m.businessType).append("\n");
                review.append("   Category: ").append(m.businessCategory.isEmpty() ? "Not specified" : m.businessCategory).append("\n\n");

                review.append("OWNER\n");
                review.append("   ").append(m.ownerName.isEmpty() ? "Not specified" : m.ownerName).append("\n\n");

                review.append("TEMPLATE\n");
                review.append("   Template #").append(m.templateIndex).append("\n\n");

                review.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");

                tvReview.setText(review.toString());
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class BusinessCardModel {
        public String street = "";
        public String city = "";
        public String pin = "";
        public String businessType = "";
        public String businessCategory = "";
        public String businessName = "";
        public String ownerName = "";
        public int templateIndex = 1;

        public BusinessCardModel() {}

        public BusinessCardModel(BusinessCardModel other) {
            this.street = other.street;
            this.city = other.city;
            this.pin = other.pin;
            this.businessType = other.businessType;
            this.businessCategory = other.businessCategory;
            this.businessName = other.businessName;
            this.ownerName = other.ownerName;
            this.templateIndex = other.templateIndex;
        }
    }
}