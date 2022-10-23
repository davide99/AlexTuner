package com.davide99.alextuner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

public class Gauge extends View {
    private Paint circlePaint, notePaint, freqPaint, gaugePaint;
    private float gaugeLength;
    private float circleRadius, centerX, centerY;
    private float angle;
    private String note;
    private String frequency;
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private void init(Context context, AttributeSet attrs) {
        int gaugeColor = Color.WHITE;
        int textColor = Color.WHITE;
        int circleColor = Color.RED;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.Gauge,
                    0, 0);

            gaugeColor = a.getColor(R.styleable.Gauge_gaugeColor, gaugeColor);
            textColor = a.getColor(R.styleable.Gauge_textColor, textColor);
            circleColor = a.getColor(R.styleable.Gauge_circleColor, circleColor);
            a.recycle();
        }

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(circleColor);

        notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaint.setColor(textColor);

        freqPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        freqPaint.setColor(textColor);

        gaugePaint = new Paint();
        gaugePaint.setColor(gaugeColor);
        gaugePaint.setStrokeWidth(20);

        angle = 0;
        note = "A#";
        setFrequency(0);
    }

    public Gauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setAngle(float angle) {
        this.angle = angle;
        invalidate();
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setFrequency(float frequency) {
        String new_freq = String.format(Locale.getDefault(), "%.1f", frequency);

        if (!new_freq.equals(this.frequency)) {
            this.frequency = new_freq;

            int index = (int) ((Math.round(12.0 * Math.log(frequency / 440.0) / Math.log(2) + 69.0)) % 12);
            if (index >= 0 && index < NOTE_NAMES.length)
                this.note = NOTE_NAMES[index];

            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int size = Math.min(w, h);
        centerX = getWidth() * 0.5f;
        centerY = getHeight() * 0.5f;
        gaugeLength = size * 0.5f;
        circleRadius = gaugeLength * 0.8f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float gaugeX = (float) Math.sin(angle) * gaugeLength;
        float gaugeY = (float) -Math.cos(angle) * gaugeLength;

        canvas.drawLine(centerX, centerY, centerX + gaugeX, centerY + gaugeY, gaugePaint);
        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);

        notePaint.setTextSize(circleRadius / 2.0f);
        float noteOffsetX = notePaint.measureText(note) * -0.5f;
        float noteOffsetY = notePaint.getFontMetrics().ascent * -0.4f;
        canvas.drawText(note, centerX + noteOffsetX, centerY + noteOffsetY, notePaint);

        String freq = frequency + " Hz";
        freqPaint.setTextSize(circleRadius / 8.0f);
        float freqOffsetX = freqPaint.measureText(freq) * -0.5f;
        float freqOffsetY = freqPaint.getFontMetrics().ascent * -0.4f - notePaint.getFontMetrics().ascent;
        canvas.drawText(freq, centerX + freqOffsetX, centerY + freqOffsetY, freqPaint);
    }
}
