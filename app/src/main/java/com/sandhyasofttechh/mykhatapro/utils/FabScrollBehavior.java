package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabScrollBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    // This constructor is essential for this to work from XML
    public FabScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        // We are only interested in vertical scrolling.
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;

    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);

        if (dyConsumed > 0) {
            // User scrolled down. We want to slide the button down and out of view.
            int bottomMargin = ((CoordinatorLayout.LayoutParams) child.getLayoutParams()).bottomMargin;
            child.animate().translationY(child.getHeight() + bottomMargin).setDuration(250).start();
        } else if (dyConsumed < 0) {
            // User scrolled up. We want to slide the button back into view.
            child.animate().translationY(0).setDuration(250).start();
        }
    }
}