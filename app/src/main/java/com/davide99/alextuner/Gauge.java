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
    private static final float A4 = 440.0f;
    private static final float C0 = (float) (A4 * Math.pow(2, -4.75));

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
            if (index >= 0 && index < NOTE_NAMES.length)
                this.note = NOTE_NAMES[index] + octave;

            //Angolo ago
            float nearest_note_freq = number_to_frequency(nearest_note_number);
            //calculate frequency difference from freq to nearest note
            float freq_difference = nearest_note_freq - frequency;

            //calculate the frequency difference to the next note (-1)
            float semitone_step = nearest_note_freq - number_to_frequency(Math.round(note_number - 1));

            //calculate the angle of the display needle
            angle = (float) (-Math.PI * ((freq_difference / semitone_step) * 2));

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
