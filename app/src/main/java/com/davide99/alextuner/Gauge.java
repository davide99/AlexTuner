package com.davide99.alextuner;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

public class Gauge extends View {
    private Paint circlePaint, notePaint, sideNotesPaint, freqPaint, movingGaugePaint, fixedGaugePaint;
    private float gaugeLength;
    private float circleRadius, centerX;
    private float gaugeX, gaugeY;
    private String note, lowerNote, higherNote;
    private float frequency;
    private RectF semicircleBounds;
    private Rect textBounds;
    private int paddingTop;

    private static final int PADDING = 24;
    private static int outOfTuneColor = Color.RED;
    private static int inTuneColor = Color.GREEN;
    private static int gaugeColor = Color.WHITE;
    private static int textColor = Color.WHITE;

    private TunedNoteListener listener = null;

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.Gauge, 0, 0
            );

            gaugeColor = a.getColor(R.styleable.Gauge_gaugeColor, gaugeColor);
            textColor = a.getColor(R.styleable.Gauge_textColor, textColor);
            outOfTuneColor = a.getColor(R.styleable.Gauge_outOfTuneColor, outOfTuneColor);
            inTuneColor = a.getColor(R.styleable.Gauge_inTuneColor, inTuneColor);
            a.recycle();
        }

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(outOfTuneColor);

        notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaint.setColor(textColor);

        sideNotesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sideNotesPaint.setColor(textColor);

        freqPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        freqPaint.setColor(textColor);

        movingGaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        movingGaugePaint.setColor(gaugeColor);
        movingGaugePaint.setStrokeWidth(20);
        movingGaugePaint.setStrokeCap(Paint.Cap.ROUND);

        fixedGaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fixedGaugePaint.setColor(outOfTuneColor);
        fixedGaugePaint.setStrokeWidth(30);
        fixedGaugePaint.setStrokeCap(Paint.Cap.ROUND);

        semicircleBounds = new RectF();
        textBounds = new Rect();

        gaugeX = 0;
        gaugeY = gaugeLength;
        note = "A#";
        lowerNote = "A";
        higherNote = "B#";
        setRawFrequency(0);
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    public Gauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Converts a frequency to a note number (for example: A4 is 69)
     *
     * @param freq frequency to convert
     * @return note number
     */
    private static float frequency_to_number(float freq) {
        return Math.round(12.0f * log2(freq / Consts.A4) + 69.0);
    }

    /**
     * Converts a note number (A4 is 69) back to frequency
     *
     * @param number note number
     * @return frequency
     */
    private static float number_to_frequency(int number) {
        return (float) (Consts.A4 * Math.pow(2, (number - 69) / 12.0));
    }

    /**
     * Converts a note number to the index of the note name in the NOTES_NAMES array
     *
     * @param number number of the note
     * @return index of the note name
     */
    private static int number_to_array_index(int number) {
        return number % Consts.NOTE_NAMES.length;
    }

    private static native float log2(float arg);

    public void setFrequency(float frequency) {
        ValueAnimator frequencyAnimator = ValueAnimator.ofFloat(this.frequency, frequency);
        frequencyAnimator.setDuration(Consts.MILLIS_FPS);
        frequencyAnimator.addUpdateListener((ValueAnimator animation) ->
                setRawFrequency((float) animation.getAnimatedValue())
        );
        frequencyAnimator.start();
    }

    private void setRawFrequency(float frequency) {
        if (Math.abs(this.frequency - frequency) >= 0.1f && !this.isInEditMode()) {
            this.frequency = frequency;

            //Note name
            float note_number = frequency_to_number(frequency);
            int nearest_note_number = Math.round(note_number);
            int index = number_to_array_index(nearest_note_number);
            int octave = (int) log2(frequency / Consts.C0);
            if (index >= 0 && index < Consts.NOTE_NAMES.length) {
                this.note = Consts.NOTE_NAMES[index] + octave;
                this.lowerNote = Consts.NOTE_NAMES[(index + Consts.NOTE_NAMES.length - 1) % Consts.NOTE_NAMES.length];
                this.higherNote = Consts.NOTE_NAMES[(index + 1) % Consts.NOTE_NAMES.length];
            }

            //Gauge angle
            float nearest_note_freq = number_to_frequency(nearest_note_number);
            //calculate frequency difference from freq to nearest note
            float freq_difference = nearest_note_freq - frequency;
            //calculate the frequency difference to the next note (-1)
            float semitone_step = nearest_note_freq - number_to_frequency(Math.round(note_number - 1));
            //calculate the angle of the display needle
            float gaugeAngle = (float) (-Math.PI * freq_difference / semitone_step);
            gaugeX = (float) Math.sin(gaugeAngle) * gaugeLength;
            gaugeY = (float) Math.cos(gaugeAngle) * gaugeLength;

            if (Math.abs(freq_difference) < 0.25) {
                circlePaint.setColor(inTuneColor);
                fixedGaugePaint.setColor(inTuneColor);
                if (listener != null)
                    listener.setTuned(this.note);
            } else {
                circlePaint.setColor(outOfTuneColor);
                fixedGaugePaint.setColor(outOfTuneColor);
            }

            invalidate();
        }
    }

    public void setListener(TunedNoteListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w * 0.5f;
        gaugeLength = w * 0.9f * 0.5f;
        circleRadius = gaugeLength * 0.8f;
        semicircleBounds.set(centerX - circleRadius, -circleRadius, centerX + circleRadius, circleRadius);
        sideNotesPaint.setTextSize((gaugeLength - circleRadius) * 0.8f);
        notePaint.setTextSize(circleRadius / 2.0f);
        freqPaint.setTextSize(circleRadius / 8.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(0, paddingTop);

        //Fixed gauge
        canvas.drawLine(centerX, 0, centerX, gaugeLength, fixedGaugePaint);
        //Moving gauge
        canvas.drawLine(centerX, 0, centerX + gaugeX, gaugeY, movingGaugePaint);
        //Semicircle
        canvas.drawArc(semicircleBounds, 0, 360, false, circlePaint);

        //Lower note
        sideNotesPaint.getTextBounds(lowerNote, 0, lowerNote.length(), textBounds);
        canvas.drawText(lowerNote, centerX - circleRadius + PADDING, textBounds.height() + PADDING, sideNotesPaint);
        //Higher note
        sideNotesPaint.getTextBounds(higherNote, 0, higherNote.length(), textBounds);
        canvas.drawText(higherNote, centerX + circleRadius - textBounds.width() - PADDING, textBounds.height() + PADDING, sideNotesPaint);

        //Note
        notePaint.getTextBounds(note, 0, note.length(), textBounds);
        canvas.drawText(note, centerX - textBounds.exactCenterX(), textBounds.height() + PADDING, notePaint);

        //Frequency
        String freq = String.format(Locale.getDefault(), "%.1f", frequency) + " Hz";
        freqPaint.getTextBounds(freq, 0, freq.length(), textBounds);
        canvas.drawText(freq, centerX - textBounds.exactCenterX(), circleRadius - textBounds.height() - PADDING, freqPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() / 2 + paddingTop);
    }
}
