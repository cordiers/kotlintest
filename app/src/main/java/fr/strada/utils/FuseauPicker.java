package fr.strada.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.Date;

import fr.strada.R;


public class FuseauPicker extends LinearLayout implements NumberPicker.OnValueChangeListener, OnClickListener {
    private Calendar mCalendar;
    private LayoutInflater mLayoutInflater;
    private NumberPicker mMonthPicker;
    private OnDateChangedListener mOnDateChangedListener;
    private OnDateClickedListener mOnDateClickedListener;
    private NumberPicker mYearPicker;

    public interface OnDateChangedListener {
        void onDateChanged(FuseauPicker datePicker, int i, int i2, int i3);
    }

    public interface OnDateClickedListener {
        void onDateClicked(FuseauPicker datePicker, int i, int i2, int i3);
    }

    public FuseauPicker(Context context) {
        this(context, null);
    }

    public FuseauPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    private void init() {
        this.mLayoutInflater.inflate(R.layout.date_picker_layout, this, true);
        this.mYearPicker = (NumberPicker) findViewById(R.id.year_picker);
        this.mMonthPicker = (NumberPicker) findViewById(R.id.month_picker);

        this.mYearPicker.setOnValueChangeListener(this);
        this.mMonthPicker.setOnValueChangeListener(this);

        if (!getResources().getConfiguration().locale.getCountry().equals("CN") && !getResources().getConfiguration().locale.getCountry().equals("TW")) {
            this.mMonthPicker.setCustomTextArray(getResources().getStringArray(R.array.month_name));
        }
        this.mCalendar = Calendar.getInstance();
        setDate(this.mCalendar.getTime());
    }

    public FuseauPicker setDate(Date date) {
        this.mCalendar.setTime(date);
        this.mYearPicker.setCurrentNumber(this.mCalendar.get(Calendar.YEAR));
        this.mMonthPicker.setCurrentNumber(this.mCalendar.get(Calendar.MONTH) + 1);
        return this;
    }

    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (picker == this.mYearPicker) {
            int dayOfMonth = this.mCalendar.get(Calendar.DAY_OF_MONTH);
            this.mCalendar.set(newVal, this.mCalendar.get(Calendar.MONTH), 1);
            int lastDayOfMonth = this.mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayOfMonth > lastDayOfMonth) {
                dayOfMonth = lastDayOfMonth;
            }
            this.mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        } else if (picker == this.mMonthPicker) {
            int dayOfMonth2 = this.mCalendar.get(Calendar.DAY_OF_MONTH);
            this.mCalendar.set(this.mCalendar.get(Calendar.YEAR), newVal - 1, 1);
            int lastDayOfMonth2 = this.mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayOfMonth2 > lastDayOfMonth2) {
                dayOfMonth2 = lastDayOfMonth2;
            }
            this.mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth2);
        }
        notifyDateChanged();
    }

    public FuseauPicker setOnDateChangedListener(OnDateChangedListener l) {
        this.mOnDateChangedListener = l;
        return this;
    }

    public FuseauPicker setOnDateClickedListener(OnDateClickedListener l) {
        this.mOnDateClickedListener = l;
        return this;
    }

    private void notifyDateChanged() {
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
        if (this.mOnDateClickedListener != null) {
            this.mOnDateClickedListener.onDateClicked(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    public int getYear() {
        return this.mCalendar.get(Calendar.YEAR);
    }

    public int getMonth() {
        return this.mCalendar.get(Calendar.MONTH) + 1;
    }

    public int getDayOfMonth() {
        return this.mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public FuseauPicker setRowNumber(int rowNumber) {
        this.mYearPicker.setRowNumber(rowNumber);
        this.mMonthPicker.setRowNumber(rowNumber);
        return this;
    }

    public FuseauPicker setTextSize(float textSize) {
        this.mYearPicker.setTextSize(textSize);
        this.mMonthPicker.setTextSize(textSize);
        return this;
    }

    public FuseauPicker setFlagTextSize(float textSize) {
        this.mYearPicker.setFlagTextSize(textSize);
        this.mMonthPicker.setFlagTextSize(textSize);
        return this;
    }

    public FuseauPicker setTextColor(int color) {
        this.mYearPicker.setTextColor(color);
        this.mMonthPicker.setTextColor(color);
        return this;
    }

    public FuseauPicker setFlagTextColor(int color) {
        this.mYearPicker.setFlagTextColor(color);
        this.mMonthPicker.setFlagTextColor(color);
        return this;
    }

    public FuseauPicker setBackground(int color) {
        super.setBackgroundColor(color);
        this.mYearPicker.setBackground(color);
        this.mMonthPicker.setBackground(color);
        return this;
    }

    public void onClick(View view) {
        notifyDateChanged();
    }
}
