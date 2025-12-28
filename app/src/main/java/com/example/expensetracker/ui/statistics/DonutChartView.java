// DonutChartView.java
package com.example.expensetracker.ui.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {

    private List<ChartSegment> segments = new ArrayList<>();
    private Paint paint;
    private RectF rectF;
    private float strokeWidth = 40f;

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        rectF = new RectF();
    }

    public void setData(List<ChartSegment> segments) {
        this.segments = segments;
        invalidate(); // Redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (segments.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - (int) strokeWidth;

        rectF.set(
                width / 2f - radius,
                height / 2f - radius,
                width / 2f + radius,
                height / 2f + radius
        );

        float startAngle = -90f; // Start from top

        for (ChartSegment segment : segments) {
            paint.setColor(segment.getColor());
            float sweepAngle = (segment.getPercentage() / 100f) * 360f;
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
            startAngle += sweepAngle;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    public static class ChartSegment {
        private String label;
        private double amount;
        private float percentage;
        private int color;

        public ChartSegment(String label, double amount, float percentage, int color) {
            this.label = label;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public double getAmount() {
            return amount;
        }

        public float getPercentage() {
            return percentage;
        }

        public int getColor() {
            return color;
        }
    }
}