package com.sandhyasofttechh.mykhatapro.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScrollAwareFabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    public ScrollAwareFabBehavior() {
        super();
    }

    public ScrollAwareFabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent,
                                   @NonNull FloatingActionButton child,
                                   @NonNull View dependency) {
        return dependency instanceof RecyclerView;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull FloatingActionButton child,
                               @NonNull View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed,
                               int type,
                               @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, type, consumed);

        // Scroll down: dyConsumed > 0, hide FAB
        // Scroll up: dyConsumed < 0, show FAB
        if (dyConsumed > 10 && child.getVisibility() == View.VISIBLE) {
            child.hide();
        } else if (dyConsumed < -10 && child.getVisibility() != View.VISIBLE) {
            child.show();
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                   @NonNull FloatingActionButton child,
                                   @NonNull View target,
                                   int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
    }
}