package com.rexvit.browser.view.customView;

import android.content.Context;
import android.content.res.TypedArray;

import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rexvit.browser.utils.Utils;


public class SwitchRecyclerView extends RecyclerView {
    private int columnWidth;
    Context context;
    private GridLayoutManager manager;

    public SwitchRecyclerView(Context context) {
        super(context);
        this.columnWidth = -1;
        this.context = context;
        init(context, null);
    }

    public SwitchRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.columnWidth = -1;
        init(context, attributeSet);
    }

    public SwitchRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.columnWidth = -1;
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        if (Utils.isTablet(context)) {
            if (attributeSet != null) {
                TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{16843031});
                this.columnWidth = obtainStyledAttributes.getDimensionPixelSize(0, -1);
                obtainStyledAttributes.recycle();
            }
            this.manager = new GridLayoutManager(getContext(), 1);

            setLayoutManager(this.manager);
            return;
        }
        setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
      

    }

    
    @Override 
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.columnWidth > 0) {
            this.manager.setSpanCount(Math.max(1, getMeasuredWidth() / this.columnWidth));
        }
    }
}
