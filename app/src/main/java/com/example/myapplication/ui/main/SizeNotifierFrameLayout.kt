/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */
package com.example.myapplication.ui.main

import android.animation.Animator

import com.example.myapplication.NativeBlur.blurBitmap
import com.example.myapplication.AndroidUtils.runOnUIThread
import kotlin.jvm.JvmOverloads
import android.widget.FrameLayout
import android.graphics.drawable.Drawable
import android.animation.ValueAnimator
import com.example.myapplication.DispatchQueue
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.*
import android.os.Build
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.BitmapDrawable
import android.view.View
import com.example.myapplication.AndroidUtils
import java.util.ArrayList

class SizeNotifierFrameLayout @JvmOverloads constructor(
    context: Context?
) : FrameLayout(
    context!!
) {
    private val rect = Rect()
    var backgroundImage: Drawable? = null
        private set
    var keyboardHeight = 0
        protected set
    private var bottomClip = 0
    private var delegate: SizeNotifierFrameLayoutDelegate? = null
    private var occupyStatusBar = true
    private var parallaxEffect: WallpaperParallaxEffect? = null
    private var translX = 0f
    private var translY = 0f
    private var bgAngle = 0f
    private var parallaxScale = 1.0f
    private var backgroundTranslationY = 0
    private var paused = true
    private var oldBackgroundDrawable: Drawable? = null
    private var emojiHeight = 0
    private var emojiOffset = 0f
    private var animationInProgress = false
    private var skipBackgroundDrawing = false
    protected var backgroundView: View

    //blur variables
    var needBlur = false
    var needBlurBottom = false
    var blurIsRunning = false
    var blurGeneratingTuskIsRunning = false
    var currentBitmap: BlurBitmap? = null
    var prevBitmap: BlurBitmap? = null
    var unusedBitmaps = ArrayList<BlurBitmap?>(10)

    @JvmField
    var blurBehindViews = ArrayList<View>()
    var matrx = Matrix()
    var matrix2 = Matrix()
    var blurPaintTop = Paint()
    var blurPaintTop2 = Paint()
    var blurPaintBottom = Paint()
    var blurPaintBottom2 = Paint()
    var saturation = 0f
    var blurCrossfadeProgress = 0f
    private val DOWN_SCALE = 12f
    private val TOP_CLIP_OFFSET = (10 + DOWN_SCALE * 2).toInt()
    var blurCrossfade: ValueAnimator? = null
    var invalidateBlur = false
    var count = 0
    var times = 0
    var count2 = 0
    var times2 = 0

    //
    fun invalidateBlur() {
        invalidateBlur = true
        invalidate()
    }

    interface SizeNotifierFrameLayoutDelegate {
        fun onSizeChanged(keyboardHeight: Int, isWidthGreater: Boolean)
    }

    fun setBackgroundImage(bitmap: Drawable, motion: Boolean) {
        if (backgroundImage === bitmap) {
            return
        }
        if (bitmap is MotionBackgroundDrawable) {
            val motionBackgroundDrawable: MotionBackgroundDrawable =
                bitmap as MotionBackgroundDrawable
            motionBackgroundDrawable.setParentView(backgroundView)
        }
        backgroundImage = bitmap
        if (motion) {
            if (parallaxEffect == null) {
                parallaxEffect = WallpaperParallaxEffect(context)
                parallaxEffect!!.setCallback(object : WallpaperParallaxEffect.Callback {
                    override fun onOffsetsChanged(offsetX: Int, offsetY: Int, angle: Float) {
                        translX = offsetX.toFloat()
                        translY = offsetY.toFloat()
                        bgAngle = angle
                        backgroundView.invalidate()
                    }
                })
                if (measuredWidth != 0 && measuredHeight != 0) {
                    parallaxScale = parallaxEffect!!.getScale(measuredWidth, measuredHeight)
                }
            }
            if (!paused) {
                parallaxEffect!!.setEnabled(true)
            }
        } else if (parallaxEffect != null) {
            parallaxEffect!!.setEnabled(false)
            parallaxEffect = null
            parallaxScale = 1.0f
            translX = 0f
            translY = 0f
        }
        backgroundView.invalidate()
    }

    fun setDelegate(delegate: SizeNotifierFrameLayoutDelegate?) {
        this.delegate = delegate
    }

    fun setOccupyStatusBar(value: Boolean) {
        occupyStatusBar = value
    }

    fun onPause() {
        if (parallaxEffect != null) {
            parallaxEffect!!.setEnabled(false)
        }
        paused = true
    }

    fun onResume() {
        if (parallaxEffect != null) {
            parallaxEffect!!.setEnabled(true)
        }
        paused = false
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        notifyHeightChanged()
    }

    fun measureKeyboardHeight(): Int {
        val rootView = rootView
        getWindowVisibleDisplayFrame(rect)
        if (rect.bottom == 0 && rect.top == 0) {
            return 0
        }
        val usableViewHeight: Int =
            rootView.height - (if (rect.top != 0) AndroidUtils.getStatusBarHeight() else 0)// - AndroidUtils.getViewInset(rootView)
        return Math.max(0, usableViewHeight - (rect.bottom - rect.top)).also { keyboardHeight = it }
    }

    fun notifyHeightChanged() {
        if (parallaxEffect != null) {
            parallaxScale = parallaxEffect!!.getScale(measuredWidth, measuredHeight)
        }
        if (delegate != null) {
            keyboardHeight = measureKeyboardHeight()
            val isWidthGreater = false
            post {
                if (delegate != null) {
                    delegate!!.onSizeChanged(keyboardHeight, isWidthGreater)
                }
            }
        }
    }

    fun setBottomClip(value: Int) {
        if (value != bottomClip) {
            bottomClip = value
            backgroundView.invalidate()
        }
    }

    fun setBackgroundTranslation(translation: Int) {
        if (translation != backgroundTranslationY) {
            backgroundTranslationY = translation
            backgroundView.invalidate()
        }
    }

    fun getBackgroundTranslationY(): Int {
        if (backgroundImage is MotionBackgroundDrawable) {
            if (animationInProgress) {
                return emojiOffset.toInt()
            } else if (emojiHeight != 0) {
                return emojiHeight
            }
            return backgroundTranslationY
        }
        return 0
    }

    val backgroundSizeY: Int
        get() {
            var offset = 0
            if (backgroundImage is MotionBackgroundDrawable) {
                val motionBackgroundDrawable: MotionBackgroundDrawable? =
                    backgroundImage as MotionBackgroundDrawable?
                offset = if (!motionBackgroundDrawable.hasPattern()) {
                    if (animationInProgress) {
                        emojiOffset.toInt()
                    } else if (emojiHeight != 0) {
                        emojiHeight
                    } else {
                        backgroundTranslationY
                    }
                } else {
                    if (backgroundTranslationY != 0) 0 else -keyboardHeight
                }
            }
            return measuredHeight - offset
        }
    val heightWithKeyboard: Int
        get() = keyboardHeight + measuredHeight

    fun setEmojiKeyboardHeight(height: Int) {
        if (emojiHeight != height) {
            emojiHeight = height
            backgroundView.invalidate()
        }
    }

    fun setEmojiOffset(animInProgress: Boolean, offset: Float) {
        if (emojiOffset != offset || animationInProgress != animInProgress) {
            emojiOffset = offset
            animationInProgress = animInProgress
            backgroundView.invalidate()
        }
    }

    protected val isActionBarVisible: Boolean
        protected get() = true


    fun setSkipBackgroundDrawing(skipBackgroundDrawing: Boolean) {
        if (this.skipBackgroundDrawing != skipBackgroundDrawing) {
            this.skipBackgroundDrawing = skipBackgroundDrawing
            backgroundView.invalidate()
        }
    }

    protected val newDrawable: Drawable
        protected get() = Theme.getCachedWallpaperNonBlocking()

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === backgroundImage || super.verifyDrawable(who)
    }

    val blurBackgroundTask = BlurBackgroundTask()

    fun startBlur() {
        if (!blurIsRunning || blurGeneratingTuskIsRunning || !invalidateBlur) {
            return
        }
        val blurAlpha = Color.alpha(Theme.getColor(Theme.key_chat_BlurAlpha))
        if (blurAlpha == 255) {
            return
        }
        val lastW = measuredWidth
        val lastH: Int =
            ActionBar.getCurrentActionBarHeight() + AndroidUtils.statusBarHeight + AndroidUtils.dp(
                100
            )
        if (lastW == 0 || lastH == 0) {
            return
        }
        invalidateBlur = false
        blurGeneratingTuskIsRunning = true
        val bitmapH = (lastH / DOWN_SCALE).toInt() + TOP_CLIP_OFFSET
        val bitmapW = (lastW / DOWN_SCALE).toInt()
        val time = System.currentTimeMillis()
        var bitmap: BlurBitmap? = null
        if (unusedBitmaps.size > 0) {
            bitmap = unusedBitmaps.removeAt(unusedBitmaps.size - 1)
        }
        if (bitmap == null) {
            bitmap = BlurBitmap()
            bitmap.topBitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
            bitmap.topCanvas = Canvas(bitmap.topBitmap)
            if (needBlurBottom) {
                bitmap.bottomBitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
                bitmap.bottomCanvas = Canvas(bitmap.bottomBitmap)
            }
        } else {
            bitmap.topBitmap!!.eraseColor(Color.TRANSPARENT)
            if (bitmap.bottomBitmap != null) {
                bitmap.bottomBitmap!!.eraseColor(Color.TRANSPARENT)
            }
        }
        val finalBitmap: BlurBitmap = bitmap
        var sX = finalBitmap.topBitmap!!.width.toFloat() / lastW.toFloat()
        var sY = (finalBitmap.topBitmap!!.height - TOP_CLIP_OFFSET).toFloat() / lastH.toFloat()
        finalBitmap.topCanvas!!.save()
        finalBitmap.pixelFixOffset = scrollOffset % (DOWN_SCALE * 2).toInt()
        finalBitmap.topCanvas!!.clipRect(
            1f,
            10 * sY,
            finalBitmap.topBitmap!!.width.toFloat(),
            (finalBitmap.topBitmap!!.height - 1).toFloat()
        )
        finalBitmap.topCanvas!!.scale(sX, sY)
        finalBitmap.topCanvas!!.translate(0f, 10 * sY + finalBitmap.pixelFixOffset)
        finalBitmap.topScaleX = 1f / sX
        finalBitmap.topScaleY = 1f / sY
        drawList(finalBitmap.topCanvas, true)
        finalBitmap.topCanvas!!.restore()
        if (needBlurBottom) {
            sX = finalBitmap.bottomBitmap!!.width.toFloat() / lastW.toFloat()
            sY = (finalBitmap.bottomBitmap!!.height - TOP_CLIP_OFFSET).toFloat() / lastH.toFloat()
            finalBitmap.needBlurBottom = true
            finalBitmap.bottomOffset = bottomOffset - lastH
            finalBitmap.drawnLisetTranslationY = bottomOffset
            finalBitmap.bottomCanvas!!.save()
            finalBitmap.bottomCanvas!!.clipRect(
                1f,
                10 * sY,
                finalBitmap.bottomBitmap!!.width.toFloat(),
                (finalBitmap.bottomBitmap!!.height - 1).toFloat()
            )
            finalBitmap.bottomCanvas!!.scale(sX, sY)
            finalBitmap.bottomCanvas!!.translate(
                0f,
                10 * sY - finalBitmap.bottomOffset + finalBitmap.pixelFixOffset
            )
            finalBitmap.bottomScaleX = 1f / sX
            finalBitmap.bottomScaleY = 1f / sY
            drawList(finalBitmap.bottomCanvas, false)
            finalBitmap.bottomCanvas!!.restore()
        } else {
            finalBitmap.needBlurBottom = false
        }
        times2 += (System.currentTimeMillis() - time).toInt()
        count2++
        if (count2 >= 20) {
            count2 = 0
            times2 = 0
        }
        if (blurQueue == null) {
            blurQueue = DispatchQueue("BlurQueue")
        }
        blurBackgroundTask.radius = ((Math.max(
            6,
            Math.max(lastH, lastW) / 180
        ) * 2.5f).toInt() * BlurSettingsBottomSheet.blurRadius) as Int
        blurBackgroundTask.finalBitmap = finalBitmap
        blurQueue!!.postRunnable(blurBackgroundTask)
    }

    inner class BlurBackgroundTask : Runnable {
        var radius = 0
        var finalBitmap: BlurBitmap? = null
        override fun run() {
            val time = System.currentTimeMillis()
            blurBitmap(finalBitmap!!.topBitmap!!, radius, true)
            if (finalBitmap!!.needBlurBottom && finalBitmap!!.bottomBitmap != null) {
                blurBitmap(finalBitmap!!.bottomBitmap!!, radius, false)
            }
            times += (System.currentTimeMillis() - time).toInt()
            count++
            if (count > 1000) {
                count = 0
                times = 0
            }
            runOnUIThread {
                if (!blurIsRunning) {
                    if (finalBitmap != null) {
                        finalBitmap!!.recycle()
                    }
                    blurGeneratingTuskIsRunning = false
                    return@runOnUIThread
                }
                prevBitmap = currentBitmap
                val oldBitmap = currentBitmap
                blurPaintTop2.shader = blurPaintTop.shader
                blurPaintBottom2.shader = blurPaintBottom.shader
                var bitmapShader = BitmapShader(
                    finalBitmap!!.topBitmap!!,
                    Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP
                )
                blurPaintTop.shader = bitmapShader
                if (finalBitmap!!.needBlurBottom && finalBitmap!!.bottomBitmap != null) {
                    bitmapShader = BitmapShader(
                        finalBitmap!!.bottomBitmap!!,
                        Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP
                    )
                    blurPaintBottom.shader = bitmapShader
                }
                if (blurCrossfade != null) {
                    blurCrossfade!!.cancel()
                }
                blurCrossfadeProgress = 0f
                blurCrossfade = ValueAnimator.ofFloat(0f, 1f)
                blurCrossfade.addUpdateListener(AnimatorUpdateListener { valueAnimator: ValueAnimator ->
                    blurCrossfadeProgress = valueAnimator.animatedValue as Float
                    invalidateBlurredViews()
                })
                blurCrossfade.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        blurCrossfadeProgress = 1f
                        unusedBitmaps.add(oldBitmap)
                        blurPaintTop2.shader = null
                        blurPaintBottom2.shader = null
                        invalidateBlurredViews()
                        super.onAnimationEnd(animation)
                    }
                })
                blurCrossfade.setDuration(50)
                blurCrossfade.start()
                invalidateBlurredViews()
                currentBitmap = finalBitmap
                runOnUIThread({
                    blurGeneratingTuskIsRunning = false
                    startBlur()
                }, 16)
            }
        }
    }

    fun invalidateBlurredViews() {
        for (i in blurBehindViews.indices) {
            blurBehindViews[i].invalidate()
        }
    }

    protected val bottomOffset: Float
        protected get() = measuredHeight.toFloat()
    protected val listTranslationY: Float
        protected get() = 0f

    protected fun drawList(blurCanvas: Canvas?, top: Boolean) {}
    protected val scrollOffset: Int
        protected get() = 0

    override fun dispatchDraw(canvas: Canvas) {
        if (blurIsRunning) {
            startBlur()
        }
        super.dispatchDraw(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (needBlur && !blurIsRunning) {
            blurIsRunning = true
            invalidateBlur = true
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        blurPaintTop.shader = null
        blurPaintTop2.shader = null
        blurPaintBottom.shader = null
        blurPaintBottom2.shader = null
        if (blurCrossfade != null) {
            blurCrossfade!!.cancel()
        }
        if (currentBitmap != null) {
            currentBitmap!!.recycle()
            currentBitmap = null
        }
        for (i in unusedBitmaps.indices) {
            if (unusedBitmaps[i] != null) {
                unusedBitmaps[i]!!.recycle()
            }
        }
        unusedBitmaps.clear()
        blurIsRunning = false
    }

    fun drawBlur(canvas: Canvas, y: Float, rectTmp: Rect, blurScrimPaint: Paint, top: Boolean) {
        val blurAlpha = Color.alpha(-0x1000000)
        if (currentBitmap == null) {
            canvas.drawRect(rectTmp, blurScrimPaint)
            return
        }
        val blurPaint = if (top) blurPaintTop else blurPaintBottom
        val blurPaint2 = if (top) blurPaintTop2 else blurPaintBottom2
        if (blurPaint.shader != null) {
            matrx.reset()
            matrix2.reset()
            if (!top) {
                var y1 =
                    -y + currentBitmap!!.bottomOffset - currentBitmap!!.pixelFixOffset - TOP_CLIP_OFFSET - (currentBitmap!!.drawnLisetTranslationY - (bottomOffset + listTranslationY))
                matrx.setTranslate(0f, y1)
                matrx.preScale(currentBitmap!!.bottomScaleX, currentBitmap!!.bottomScaleY)
                if (prevBitmap != null) {
                    y1 =
                        -y + prevBitmap!!.bottomOffset - prevBitmap!!.pixelFixOffset - TOP_CLIP_OFFSET - (prevBitmap!!.drawnLisetTranslationY - (bottomOffset + listTranslationY))
                    matrix2.setTranslate(0f, y1)
                    matrix2.preScale(prevBitmap!!.bottomScaleX, prevBitmap!!.bottomScaleY)
                }
            } else {
                matrx.setTranslate(0f, -y - currentBitmap!!.pixelFixOffset - TOP_CLIP_OFFSET)
                matrx.preScale(currentBitmap!!.topScaleX, currentBitmap!!.topScaleY)
                if (prevBitmap != null) {
                    matrix2.setTranslate(0f, -y - prevBitmap!!.pixelFixOffset - TOP_CLIP_OFFSET)
                    matrix2.preScale(prevBitmap!!.topScaleX, prevBitmap!!.topScaleY)
                }
            }
            blurPaint.shader.setLocalMatrix(matrx)
            if (blurPaint2.shader != null) {
                blurPaint2.shader.setLocalMatrix(matrx)
            }
        }
        blurScrimPaint.alpha = 255
        if (blurCrossfadeProgress != 1f && blurPaint2.shader != null) {
            canvas.drawRect(rectTmp, blurScrimPaint)
            canvas.drawRect(rectTmp, blurPaint2)
            canvas.saveLayerAlpha(
                rectTmp.left.toFloat(),
                rectTmp.top.toFloat(),
                rectTmp.right.toFloat(),
                rectTmp.bottom.toFloat(),
                (blurCrossfadeProgress * 255).toInt(),
                Canvas.ALL_SAVE_FLAG
            )
            canvas.drawRect(rectTmp, blurScrimPaint)
            canvas.drawRect(rectTmp, blurPaint)
            canvas.restore()
        } else {
            canvas.drawRect(rectTmp, blurScrimPaint)
            canvas.drawRect(rectTmp, blurPaint)
        }
        blurScrimPaint.alpha = blurAlpha
        canvas.drawRect(rectTmp, blurScrimPaint)
    }

    protected val bottomTranslation: Float
        protected get() = 0f

    class BlurBitmap {
        var needBlurBottom = false
        var pixelFixOffset = 0
        var topCanvas: Canvas? = null
        var topBitmap: Bitmap? = null
        var topScaleX = 0f
        var topScaleY = 0f
        var bottomScaleX = 0f
        var bottomScaleY = 0f
        var bottomOffset = 0f
        var drawnLisetTranslationY = 0f
        var bottomCanvas: Canvas? = null
        var bottomBitmap: Bitmap? = null
        fun recycle() {
            topBitmap!!.recycle()
            if (bottomBitmap != null) {
                bottomBitmap!!.recycle()
            }
        }
    }

    companion object {
        private var blurQueue: DispatchQueue? = null
    }

    init {
        setWillNotDraw(false)


        backgroundView = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                if (backgroundImage == null || skipBackgroundDrawing) {
                    return
                }
                val newDrawable: Drawable? = newDrawable
                if (newDrawable !== backgroundImage && newDrawable != null) {
                    if (Theme.isAnimatingColor()) {
                        oldBackgroundDrawable = backgroundImage
                    }
                    if (newDrawable is MotionBackgroundDrawable) {
                        val motionBackgroundDrawable: MotionBackgroundDrawable =
                            newDrawable as MotionBackgroundDrawable
                        motionBackgroundDrawable.setParentView(backgroundView)
                    }
                    backgroundImage = newDrawable
                }
                val themeAnimationValue = 1.0f
                for (a in 0..1) {
                    val drawable =
                        (if (a == 0) oldBackgroundDrawable else backgroundImage) ?: continue
                    if (a == 1 && oldBackgroundDrawable != null) {
                        drawable.alpha = (255 * themeAnimationValue).toInt()
                    } else {
                        drawable.alpha = 255
                    }
                    if (drawable is MotionBackgroundDrawable) {
                        val motionBackgroundDrawable: MotionBackgroundDrawable =
                            drawable as MotionBackgroundDrawable
                        if (motionBackgroundDrawable.hasPattern()) {
                            val actionBarHeight: Int =
                                (if (isActionBarVisible) AndroidUtils.getStatusBarHeight() else 0) + if (Build.VERSION.SDK_INT >= 21 && occupyStatusBar) AndroidUtils.statusBarHeight else 0
                            val viewHeight = rootView.measuredHeight - actionBarHeight
                            val scaleX = measuredWidth.toFloat() / drawable.intrinsicWidth
                                .toFloat()
                            val scaleY = viewHeight.toFloat() / drawable.intrinsicHeight
                                .toFloat()
                            val scale = Math.max(scaleX, scaleY)
                            val width =
                                Math.ceil((drawable.intrinsicWidth * scale * parallaxScale).toDouble())
                                    .toInt()
                            val height =
                                Math.ceil((drawable.intrinsicHeight * scale * parallaxScale).toDouble())
                                    .toInt()
                            val x = (measuredWidth - width) / 2 + translationX.toInt()
                            val y =
                                backgroundTranslationY + (viewHeight - height) / 2 + actionBarHeight + translationY.toInt()
                            canvas.save()
                            canvas.clipRect(0, actionBarHeight, width, measuredHeight - bottomClip)
                            drawable.setBounds(x, y, x + width, y + height)
                            drawable.draw(canvas)
                            canvas.restore()
                        } else {
                            if (bottomClip != 0) {
                                canvas.save()
                                canvas.clipRect(
                                    0,
                                    0,
                                    measuredWidth,
                                    rootView.measuredHeight - bottomClip
                                )
                            }
                            motionBackgroundDrawable.setTranslationY(backgroundTranslationY)
                            var bottom =
                                (rootView.measuredHeight - backgroundTranslationY + translationY).toInt()
                            if (animationInProgress) {
                                bottom -= emojiOffset.toInt()
                            } else if (emojiHeight != 0) {
                                bottom -= emojiHeight
                            }
                            drawable.setBounds(0, 0, measuredWidth, bottom)
                            drawable.draw(canvas)
                            if (bottomClip != 0) {
                                canvas.restore()
                            }
                        }
                    } else if (drawable is ColorDrawable) {
                        if (bottomClip != 0) {
                            canvas.save()
                            canvas.clipRect(0, 0, measuredWidth, measuredHeight - bottomClip)
                        }
                        drawable.setBounds(0, 0, measuredWidth, rootView.measuredHeight)
                        drawable.draw(canvas)
                        if (bottomClip != 0) {
                            canvas.restore()
                        }
                    } else if (drawable is GradientDrawable) {
                        if (bottomClip != 0) {
                            canvas.save()
                            canvas.clipRect(
                                0,
                                0,
                                measuredWidth,
                                rootView.measuredHeight - bottomClip
                            )
                        }
                        drawable.setBounds(
                            0,
                            backgroundTranslationY,
                            measuredWidth,
                            backgroundTranslationY + rootView.measuredHeight
                        )
                        drawable.draw(canvas)
                        if (bottomClip != 0) {
                            canvas.restore()
                        }
                    } else if (drawable is BitmapDrawable) {
                        if (drawable.tileModeX == Shader.TileMode.REPEAT) {
                            canvas.save()
                            val scale: Float = 2.0f / AndroidUtils.density
                            canvas.scale(scale, scale)
                            drawable.setBounds(
                                0,
                                0,
                                Math.ceil((measuredWidth / scale).toDouble())
                                    .toInt(),
                                Math.ceil((rootView.measuredHeight / scale).toDouble())
                                    .toInt()
                            )
                            drawable.draw(canvas)
                            canvas.restore()
                        } else {
                            val actionBarHeight: Int =
                                (if (isActionBarVisible) AndroidUtils.getStatusBarHeight() else 0) + if (Build.VERSION.SDK_INT >= 21 && occupyStatusBar) AndroidUtils.statusBarHeight else 0
                            val viewHeight = rootView.measuredHeight - actionBarHeight
                            val scaleX = measuredWidth.toFloat() / drawable.getIntrinsicWidth()
                                .toFloat()
                            val scaleY = viewHeight.toFloat() / drawable.getIntrinsicHeight()
                                .toFloat()
                            val scale = Math.max(scaleX, scaleY)
                            val width =
                                Math.ceil((drawable.getIntrinsicWidth() * scale * parallaxScale).toDouble())
                                    .toInt()
                            val height =
                                Math.ceil((drawable.getIntrinsicHeight() * scale * parallaxScale).toDouble())
                                    .toInt()
                            val x = (measuredWidth - width) / 2 + translationX.toInt()
                            val y =
                                backgroundTranslationY + (viewHeight - height) / 2 + actionBarHeight + translationY.toInt()
                            canvas.save()
                            canvas.clipRect(0, actionBarHeight, width, measuredHeight - bottomClip)
                            drawable.setBounds(x, y, x + width, y + height)
                            drawable.draw(canvas)
                            canvas.restore()
                        }
                    }
                    if (a == 0 && oldBackgroundDrawable != null && themeAnimationValue >= 1.0f) {
                        oldBackgroundDrawable = null
                        backgroundView.invalidate()
                    }
                }
            }
        }
        addView(
            backgroundView,
            LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT)
        )
    }
}