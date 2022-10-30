package com.davide99.alextuner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


public class Notes extends View {
    private String[] notes;
    private int spacing;
    private int note_size;
    private Paint notePaint, textPaint;
    private Rect textBounds;

    private void init(Context context, AttributeSet attrs) {
        notes = new String[]{"E#", "A", "D", "G", "B", "E"};

        notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaint.setColor(Color.RED);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);

        textBounds = new Rect();
    }

    public Notes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void compute_spacing_and_note_size(int view_width) {
        int total_spacing = Math.round(view_width * 0.1f);
        //Spacing between notes
        spacing = Math.round(total_spacing / (notes.length + 1f));
        note_size = Math.round((view_width - total_spacing) / (notes.length * 1f));
    }

    private void recompute_everything(int w) {
        compute_spacing_and_note_size(w);

        //We need to recompute the text sizes
        int max_note_length = 0;
        for (String note : notes)
            if (note.length() > max_note_length)
                max_note_length = note.length();

        for (int text_size = 1; textBounds.width() < note_size * 0.8f; text_size++) {
            textPaint.setTextSize(text_size);
            textPaint.getTextBounds("##", 0, 2, textBounds);
        }
    }

    public void setNotes(String[] notes) {
        this.notes = notes;
        recompute_everything(super.getWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        recompute_everything(w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cy = note_size / 2f;

        for (int i = 0; i < notes.length; i++) {
            float cx = (i + 1) * spacing + (2 * i + 1) * note_size / 2f;
            canvas.drawCircle(cx, cy, note_size / 2f, notePaint);
            textPaint.getTextBounds(notes[i], 0, notes[i].length(), textBounds);
            canvas.drawText(notes[i], cx - textBounds.exactCenterX(), cy - textBounds.exactCenterY(), textPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        compute_spacing_and_note_size(getMeasuredWidth());
        setMeasuredDimension(getMeasuredWidth(), note_size);
    }
}
