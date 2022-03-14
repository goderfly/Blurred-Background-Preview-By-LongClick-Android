package com.mirbor.blurpreview.ui.main

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.SystemClock
import android.view.View
import androidx.core.graphics.ColorUtils
import com.mirbor.blurpreview.AndroidUtils
import com.mirbor.blurpreview.AndroidUtils.generateGradient
import com.mirbor.blurpreview.AndroidUtils.getAverageColor
import java.lang.ref.WeakReference

class MotionBackgroundDrawable : Drawable {
    val colors = intArrayOf(
        -0xbd92a9,
        -0x81b75,
        -0x785d7c,
        -0x20936
    )
    private var lastUpdateTime: Long = 0
    private var parentView: WeakReference<View?>? = null
    private val interpolator = CubicBezierInterpolator(0.33, 0.0, 0.0, 1.0)
    private var translationY = 0
    private var isPreview = false
    var posAnimationProgress = 1.0f
    private var phase = 0
    private val rect = RectF()
    var bitmap: Bitmap? = null
        private set
    private var gradientFromBitmap: Bitmap? = null
    private val gradientToBitmap = arrayOfNulls<Bitmap>(ANIMATION_CACHE_BITMAPS_COUNT)
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val paint2 = Paint(Paint.FILTER_BITMAP_FLAG)
    private val paint3 = Paint()
    var intensity = 100
        private set
    private var gradientCanvas: Canvas? = null
    private var gradientFromCanvas: Canvas? = null
    private var postInvalidateParent = false
    private var patternBitmap: Bitmap? = null
    var bitmapShader: BitmapShader? = null
        private set
    private var gradientShader: BitmapShader? = null
    private var matrix: Matrix? = null
    private var fastAnimation = false
    private var legacyCanvas: Canvas? = null
    private var legacyBitmap: Bitmap? = null
    private var legacyCanvas2: Canvas? = null
    private var legacyBitmap2: Bitmap? = null
    private var gradientDrawable: GradientDrawable? = GradientDrawable()
    private var invalidateLegacy = false
    private var rotationBack = false
    private var rotatingPreview = false
    private val updateAnimationRunnable = Runnable { updateAnimation(true) }
    private val patternBounds = Rect()
    private var patternColorFilter: ColorFilter? = null
    private var roundRadius = 0
    private var patternAlpha = 1f
    private var backgroundAlpha = 1f
    private var childAlpha = 255
    private var legacyBitmapColorFilter: ColorFilter? = null
    private var legacyBitmapColor = 0
    private var isIndeterminateAnimation = false
    private var overrideBitmapPaint: Paint? = null

    constructor() : super() {
        init()
    }

    constructor(c1: Int, c2: Int, c3: Int, c4: Int, preview: Boolean) : this(
        c1,
        c2,
        c3,
        c4,
        0,
        preview
    ) {
    }

    constructor(c1: Int, c2: Int, c3: Int, c4: Int, rotation: Int, preview: Boolean) : super() {
        isPreview = preview
        setColors(c1, c2, c3, c4, rotation, false)
        init()
    }

    @SuppressLint("NewApi")
    private fun init() {
        bitmap = Bitmap.createBitmap(60, 80, Bitmap.Config.ARGB_8888)
        for (i in 0 until ANIMATION_CACHE_BITMAPS_COUNT) {
            gradientToBitmap[i] = Bitmap.createBitmap(60, 80, Bitmap.Config.ARGB_8888)
        }
        gradientCanvas = Canvas(bitmap!!)
        gradientFromBitmap = Bitmap.createBitmap(60, 80, Bitmap.Config.ARGB_8888)
        gradientFromCanvas = Canvas(gradientFromBitmap!!)
        generateGradient(
            bitmap,
            true,
            phase,
            interpolator.getInterpolation(posAnimationProgress),
            bitmap!!.getWidth(),
            bitmap!!.getHeight(),
            bitmap!!.getRowBytes(),
            colors
        )
        if (useSoftLight) {
            paint2.blendMode = BlendMode.SOFT_LIGHT
        }
    }

