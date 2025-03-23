package com.example.beacon.ui.explore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.CalendarView;

import java.util.HashSet;
import java.util.Set;

public class CustomCalendarView extends CalendarView {

    private final Paint circlePaint = new Paint();
    private final Set<Long> eventDates = new HashSet<>(); // Set to store dates with events

    public CustomCalendarView(Context context) {
        super(context);
        init();
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint.setColor(Color.RED); // Set circle color
        circlePaint.setAntiAlias(true); // Smooth edges
    }

    // Add a date to the set of dates with events
    public void addEventDate(long dateInMillis) {
        eventDates.add(dateInMillis);
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw circles on dates with events
        for (Long date : eventDates) {
            drawCircleOnDate(canvas, date);
        }
    }

    private void drawCircleOnDate(Canvas canvas, long dateInMillis) {
        // Get the bounds of the date cell
        Rect rect = new Rect();
        getDateBounds(dateInMillis, rect);

        // Draw a circle in the center of the date cell
        int radius = Math.min(rect.width(), rect.height()) / 4; // Circle radius
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
    }

    // Get the bounds of a specific date cell
    private void getDateBounds(long dateInMillis, Rect rect) {
        // This method is a placeholder. You need to implement logic to get the bounds of a date cell.
        // For simplicity, we'll assume the bounds are the same for all dates.
        rect.set(0, 0, getWidth(), getHeight());
    }
}