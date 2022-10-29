package com.davide99.alextuner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

public class RoundedTextView extends AppCompatTextView {
    private void init(Context context) {
        super.setBackground(ContextCompat.getDrawable(context, R.drawable.circle));
        super.setGravity(Gravity.CENTER);
    }

    public RoundedTextView(Context context) {
        super(context);
        init(context);
    }

    public RoundedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RoundedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int r = Math.max(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(r, r);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.setTextSize(w / 5.0f);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.getBackground().setColorFilter(
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        );
    }
}