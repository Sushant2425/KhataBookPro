package com.sandhyasofttechh.mykhatapro.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sandhyasofttechh.mykhatapro.fragments.ProductsFragment;
import com.sandhyasofttechh.mykhatapro.fragments.ServicesFragment;

public class StockPagerAdapter extends FragmentStateAdapter {

    public StockPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? new ProductsFragment() : new ServicesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
