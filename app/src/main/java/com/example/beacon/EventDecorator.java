package com.example.beacon;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import android.os.Parcel;
import android.os.Parcelable;

public class EventDecorator implements DayViewDecorator, Parcelable {
    private final int color; // Color of the dot
    private final CalendarDay date; // Date to decorate

    // Constructor
    public EventDecorator(int color, CalendarDay date) {
        this.color = color;
        this.date = date;
    }

    // Parcelable constructor
    protected EventDecorator(Parcel in) {
        color = in.readInt();
        date = in.readParcelable(CalendarDay.class.getClassLoader());
    }

    // Decorate only the specified date
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date.equals(day);
    }

    // Add a dot to the date
    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, color)); // 5 is the radius of the dot
    }

    // Parcelable methods
    @Override
    public int describeContents() {
        return 0; // No special contents
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(color);
        dest.writeParcelable(date, flags);
    }

    // CREATOR field for Parcelable
    public static final Creator<EventDecorator> CREATOR = new Creator<EventDecorator>() {
        @Override
        public EventDecorator createFromParcel(Parcel in) {
            return new EventDecorator(in);
        }

        @Override
        public EventDecorator[] newArray(int size) {
            return new EventDecorator[size];
        }
    };
}