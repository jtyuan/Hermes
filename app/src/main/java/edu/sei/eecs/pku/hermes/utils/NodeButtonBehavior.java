package edu.sei.eecs.pku.hermes.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by bilibili on 16/5/4.
 */
public class NodeButtonBehavior extends CoordinatorLayout.Behavior<View> {

    public NodeButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        // we only want to trigger the change
        // only when the changes is from a snackbar
        return dependency instanceof Snackbar.SnackbarLayout;
    }
}