    fun setRoundRadius(rad: Int) {
        roundRadius = rad
        matrix = Matrix()
        bitmapShader = BitmapShader(bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = bitmapShader
        invalidateParent()
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        patternBounds.set(bounds)
    }

    fun setPatternBounds(left: Int, top: Int, right: Int, bottom: Int) {
        patternBounds[left, top, right] = bottom
    }

    val patternColor: Int
        get() = getPatternColor(colors[0], colors[1], colors[2], colors[3])

    fun getPhase(): Int {
        return phase
    }

    fun setPostInvalidateParent(value: Boolean) {
        postInvalidateParent = value
    }

    fun rotatePreview(back: Boolean) {
        if (posAnimationProgress < 1.0f) {
            return
        }
        rotatingPreview = true
        posAnimationProgress = 0.0f
        rotationBack = back
        invalidateParent()
    }

    fun setPhase(value: Int) {
        phase = value
        if (phase < 0) {
            phase = 0
        } else if (phase > 7) {
            phase = 7
        }
        AndroidUtils.generateGradient(
            bitmap,
            true,
            phase,
            interpolator.getInterpolation(posAnimationProgress),
            bitmap!!.width,
            bitmap!!.height,
            bitmap!!.rowBytes,
            colors
        )
    }

    @JvmOverloads
    fun switchToNextPosition(fast: Boolean = false) {
        if (posAnimationProgress < 1.0f) {
            return
        }
        rotatingPreview = false
        rotationBack = false
        fastAnimation = fast
        posAnimationProgress = 0.0f
        phase--
        if (phase < 0) {
            phase = 7
        }
        invalidateParent()
        gradientFromCanvas!!.drawBitmap(bitmap!!, 0f, 0f, null)
        generateNextGradient()
    }

    private fun generateNextGradient() {
        if (useLegacyBitmap && intensity < 0) {
            try {
                if (legacyBitmap != null) {
                    if (legacyBitmap2 == null || legacyBitmap2!!.height != legacyBitmap!!.height || legacyBitmap2!!.width != legacyBitmap!!.width) {
                        if (legacyBitmap2 != null) {
                            legacyBitmap2!!.recycle()
                        }
                        legacyBitmap2 = Bitmap.createBitmap(
                            legacyBitmap!!.width,
                            legacyBitmap!!.height,
                            Bitmap.Config.ARGB_8888
                        )
                        legacyCanvas2 = Canvas(legacyBitmap2!!)
                    } else {
                        legacyBitmap2!!.eraseColor(Color.TRANSPARENT)
                    }
                    legacyCanvas2!!.drawBitmap(legacyBitmap!!, 0f, 0f, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (legacyBitmap2 != null) {
                    legacyBitmap2!!.recycle()
                    legacyBitmap2 = null
                }
            }
            AndroidUtils.generateGradient(
                bitmap,
                true,
                phase,
                1f,
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.rowBytes,
                colors
            )
            invalidateLegacy = true
        }
        for (i in 0 until ANIMATION_CACHE_BITMAPS_COUNT) {
            val p = (i + 1) / ANIMATION_CACHE_BITMAPS_COUNT.toFloat()
            AndroidUtils.generateGradient(
                gradientToBitmap[i],
                true,
                phase,
                p,
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.rowBytes,
                colors
            )
        }
    }

    fun switchToPrevPosition(fast: Boolean) {
        if (posAnimationProgress < 1.0f) {
            return
        }
        rotatingPreview = false
        fastAnimation = fast
        rotationBack = true
        posAnimationProgress = 0.0f
        invalidateParent()
        AndroidUtils.generateGradient(
            gradientFromBitmap,
            true,
            phase,
            0f,
            bitmap!!.width,
            bitmap!!.height,
            bitmap!!.rowBytes,
            colors
        )
        generateNextGradient()
    }

    fun setParentView(view: View?) {
        parentView = WeakReference(view)
    }

    fun setColors(c1: Int, c2: Int, c3: Int, c4: Int) {
        setColors(c1, c2, c3, c4, 0, true)
    }

    fun setColors(c1: Int, c2: Int, c3: Int, c4: Int, bitmap: Bitmap?) {
        colors[0] = c1
        colors[1] = c2
        colors[2] = c3
        colors[3] = c4
        AndroidUtils.generateGradient(
            bitmap,
            true,
            phase,
            interpolator.getInterpolation(posAnimationProgress),
            bitmap!!.width,
            bitmap.height,
            bitmap.rowBytes,
            colors
        )
    }

    fun getGradientOrientation(gradientAngle: Int): GradientDrawable.Orientation? {
        return when (gradientAngle) {
            0 -> GradientDrawable.Orientation.BOTTOM_TOP
            90 -> GradientDrawable.Orientation.LEFT_RIGHT
            135 -> GradientDrawable.Orientation.TL_BR
            180 -> GradientDrawable.Orientation.TOP_BOTTOM
            225 -> GradientDrawable.Orientation.TR_BL
            270 -> GradientDrawable.Orientation.RIGHT_LEFT
            315 -> GradientDrawable.Orientation.BR_TL
            else -> GradientDrawable.Orientation.BL_TR
        }
    }

    fun setColors(c1: Int, c2: Int, c3: Int, c4: Int, rotation: Int, invalidate: Boolean) {
        gradientDrawable = if (isPreview && c3 == 0 && c4 == 0) {
            GradientDrawable(
                getGradientOrientation(rotation),
                intArrayOf(c1, c2)
            )
        } else {
            null
        }
        colors[0] = c1
        colors[1] = c2
        colors[2] = c3
        colors[3] = c4
        if (bitmap != null) {
            AndroidUtils.generateGradient(
                bitmap,
                true,
                phase,
                interpolator.getInterpolation(posAnimationProgress),
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.rowBytes,
                colors
            )
            if (invalidate) {
                invalidateParent()
            }
        }
    }

    private fun invalidateParent() {
        invalidateSelf()
        if (parentView != null && parentView!!.get() != null) {
            parentView!!.get()!!.invalidate()
        }
        if (postInvalidateParent) {
            //NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.invalidateMotionBackground)
            updateAnimation(false)
            //AndroidUtilities.cancelRunOnUIThread(updateAnimationRunnable)
            //AndroidUtilities.runOnUIThread(updateAnimationRunnable, 16)
        }
    }

    fun hasPattern(): Boolean {
        return patternBitmap != null
    }

    override fun getIntrinsicWidth(): Int {
        return if (patternBitmap != null) {
            patternBitmap!!.width
        } else super.getIntrinsicWidth()
    }

    override fun getIntrinsicHeight(): Int {
        return if (patternBitmap != null) {
            patternBitmap!!.height
        } else super.getIntrinsicHeight()
    }

    fun setTranslationY(y: Int) {
        translationY = y
    }

    fun setPatternBitmap(intensity: Int) {
        setPatternBitmap(intensity, patternBitmap)
    }

    @SuppressLint("NewApi")
    fun setPatternBitmap(intensity: Int, bitmap: Bitmap?) {
        this.intensity = intensity
        patternBitmap = bitmap
        invalidateLegacy = true
        if (patternBitmap == null) {
            return
        }
        if (useSoftLight) {
            if (intensity >= 0) {
                paint2.blendMode = BlendMode.SOFT_LIGHT
            } else {
                paint2.blendMode = null
            }
        }
        if (intensity < 0) {
            if (!useLegacyBitmap) {
                bitmapShader =
                    BitmapShader(this.bitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                gradientShader =
                    BitmapShader(patternBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                paint2.shader =
                    ComposeShader(bitmapShader!!, gradientShader!!, PorterDuff.Mode.DST_IN)
                matrix = Matrix()
            } else {
                createLegacyBitmap()
                if (!errorWhileGenerateLegacyBitmap) {
                    paint2.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                } else {
                    paint2.xfermode = null
                }
            }
        } else {
            if (!useLegacyBitmap) {
            } else {
                paint2.xfermode = null
            }
        }
    }

    fun setPatternColorFilter(color: Int) {
        patternColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        invalidateParent()
    }

    fun setPatternAlpha(alpha: Float) {
        patternAlpha = alpha
        invalidateParent()
    }

    fun setBackgroundAlpha(alpha: Float) {
        backgroundAlpha = alpha
        invalidateParent()
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        patternBounds[left, top, right] = bottom
        createLegacyBitmap()
    }

    private fun createLegacyBitmap() {
        if (useLegacyBitmap && intensity < 0 && !errorWhileGenerateLegacyBitmap) {
            val w = (patternBounds.width() * legacyBitmapScale).toInt()
            val h = (patternBounds.height() * legacyBitmapScale).toInt()
            if (w > 0 && h > 0 && (legacyBitmap == null || legacyBitmap!!.width != w || legacyBitmap!!.height != h)) {
                if (legacyBitmap != null) {
                    legacyBitmap!!.recycle()
                }
                if (legacyBitmap2 != null) {
                    legacyBitmap2!!.recycle()
                    legacyBitmap2 = null
                }
                try {
                    legacyBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    legacyCanvas = Canvas(legacyBitmap!!)
                    invalidateLegacy = true
                } catch (e: Exception) {
                    if (legacyBitmap != null) {
                        legacyBitmap!!.recycle()
                        legacyBitmap = null
                    }
                    e.printStackTrace()
                    errorWhileGenerateLegacyBitmap = true
                    paint2.xfermode = null
                }
            }
        }
    }

    fun drawBackground(canvas: Canvas) {
        val bounds = bounds
        canvas.save()
        val tr = if (patternBitmap != null) bounds.top.toFloat() else translationY.toFloat()
        val bitmapWidth = bitmap!!.width
        val bitmapHeight = bitmap!!.height
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        val maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
        val width = bitmapWidth * maxScale
        val height = bitmapHeight * maxScale
        var x = (w - width) / 2
        var y = (h - height) / 2
        if (isPreview) {
            x += bounds.left.toFloat()
            y += bounds.top.toFloat()
            canvas.clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
        if (intensity < 0) {
            canvas.drawColor(
                ColorUtils.setAlphaComponent(
                    Color.BLACK,
                    (childAlpha * backgroundAlpha).toInt()
                )
            )
        } else {
            if (roundRadius != 0) {
                matrix!!.reset()
                matrix!!.setTranslate(x, y)
                val scaleW = bitmap!!.width / bounds.width().toFloat()
                val scaleH = bitmap!!.height / bounds.height().toFloat()
                val scale = 1.0f / Math.min(scaleW, scaleH)
                matrix!!.preScale(scale, scale)
                bitmapShader!!.setLocalMatrix(matrix)
                rect[bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat()] =
                    bounds.bottom.toFloat()
                val wasAlpha = paint.alpha
                paint.alpha = (wasAlpha * backgroundAlpha).toInt()
                canvas.drawRoundRect(rect, roundRadius.toFloat(), roundRadius.toFloat(), paint)
                paint.alpha = wasAlpha
            } else {
                canvas.translate(0f, tr)
                if (gradientDrawable != null) {
                    gradientDrawable!!.setBounds(
                        x.toInt(),
                        y.toInt(),
                        (x + width).toInt(),
                        (y + height).toInt()
                    )
                    gradientDrawable!!.alpha = (255 * backgroundAlpha).toInt()
                    gradientDrawable!!.draw(canvas)
                } else {
                    rect[x, y, x + width] = y + height
                    val bitmapPaint =
                        if (overrideBitmapPaint != null) overrideBitmapPaint!! else paint
                    val wasAlpha = bitmapPaint.alpha
                    bitmapPaint.alpha = (wasAlpha * backgroundAlpha).toInt()
                    canvas.drawBitmap(bitmap!!, null, rect, bitmapPaint)
                    bitmapPaint.alpha = wasAlpha
                }
            }
        }
        canvas.restore()
        updateAnimation(true)
    }

    fun drawPattern(canvas: Canvas) {
        val bounds = bounds
        canvas.save()
        val tr = if (patternBitmap != null) bounds.top.toFloat() else translationY.toFloat()
        var bitmapWidth = bitmap!!.width
        var bitmapHeight = bitmap!!.height
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        var maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
        var width = bitmapWidth * maxScale
        var height = bitmapHeight * maxScale
        var x = (w - width) / 2
        var y = (h - height) / 2
        if (isPreview) {
            x += bounds.left.toFloat()
            y += bounds.top.toFloat()
            canvas.clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
        if (intensity < 0) {
            if (patternBitmap != null) {
                if (useLegacyBitmap) {
                    if (errorWhileGenerateLegacyBitmap) {
                        bitmapWidth = patternBitmap!!.width
                        bitmapHeight = patternBitmap!!.height
                        maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                        width = bitmapWidth * maxScale
                        height = bitmapHeight * maxScale
                        x = (w - width) / 2
                        y = (h - height) / 2
                        rect[x, y, x + width] = y + height
                        var averageColor: Int = AndroidUtils.getAverageColor(
                            colors[2], AndroidUtils.getAverageColor(
                                colors[0], colors[1]
                            )
                        )
                        if (colors[3] != 0) {
                            averageColor = AndroidUtils.getAverageColor(colors[3], averageColor)
                        }
                        if (legacyBitmapColorFilter == null || averageColor != legacyBitmapColor) {
                            legacyBitmapColor = averageColor
                            legacyBitmapColorFilter =
                                PorterDuffColorFilter(averageColor, PorterDuff.Mode.SRC_IN)
                        }
                        paint2.colorFilter = legacyBitmapColorFilter
                        paint2.alpha = (Math.abs(intensity) / 100f * childAlpha * patternAlpha).toInt()
                        canvas.translate(0f, tr)
                        canvas.drawBitmap(patternBitmap!!, null, rect, paint2)
                    } else if (legacyBitmap != null) {
                        if (invalidateLegacy) {
                            rect[0f, 0f, legacyBitmap!!.width.toFloat()] =
                                legacyBitmap!!.height.toFloat()
                            val oldAlpha = paint.alpha
                            paint.alpha = 255
                            legacyCanvas!!.drawBitmap(bitmap!!, null, rect, paint)
                            paint.alpha = oldAlpha
                            bitmapWidth = patternBitmap!!.width
                            bitmapHeight = patternBitmap!!.height
                            maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                            width = bitmapWidth * maxScale
                            height = bitmapHeight * maxScale
                            x = (w - width) / 2
                            y = (h - height) / 2
                            rect[x, y, x + width] = y + height
                            paint2.colorFilter = null
                            paint2.alpha = (Math.abs(intensity) / 100f * 255).toInt()
                            legacyCanvas!!.save()
                            legacyCanvas!!.scale(legacyBitmapScale, legacyBitmapScale)
                            legacyCanvas!!.drawBitmap(patternBitmap!!, null, rect, paint2)
                            legacyCanvas!!.restore()
                            invalidateLegacy = false
                        }
                        rect[bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat()] =
                            bounds.bottom.toFloat()
                        if (legacyBitmap2 != null && posAnimationProgress != 1f) {
                            paint.alpha =
                                (childAlpha * patternAlpha * (1f - posAnimationProgress)).toInt()
                            canvas.drawBitmap(legacyBitmap2!!, null, rect, paint)
                            paint.alpha = (childAlpha * patternAlpha * posAnimationProgress).toInt()
                            canvas.drawBitmap(legacyBitmap!!, null, rect, paint)
                            paint.alpha = childAlpha
                        } else {
                            canvas.drawBitmap(legacyBitmap!!, null, rect, paint)
                        }
                    }
                } else {
                    if (matrix == null) {
                        matrix = Matrix()
                    }
                    matrix!!.reset()
                    matrix!!.setTranslate(x, y + tr)
                    val scaleW = bitmap!!.width / bounds.width().toFloat()
                    val scaleH = bitmap!!.height / bounds.height().toFloat()
                    val scale = 1.0f / Math.min(scaleW, scaleH)
                    matrix!!.preScale(scale, scale)
                    bitmapShader!!.setLocalMatrix(matrix)
                    matrix!!.reset()
                    bitmapWidth = patternBitmap!!.width
                    bitmapHeight = patternBitmap!!.height
                    maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                    width = bitmapWidth * maxScale
                    height = bitmapHeight * maxScale
                    x = (w - width) / 2
                    y = (h - height) / 2
                    matrix!!.setTranslate(x, y + tr)
                    matrix!!.preScale(maxScale, maxScale)
                    gradientShader!!.setLocalMatrix(matrix)
                    paint2.colorFilter = null
                    paint2.alpha = (Math.abs(intensity) / 100f * childAlpha * patternAlpha).toInt()
                    rect[bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat()] =
                        bounds.bottom.toFloat()
                    canvas.drawRoundRect(rect, roundRadius.toFloat(), roundRadius.toFloat(), paint2)
                }
            }
        } else {
            if (patternBitmap != null) {
                bitmapWidth = patternBitmap!!.width
                bitmapHeight = patternBitmap!!.height
                maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                width = bitmapWidth * maxScale
                height = bitmapHeight * maxScale
                x = (w - width) / 2
                y = (h - height) / 2
                rect[x, y, x + width] = y + height
                paint2.colorFilter = patternColorFilter
                paint2.alpha = (Math.abs(intensity) / 100f * childAlpha * patternAlpha).toInt()
                canvas.drawBitmap(patternBitmap!!, null, rect, paint2)
            }
        }
        canvas.restore()
        updateAnimation(true)
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        canvas.save()
        val tr = if (patternBitmap != null) bounds.top.toFloat() else translationY.toFloat()
        var bitmapWidth = bitmap!!.width
        var bitmapHeight = bitmap!!.height
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        var maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
        var width = bitmapWidth * maxScale
        var height = bitmapHeight * maxScale
        var x = (w - width) / 2
        var y = (h - height) / 2
        if (isPreview) {
            x += bounds.left.toFloat()
            y += bounds.top.toFloat()
            canvas.clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
        if (intensity < 0) {
            canvas.drawColor(
                ColorUtils.setAlphaComponent(
                    Color.BLACK,
                    (childAlpha * backgroundAlpha).toInt()
                )
            )
            if (patternBitmap != null) {
                if (useLegacyBitmap) {
                    if (errorWhileGenerateLegacyBitmap) {
                        bitmapWidth = patternBitmap!!.width
                        bitmapHeight = patternBitmap!!.height
                        maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                        width = bitmapWidth * maxScale
                        height = bitmapHeight * maxScale
                        x = (w - width) / 2
                        y = (h - height) / 2
                        rect[x, y, x + width] = y + height
                        var averageColor: Int = AndroidUtils.getAverageColor(
                            colors[2], AndroidUtils.getAverageColor(
                                colors[0], colors[1]
                            )
                        )
                        if (colors[3] != 0) {
                            averageColor = AndroidUtils.getAverageColor(colors[3], averageColor)
                        }
                        if (legacyBitmapColorFilter == null || averageColor != legacyBitmapColor) {
                            legacyBitmapColor = averageColor
                            legacyBitmapColorFilter =
                                PorterDuffColorFilter(averageColor, PorterDuff.Mode.SRC_IN)
                        }
                        paint2.colorFilter = legacyBitmapColorFilter
                        paint2.alpha = (Math.abs(intensity) / 100f * childAlpha * patternAlpha).toInt()
                        canvas.translate(0f, tr)
                        canvas.drawBitmap(patternBitmap!!, null, rect, paint2)
                    } else if (legacyBitmap != null) {
                        if (invalidateLegacy) {
                            rect[0f, 0f, legacyBitmap!!.width.toFloat()] =
                                legacyBitmap!!.height.toFloat()
                            val oldAlpha = paint.alpha
                            paint.alpha = 255
                            legacyCanvas!!.drawBitmap(bitmap!!, null, rect, paint)
                            paint.alpha = oldAlpha
                            bitmapWidth = patternBitmap!!.width
                            bitmapHeight = patternBitmap!!.height
                            maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                            width = bitmapWidth * maxScale
                            height = bitmapHeight * maxScale
                            x = (w - width) / 2
                            y = (h - height) / 2
                            rect[x, y, x + width] = y + height
                            paint2.colorFilter = null
                            paint2.alpha = (Math.abs(intensity) / 100f * 255).toInt()
                            legacyCanvas!!.save()
                            legacyCanvas!!.scale(legacyBitmapScale, legacyBitmapScale)
                            legacyCanvas!!.drawBitmap(patternBitmap!!, null, rect, paint2)
                            legacyCanvas!!.restore()
                            invalidateLegacy = false
                        }
                        rect[bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat()] =
                            bounds.bottom.toFloat()
                        if (legacyBitmap2 != null && posAnimationProgress != 1f) {
                            paint.alpha =
                                (childAlpha * patternAlpha * (1f - posAnimationProgress)).toInt()
                            canvas.drawBitmap(legacyBitmap2!!, null, rect, paint)
                            paint.alpha = (childAlpha * patternAlpha * posAnimationProgress).toInt()
                            canvas.drawBitmap(legacyBitmap!!, null, rect, paint)
                            paint.alpha = childAlpha
                        } else {
                            canvas.drawBitmap(legacyBitmap!!, null, rect, paint)
                        }
                    }
                } else {
                    if (matrix == null) {
                        matrix = Matrix()
                    }
                    matrix!!.reset()
                    matrix!!.setTranslate(x, y + tr)
                    val scaleW = bitmap!!.width / bounds.width().toFloat()
                    val scaleH = bitmap!!.height / bounds.height().toFloat()
                    val scale = 1.0f / Math.min(scaleW, scaleH)
                    matrix!!.preScale(scale, scale)
                    bitmapShader!!.setLocalMatrix(matrix)
                    matrix!!.reset()
                    bitmapWidth = patternBitmap!!.width
                    bitmapHeight = patternBitmap!!.height
                    maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                    width = bitmapWidth * maxScale
                    height = bitmapHeight * maxScale
                    x = (w - width) / 2
                    y = (h - height) / 2
                    matrix!!.setTranslate(x, y + tr)
                    matrix!!.preScale(maxScale, maxScale)
                    gradientShader!!.setLocalMatrix(matrix)
                    paint2.colorFilter = null
                    paint2.alpha = (Math.abs(intensity) / 100f * childAlpha * patternAlpha).toInt()
                    rect[bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat()] =
                        bounds.bottom.toFloat()
                    canvas.drawRoundRect(rect, roundRadius.toFloat(), roundRadius.toFloat(), paint2)
                }
            }
        } else {
            if (roundRadius != 0) {
                matrix!!.reset()
                matrix!!.setTranslate(x, y)
                val scaleW = bitmap!!.width / bounds.width().toFloat()
                val scaleH = bitmap!!.height / bounds.height().toFloat()
                val scale = 1.0f / Math.min(scaleW, scaleH)
                matrix!!.preScale(scale, scale)
                bitmapShader!!.setLocalMatrix(matrix)
                rect[bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat()] =
                    bounds.bottom.toFloat()
                canvas.drawRoundRect(rect, roundRadius.toFloat(), roundRadius.toFloat(), paint)
            } else {
                canvas.translate(0f, tr)
                if (gradientDrawable != null) {
                    gradientDrawable!!.setBounds(
                        x.toInt(),
                        y.toInt(),
                        (x + width).toInt(),
                        (y + height).toInt()
                    )
                    gradientDrawable!!.alpha = (255 * backgroundAlpha).toInt()
                    gradientDrawable!!.draw(canvas)
                } else {
                    rect[x, y, x + width] = y + height
                    val bitmapPaint =
                        if (overrideBitmapPaint != null) overrideBitmapPaint!! else paint
                    val wasAlpha = bitmapPaint.alpha
                    bitmapPaint.alpha = (wasAlpha * backgroundAlpha).toInt()
                    canvas.drawBitmap(bitmap!!, null, rect, bitmapPaint)
                    bitmapPaint.alpha = wasAlpha
                }
            }
            if (patternBitmap != null) {
                bitmapWidth = patternBitmap!!.width
                bitmapHeight = patternBitmap!!.height
                maxScale = Math.max(w / bitmapWidth, h / bitmapHeight)
                width = bitmapWidth * maxScale
                height = bitmapHeight * maxScale
                x = (w - width) / 2
                y = (h - height) / 2
                rect[x, y, x + width] = y + height
                paint2.colorFilter = patternColorFilter
                paint2.alpha = (Math.abs(intensity) / 100f * childAlpha * patternAlpha).toInt()
                canvas.drawBitmap(patternBitmap!!, null, rect, paint2)
            }
        }
        canvas.restore()
        updateAnimation(true)
    }

    fun updateAnimation(invalidate: Boolean) {
        val newTime = SystemClock.elapsedRealtime()
        var dt = newTime - lastUpdateTime
        if (dt > 20) {
            dt = 17
        }
        lastUpdateTime = newTime
        if (dt <= 1) {
            return
        }
        if (isIndeterminateAnimation && posAnimationProgress == 1.0f) {
            posAnimationProgress = 0f
        }
        if (posAnimationProgress < 1.0f) {
            var progress: Float
            var isNeedGenerateGradient = postInvalidateParent || rotatingPreview
            if (isIndeterminateAnimation) {
                posAnimationProgress += dt / 12000f
                if (posAnimationProgress >= 1.0f) {
                    posAnimationProgress = 0.0f
                }
                val progressPerPhase = 1f / 8f
                phase = (posAnimationProgress / progressPerPhase).toInt()
                progress = 1f - (posAnimationProgress - phase * progressPerPhase) / progressPerPhase
                isNeedGenerateGradient = true
            } else {
                if (rotatingPreview) {
                    val stageBefore: Int
                    val progressBefore = interpolator.getInterpolation(posAnimationProgress)
                    stageBefore = if (progressBefore <= 0.25f) {
                        0
                    } else if (progressBefore <= 0.5f) {
                        1
                    } else if (progressBefore <= 0.75f) {
                        2
                    } else {
                        3
                    }
                    posAnimationProgress += dt / if (rotationBack) 1000.0f else 2000.0f
                    if (posAnimationProgress > 1.0f) {
                        posAnimationProgress = 1.0f
                    }
                    progress = interpolator.getInterpolation(posAnimationProgress)
                    if (stageBefore == 0 && progress > 0.25f || stageBefore == 1 && progress > 0.5f || stageBefore == 2 && progress > 0.75f) {
                        if (rotationBack) {
                            phase++
                            if (phase > 7) {
                                phase = 0
                            }
                        } else {
                            phase--
                            if (phase < 0) {
                                phase = 7
                            }
                        }
                    }
                    if (progress <= 0.25f) {
                        progress /= 0.25f
                    } else if (progress <= 0.5f) {
                        progress = (progress - 0.25f) / 0.25f
                    } else if (progress <= 0.75f) {
                        progress = (progress - 0.5f) / 0.25f
                    } else {
                        progress = (progress - 0.75f) / 0.25f
                    }
                    if (rotationBack) {
                        val prevProgress = progress
                        progress = 1.0f - progress
                        if (posAnimationProgress >= 1.0f) {
                            phase++
                            if (phase > 7) {
                                phase = 0
                            }
                            progress = 1.0f
                        }
                    }
                } else {
                    posAnimationProgress += dt / if (fastAnimation) 300.0f else 500.0f
                    if (posAnimationProgress > 1.0f) {
                        posAnimationProgress = 1.0f
                    }
                    progress = interpolator.getInterpolation(posAnimationProgress)
                    if (rotationBack) {
                        progress = 1.0f - progress
                        if (posAnimationProgress >= 1.0f) {
                            phase++
                            if (phase > 7) {
                                phase = 0
                            }
                            progress = 1.0f
                        }
                    }
                }
            }
            if (isNeedGenerateGradient) {
                AndroidUtils.generateGradient(
                    bitmap,
                    true,
                    phase,
                    progress,
                    bitmap!!.width,
                    bitmap!!.height,
                    bitmap!!.rowBytes,
                    colors
                )
                invalidateLegacy = true
            } else {
                if (useLegacyBitmap && intensity < 0) {
                } else {
                    if (progress != 1f) {
                        val part = 1f / ANIMATION_CACHE_BITMAPS_COUNT
                        val i = (progress / part).toInt()
                        if (i == 0) {
                            gradientCanvas!!.drawBitmap(gradientFromBitmap!!, 0f, 0f, null)
                        } else {
                            gradientCanvas!!.drawBitmap(gradientToBitmap[i - 1]!!, 0f, 0f, null)
                        }
                        val alpha = (progress - i * part) / part
                        paint3.alpha = (255 * alpha).toInt()
                        gradientCanvas!!.drawBitmap(gradientToBitmap[i]!!, 0f, 0f, paint3)
                    } else {
                        gradientCanvas!!.drawBitmap(
                            gradientToBitmap[ANIMATION_CACHE_BITMAPS_COUNT - 1]!!,
                            0f,
                            0f,
                            paint3
                        )
                    }
                }
            }
            if (invalidate) {
                invalidateParent()
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        this.childAlpha = alpha
        paint.alpha = alpha
        paint2.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    val isOneColor: Boolean
        get() = colors[0] == colors[1] && colors[0] == colors[2] && colors[0] == colors[3]

    fun setIndeterminateAnimation(isIndeterminateAnimation: Boolean) {
        this.isIndeterminateAnimation = isIndeterminateAnimation
    }

    fun setOverrideBitmapPaint(overrideBitmapPaint: Paint?) {
        this.overrideBitmapPaint = overrideBitmapPaint
    }

    companion object {
        private const val ANIMATION_CACHE_BITMAPS_COUNT = 3
        private val useLegacyBitmap = Build.VERSION.SDK_INT < 28
        private val useSoftLight = Build.VERSION.SDK_INT >= 29
        private var errorWhileGenerateLegacyBitmap = false
        private const val legacyBitmapScale = 0.7f
        fun isDark(color1: Int, color2: Int, color3: Int, color4: Int): Boolean {
            var averageColor = getAverageColor(color1, color2)
            if (color3 != 0) {
                averageColor = getAverageColor(averageColor, color3)
            }
            if (color4 != 0) {
                averageColor = getAverageColor(averageColor, color4)
            }
            val hsb: FloatArray = AndroidUtils.RGBtoHSB(
                Color.red(averageColor),
                Color.green(averageColor),
                Color.blue(averageColor)
            )
            return hsb[2] < 0.3f
        }

        fun getPatternColor(color1: Int, color2: Int, color3: Int, color4: Int): Int {
            return if (isDark(color1, color2, color3, color4)) {
                if (!useSoftLight) 0x7fffffff else -0x1
            } else {
                if (!useSoftLight) {
                    var averageColor: Int = AndroidUtils.getAverageColor(
                        color3,
                        AndroidUtils.getAverageColor(color1, color2)
                    )
                    if (color4 != 0) {
                        averageColor = AndroidUtils.getAverageColor(color4, averageColor)
                    }
                    AndroidUtils.getPatternColor(
                        averageColor,
                        true
                    ) and 0x00ffffff or 0x64000000
                } else {
                    -0x1000000
                }
            }
        }
    }
}