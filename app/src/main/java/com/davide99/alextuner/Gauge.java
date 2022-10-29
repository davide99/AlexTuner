package com.davide99.alextuner;

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
    private float gaugeAngle;
    private String note, lowerNote, higherNote;
    private String frequency;
    private RectF semicircle;
    private Rect textBounds;

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final float A4 = 440.0f;
    private static final float C0 = (float) (A4 * Math.pow(2, -4.75));

    private static final int PADDING = 24;
    private static int circleColorWrong = Color.RED;
    private static int circleColorOk = Color.GREEN;

    private void init(Context context, AttributeSet attrs) {
        int gaugeColor = Color.WHITE;
        int textColor = Color.WHITE;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.Gauge,
                    0, 0);

            gaugeColor = a.getColor(R.styleable.Gauge_gaugeColor, gaugeColor);
            textColor = a.getColor(R.styleable.Gauge_textColor, textColor);
            circleColorWrong = a.getColor(R.styleable.Gauge_circleColorWrong, circleColorWrong);
            circleColorOk = a.getColor(R.styleable.Gauge_circleColorWrong, circleColorOk);
            a.recycle();
        }

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(circleColorWrong);

        notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaint.setColor(textColor);

        sideNotesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sideNotesPaint.setColor(textColor);

        freqPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        freqPaint.setColor(textColor);

        movingGaugePaint = new Paint();
        movingGaugePaint.setColor(gaugeColor);
        movingGaugePaint.setStrokeWidth(20);
        movingGaugePaint.setStrokeCap(Paint.Cap.ROUND);

        fixedGaugePaint = new Paint();
        fixedGaugePaint.setColor(circleColorOk);
        fixedGaugePaint.setStrokeWidth(30);
        fixedGaugePaint.setStrokeCap(Paint.Cap.ROUND);

        textBounds = new Rect();

        gaugeAngle = 0;
        note = "A#";
        lowerNote = "A";
        higherNote = "B#";
        setFrequency(0);
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
        return Math.round(12.0f * log2(freq / A4) + 69.0);
    }

    /**
     * Converts a note number (A4 is 69) back to frequency
     *
     * @param number note number
     * @return frequency
     */
    private static float number_to_frequency(int number) {
        return (float) (A4 * Math.pow(2, (number - 69) / 12.0));
    }

    /**
     * Converts a note number to the index of the note name in the NOTES_NAMES array
     *
     * @param number number of the note
     * @return index of the note name
     */
    private static int number_to_array_index(int number) {
        return number % NOTE_NAMES.length;
    }

    private static native float log2(float arg);

    public void setFrequency(float frequency) {
        String new_freq = String.format(Locale.getDefault(), "%.1f", frequency);

        if (!new_freq.equals(this.frequency) && !this.isInEditMode()) {
            this.frequency = new_freq;

            //Nome nota
            float note_number = frequency_to_number(frequency);
            int nearest_note_number = Math.round(note_number);
            int index = number_to_array_index(nearest_note_number);
            int octave = (int) log2(frequency / C0);
            if (index >= 0 && index < NOTE_NAMES.length) {
                this.note = NOTE_NAMES[index] + octave;
                this.lowerNote = NOTE_NAMES[(index + NOTE_NAMES.length - 1) % NOTE_NAMES.length];
                this.higherNote = NOTE_NAMES[(index + 1) % NOTE_NAMES.length];
            }

            //Angolo ago
            float nearest_note_freq = number_to_frequency(nearest_note_number);
            //calculate frequency difference from freq to nearest note
            float freq_difference = nearest_note_freq - frequency;

            //calculate the frequency difference to the next note (-1)
            float semitone_step = nearest_note_freq - number_to_frequency(Math.round(note_number - 1));

            //calculate the angle of the display needle
            gaugeAngle = (float) (-Math.PI * freq_difference / semitone_step);

            if (Math.abs(freq_difference) < 0.25) {
                circlePaint.setColor(circleColorOk);
            } else {
                circlePaint.setColor(circleColorWrong);
            }

            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w * 0.5f;
        gaugeLength = w * 0.9f * 0.5f;
        circleRadius = gaugeLength * 0.8f;
        semicircle = new RectF(centerX - circleRadius, -circleRadius, centerX + circleRadius, circleRadius);
        sideNotesPaint.setTextSize((gaugeLength - circleRadius) * 0.8f);
        notePaint.setTextSize(circleRadius / 2.0f);
        freqPaint.setTextSize(circleRadius / 8.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float gaugeX = (float) Math.sin(gaugeAngle) * gaugeLength;
        float gaugeY = (float) Math.cos(gaugeAngle) * gaugeLength;

        //Linea verde (mobile)
        canvas.drawLine(centerX, 0, centerX, gaugeLength, fixedGaugePaint);
        //Linea mobile
        canvas.drawLine(centerX, 0, centerX + gaugeX, gaugeY, movingGaugePaint);
        //Semicerchio
        canvas.drawArc(semicircle, 0, 180, false, circlePaint);

        //Nota bassa
        sideNotesPaint.getTextBounds(lowerNote, 0, lowerNote.length(), textBounds);
        canvas.drawText(lowerNote, centerX - gaugeLength - textBounds.exactCenterX(), textBounds.height() + PADDING, sideNotesPaint);
        //Nota alta
        sideNotesPaint.getTextBounds(higherNote, 0, higherNote.length(), textBounds);
        canvas.drawText(higherNote, centerX + gaugeLength - textBounds.exactCenterX(), textBounds.height() + PADDING, sideNotesPaint);

        //Nota principale
        notePaint.getTextBounds(note, 0, note.length(), textBounds);
        canvas.drawText(note, centerX - textBounds.exactCenterX(), textBounds.height() + PADDING, notePaint);

        //Frequenza
        String freq = frequency + " Hz";
        freqPaint.getTextBounds(freq, 0, freq.length(), textBounds);
        canvas.drawText(freq, centerX - textBounds.exactCenterX(), circleRadius - textBounds.height() - PADDING, freqPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() / 2);
    }
}
