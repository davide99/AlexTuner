package com.davide99.alextuner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;


public class Notes extends View {
    private String[] notes;
    private boolean[] tuned;
    private int spacing;
    private int note_size;
    private Paint notePaintWrong, notePaintOk, textPaint;
    private Rect textBounds;
    private boolean isVertical;
    private int paddingTop;
    private static int outOfTuneColor = Color.RED;
    private static int inTuneColor = Color.GREEN;
    private static int textColor = Color.WHITE;

    private void init(Context context, AttributeSet attrs) {
        isVertical = false;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.Notes, 0, 0
            );

            //TODO: there's some enum somewhere?
            isVertical = a.getInt(R.styleable.Notes_android_orientation, isVertical ? 1 : 0) == 1;
            outOfTuneColor = a.getColor(R.styleable.Notes_outOfTuneColor, outOfTuneColor);
            inTuneColor = a.getColor(R.styleable.Notes_inTuneColor, inTuneColor);
            textColor = a.getColor(R.styleable.Notes_textColor, textColor);
            a.recycle();
        }

        notes = new String[]{"E#", "A", "D", "G", "B", "E"};
        tuned = new boolean[]{false, false, false, false, false, false};

        notePaintWrong = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaintWrong.setColor(outOfTuneColor);

        notePaintOk = new Paint(Paint.ANTI_ALIAS_FLAG);
        notePaintOk.setColor(inTuneColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);

        textBounds = new Rect();

        super.setSaveEnabled(true);
    }

    public Notes(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    private void compute_spacing_and_note_size(int view_major_size) {
        int total_spacing = Math.round(view_major_size * 0.1f);
        //Spacing between notes
        spacing = Math.round(total_spacing / (notes.length + 1f));
        note_size = Math.round((view_major_size - total_spacing) / (notes.length * 1f));
    }

    private void recompute_everything(int view_major_size) {
        compute_spacing_and_note_size(view_major_size);

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

    public void setNotes(@NonNull String[] notes) {
        this.notes = notes;
        tuned = new boolean[notes.length];
        Arrays.fill(tuned, false);
        if (isVertical) {
            recompute_everything(super.getHeight() - paddingTop);
        } else {
            recompute_everything(super.getWidth());
        }
    }

    public void setTuned(String note) {
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].equals(note)) {
                tuned[i] = true;
                invalidate();
                break;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isVertical) {
            recompute_everything(h - paddingTop);
        } else {
            recompute_everything(w);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isVertical)
            canvas.translate(0, paddingTop);

        float cy = note_size / 2f;

        for (int i = 0; i < notes.length; i++) {
            float cx = (i + 1) * spacing + (2 * i + 1) * note_size / 2f;
            canvas.drawCircle(
                    isVertical ? cy : cx, isVertical ? cx : cy,
                    note_size / 2f, tuned[i] ? notePaintOk : notePaintWrong
            );
            textPaint.getTextBounds(notes[i], 0, notes[i].length(), textBounds);
            canvas.drawText(notes[i],
                    (isVertical ? cy : cx) - textBounds.exactCenterX(),
                    (isVertical ? cx : cy) - textBounds.exactCenterY(),
                    textPaint
            );
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isVertical) {
            compute_spacing_and_note_size(getMeasuredHeight());
            setMeasuredDimension(note_size, getMeasuredHeight() + paddingTop);
        } else {
            compute_spacing_and_note_size(getMeasuredWidth());
            setMeasuredDimension(getMeasuredWidth(), note_size);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.tunedState = tuned;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        tuned = ss.tunedState;
    }


    static class SavedState extends BaseSavedState {
        boolean[] tunedState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            in.readBooleanArray(tunedState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBooleanArray(tunedState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
