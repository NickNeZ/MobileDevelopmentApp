package com.example.dayclock.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.dayclock.data.TaskEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ClockSectorView extends View {

    private final Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minorTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint majorTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hourLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint taskLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF arcRect = new RectF();
    private final List<TaskEntity> tasks = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 30_000);
        }
    };

    private float ringRadius;
    private float ringStrokeWidth;
    private float centerX;
    private float centerY;

    public ClockSectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setColor(Color.parseColor("#FCFCFD"));

        ringBasePaint.setStyle(Paint.Style.STROKE);
        ringBasePaint.setStrokeCap(Paint.Cap.ROUND);
        ringBasePaint.setColor(Color.parseColor("#E4E8EF"));

        sectorPaint.setStyle(Paint.Style.STROKE);
        sectorPaint.setStrokeCap(Paint.Cap.BUTT);

        minorTickPaint.setColor(Color.parseColor("#AEB8C5"));
        minorTickPaint.setStrokeWidth(dp(1.25f));

        majorTickPaint.setColor(Color.parseColor("#687385"));
        majorTickPaint.setStrokeWidth(dp(2f));

        hourLabelPaint.setColor(Color.parseColor("#2D3440"));
        hourLabelPaint.setTextSize(sp(12f));
        hourLabelPaint.setTextAlign(Paint.Align.CENTER);

        taskLabelPaint.setTextSize(sp(11f));
        taskLabelPaint.setTextAlign(Paint.Align.CENTER);

        handPaint.setColor(Color.parseColor("#111827"));
        handPaint.setStrokeWidth(dp(3f));
        handPaint.setStrokeCap(Paint.Cap.ROUND);

        minuteHandPaint.setColor(Color.parseColor("#2563EB"));
        minuteHandPaint.setStrokeWidth(dp(2.2f));
        minuteHandPaint.setStrokeCap(Paint.Cap.ROUND);

        centerPaint.setColor(Color.parseColor("#111827"));
        centerPaint.setStyle(Paint.Style.FILL);
    }

    public void setTasks(List<TaskEntity> taskList) {
        tasks.clear();
        if (taskList != null) {
            tasks.addAll(taskList);
        }
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        centerX = w / 2f;
        centerY = h / 2f;

        Paint.FontMetrics hourMetrics = hourLabelPaint.getFontMetrics();
        float hourTextHalfHeight = (hourMetrics.descent - hourMetrics.ascent) / 2f;
        float labelAndTicksInset = dp(14f) + dp(30f) + hourTextHalfHeight + dp(6f);
        float outerRadius = Math.min(w, h) / 2f - labelAndTicksInset;
        outerRadius = Math.max(outerRadius, dp(36f));
        ringStrokeWidth = Math.min(dp(30f), outerRadius * 0.23f);
        ringRadius = outerRadius - ringStrokeWidth / 2f;

        arcRect.set(
                centerX - ringRadius,
                centerY - ringRadius,
                centerX + ringRadius,
                centerY + ringRadius
        );

        ringBasePaint.setStrokeWidth(ringStrokeWidth);
        sectorPaint.setStrokeWidth(ringStrokeWidth);

        canvas.drawCircle(centerX, centerY, outerRadius + dp(10f), facePaint);
        canvas.drawArc(arcRect, 0f, 360f, false, ringBasePaint);

        drawTasks(canvas);
        drawTicksAndLabels(canvas, outerRadius);
        drawCurrentTimeHands(canvas);
    }

    private void drawTicksAndLabels(Canvas canvas, float outerRadius) {
        for (int hour = 0; hour < 24; hour++) {
            float angle = (float) Math.toRadians(minutesToAngle(hour * 60));
            boolean isMajor = hour % 2 == 0;

            float tickStart = outerRadius + dp(5f);
            float tickEnd = outerRadius + (isMajor ? dp(14f) : dp(10f));

            float sx = centerX + (float) Math.cos(angle) * tickStart;
            float sy = centerY + (float) Math.sin(angle) * tickStart;
            float ex = centerX + (float) Math.cos(angle) * tickEnd;
            float ey = centerY + (float) Math.sin(angle) * tickEnd;

            canvas.drawLine(sx, sy, ex, ey, isMajor ? majorTickPaint : minorTickPaint);

            if (isMajor) {
                float labelRadius = outerRadius + dp(30f);
                float tx = centerX + (float) Math.cos(angle) * labelRadius;
                float ty = centerY + (float) Math.sin(angle) * labelRadius + dp(4f);
                canvas.drawText(String.format(Locale.getDefault(), "%02d", hour), tx, ty, hourLabelPaint);
            }
        }
    }

    private void drawTasks(Canvas canvas) {
        for (TaskEntity task : tasks) {
            int duration = getDuration(task);
            if (duration <= 0) {
                continue;
            }

            float startAngle = minutesToAngle(task.startMinutes);
            float sweep = duration * 360f / 1440f;

            sectorPaint.setColor(task.color);

            if (task.endMinutes >= task.startMinutes) {
                canvas.drawArc(arcRect, startAngle, sweep, false, sectorPaint);
            } else {
                float firstSweep = (1440 - task.startMinutes) * 360f / 1440f;
                canvas.drawArc(arcRect, startAngle, firstSweep, false, sectorPaint);

                float secondSweep = task.endMinutes * 360f / 1440f;
                canvas.drawArc(arcRect, minutesToAngle(0), secondSweep, false, sectorPaint);
            }

            drawTaskLabel(canvas, task, duration);
        }
    }

    private void drawTaskLabel(Canvas canvas, TaskEntity task, int duration) {
        if (duration < 45 || TextUtils.isEmpty(task.title)) {
            return;
        }

        int middleMinute = (task.startMinutes + duration / 2) % 1440;
        float midAngleDeg = minutesToAngle(middleMinute);
        float textRadius = ringRadius;
        float arcLengthPx = (float) (2 * Math.PI * textRadius * (duration / 1440f));
        float maxTextWidth = arcLengthPx - dp(14f);
        if (maxTextWidth <= dp(12f)) {
            return;
        }

        String title = fitLabelToWidth(task.title, maxTextWidth);
        if (TextUtils.isEmpty(title)) {
            return;
        }

        taskLabelPaint.setColor(getReadableTextColor(task.color));
        float direction = isBottomHalf(midAngleDeg) ? -1f : 1f;
        drawCurvedText(canvas, title, midAngleDeg, textRadius, direction);
    }

    private void drawCurrentTimeHands(Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int currentMinutes = hourOfDay * 60 + minute;

        double dayAngleRad = Math.toRadians(minutesToAngle(currentMinutes));
        float dayHandLength = ringRadius - ringStrokeWidth * 0.65f;

        float dayX = centerX + (float) Math.cos(dayAngleRad) * dayHandLength;
        float dayY = centerY + (float) Math.sin(dayAngleRad) * dayHandLength;
        canvas.drawLine(centerX, centerY, dayX, dayY, handPaint);

        float minuteAngleDeg = (minute / 60f) * 360f - 90f;
        double minuteAngleRad = Math.toRadians(minuteAngleDeg);
        float minuteHandLength = ringRadius - ringStrokeWidth * 0.15f;
        float minuteX = centerX + (float) Math.cos(minuteAngleRad) * minuteHandLength;
        float minuteY = centerY + (float) Math.sin(minuteAngleRad) * minuteHandLength;
        canvas.drawLine(centerX, centerY, minuteX, minuteY, minuteHandPaint);

        canvas.drawCircle(centerX, centerY, dp(5f), centerPaint);
    }

    private int getDuration(TaskEntity task) {
        int duration = task.endMinutes - task.startMinutes;
        if (duration < 0) {
            duration += 1440;
        }
        return duration;
    }

    private String trimLabel(String source, int maxChars) {
        if (source.length() <= maxChars) {
            return source;
        }
        if (maxChars <= 3) {
            return source.substring(0, Math.max(1, maxChars));
        }
        return source.substring(0, maxChars - 3) + "...";
    }

    private String fitLabelToWidth(String source, float maxWidth) {
        String candidate = source;
        if (taskLabelPaint.measureText(candidate) <= maxWidth) {
            return candidate;
        }

        int maxChars = source.length();
        while (maxChars > 3) {
            candidate = trimLabel(source, maxChars);
            if (taskLabelPaint.measureText(candidate) <= maxWidth) {
                return candidate;
            }
            maxChars--;
        }
        return "";
    }

    private void drawCurvedText(Canvas canvas, String text, float centerAngleDeg, float radius, float direction) {
        float totalWidth = taskLabelPaint.measureText(text);
        float pxToDeg = 360f / (float) (2 * Math.PI * radius);
        float startOffset = -totalWidth / 2f;

        Paint.FontMetrics metrics = taskLabelPaint.getFontMetrics();
        float baseline = -(metrics.ascent + metrics.descent) / 2f;

        float xOffset = startOffset;
        for (int i = 0; i < text.length(); i++) {
            String ch = text.substring(i, i + 1);
            float charWidth = taskLabelPaint.measureText(ch);
            float charCenterOffset = xOffset + charWidth / 2f;
            float charAngleDeg = centerAngleDeg + direction * charCenterOffset * pxToDeg;

            double angleRad = Math.toRadians(charAngleDeg);
            float x = centerX + (float) Math.cos(angleRad) * radius;
            float y = centerY + (float) Math.sin(angleRad) * radius;

            float tangentRotation = charAngleDeg + (direction > 0 ? 90f : -90f);

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(tangentRotation);
            canvas.drawText(ch, 0f, baseline, taskLabelPaint);
            canvas.restore();

            xOffset += charWidth;
        }
    }

    private boolean isBottomHalf(float angleDeg) {
        float normalized = angleDeg % 360f;
        if (normalized < 0f) {
            normalized += 360f;
        }
        return normalized > 0f && normalized < 180f;
    }

    private int getReadableTextColor(int backgroundColor) {
        int r = Color.red(backgroundColor);
        int g = Color.green(backgroundColor);
        int b = Color.blue(backgroundColor);
        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance > 145 ? Color.parseColor("#1F2937") : Color.WHITE;
    }

    private float minutesToAngle(int minutes) {
        return (minutes / 1440f) * 360f + 90f;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
