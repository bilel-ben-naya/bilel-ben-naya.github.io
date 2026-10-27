package com.example.planespotter.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Horizontal, high-contrast altitude legend bar.
 *
 * The bar is rendered as 6 step-like segments that match the aircraft marker colors on the map.
 * Altitude thresholds (in meters):
 *   < 2 000, 2 000–4 000, 4 000–6 000, 6 000–8 000, 8 000–10 000, >= 10 000
 */
public class AltitudeLegendBarView extends View {

    // Colors must match MapActivity#getColorForAltitudeMeters()
    private static final int C1 = 0xFF0033CC; // deep blue
    private static final int C2 = 0xFF3D00CC; // indigo
    private static final int C3 = 0xFF6A00FF; // violet
    private static final int C4 = 0xFFC000FF; // fuchsia
    private static final int C5 = 0xFFFF2D55; // rose
    private static final int C6 = 0xFFFF3B30; // red

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float cornerRadiusPx;
    private float strokeWidthPx;
    private LinearGradient shader;

    public AltitudeLegendBarView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AltitudeLegendBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AltitudeLegendBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // dp → px
        cornerRadiusPx = dp(context, 8);
        strokeWidthPx = dp(context, 1);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);
        strokePaint.setColor(0x33000000); // subtle outline for readability on map

        setWillNotDraw(false);
    }

    private static float dp(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0) return;

        // Create a "stepped" gradient by duplicating colors at the segment boundaries.
        // 6 segments => boundaries at i/6.
        final float s = 1f / 6f;

        int[] colors = new int[] {
                C1, C1,
                C2, C2,
                C3, C3,
                C4, C4,
                C5, C5,
                C6, C6
        };
        float[] positions = new float[] {
                0f, 1f * s,
                1f * s, 2f * s,
                2f * s, 3f * s,
                3f * s, 4f * s,
                4f * s, 5f * s,
                5f * s, 1f
        };

        shader = new LinearGradient(
                0f, 0f,
                (float) w, 0f,
                colors,
                positions,
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = getWidth() - getPaddingRight();
        float bottom = getHeight() - getPaddingBottom();
        rect.set(left, top, right, bottom);

        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint);
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint);
    }
}
