/*
* Copyright (c) 2021 Abolfazl Abbasi
* Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
* Ported algorithm by Telegram
* */

#include "NativeBlur.h"

extern "C" {
JNIEXPORT jint
Java_com_mirbor_blurpreview_NativeBlur_fastBlurAlpha(JNIEnv *env, jobject clazz, jobject bitmap,
                                                     jint radius) {
    if (radius < 1) {
        return INVALID_RADIUS;
    }

    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return CAN_NOT_GET_BITMAP_INFO;
    } else if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return INVALID_BITMAP_FORMAT;
    }

    int w = info.width;
    int h = info.height;
    int stride = info.stride;

    unsigned char *pixels = nullptr;
    AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels);
    if (!pixels) {
        return BITMAP_CONCURRENCY_ERROR;
    }

    const int wm = w - 1;
    const int hm = h - 1;
    const int wh = w * h;
    const int r1 = radius + 1;
    const int div = radius + r1;
    const int div_sum = SQUARE((div + 1) >> 1);

    int stack[div * 4];
    int vmin[MAX(w, h)];
    int *pRed = new int[wh];
    int *pGreen = new int[wh];
    int *pBlue = new int[wh];
    int *pAlpha = new int[wh];
    int *sir;
    int x, y, rbs, stackpointer, stackstart;
    int routsum, goutsum, boutsum, aoutsum;
    int rinsum, ginsum, binsum, ainsum;
    int rsum, gsum, bsum, asum, p, yp;
    int yw = 0, yi = 0;

    zeroClearInt(stack, div * 4);
    zeroClearInt(vmin, MAX(w, h));
    zeroClearInt(pRed, wh);
    zeroClearInt(pGreen, wh);
    zeroClearInt(pBlue, wh);
    zeroClearInt(pAlpha, wh);

    const size_t dvcount = 256 * div_sum;
    int *dv = new int[dvcount];
    int i;
    for (i = 0; (size_t) i < dvcount; i++) {
        dv[i] = (i / div_sum);
    }

    for (y = 0; y < h; y++) {
        ainsum = aoutsum = asum = rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        for (i = -radius; i <= radius; i++) {
            sir = &stack[(i + radius) * 4];
            int offset = (y * stride + (MIN(wm, MAX(i, 0))) * 4);
            sir[0] = pixels[offset];
            sir[1] = pixels[offset + 1];
            sir[2] = pixels[offset + 2];
            sir[3] = pixels[offset + 3];

            rbs = r1 - abs(i);
            rsum += sir[0] * rbs;
            gsum += sir[1] * rbs;
            bsum += sir[2] * rbs;
            asum += sir[3] * rbs;
            if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                ainsum += sir[3];
            } else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                aoutsum += sir[3];
            }
        }
        stackpointer = radius;

        for (x = 0; x < w; x++) {
            pRed[yi] = dv[rsum];
            pGreen[yi] = dv[gsum];
            pBlue[yi] = dv[bsum];
            pAlpha[yi] = dv[asum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;
            asum -= aoutsum;

            stackstart = stackpointer - radius + div;
            sir = &stack[(stackstart % div) * 4];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];
            aoutsum -= sir[3];

            if (y == 0) {
                vmin[x] = MIN(x + radius + 1, wm);
            }

            int offset = (y * stride + vmin[x] * 4);
            sir[0] = pixels[offset];
            sir[1] = pixels[offset + 1];
            sir[2] = pixels[offset + 2];
            sir[3] = pixels[offset + 3];
            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];
            ainsum += sir[3];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;
            asum += ainsum;

            stackpointer = (stackpointer + 1) % div;
            sir = &stack[(stackpointer % div) * 4];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];
            aoutsum += sir[3];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];
            ainsum -= sir[3];

            yi++;
        }
        yw += w;
    }

    for (x = 0; x < w; x++) {
        ainsum = aoutsum = asum = rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        yp = -radius * w;
        for (i = -radius; i <= radius; i++) {
            yi = MAX(0, yp) + x;

            sir = &stack[(i + radius) * 4];

            sir[0] = pRed[yi];
            sir[1] = pGreen[yi];
            sir[2] = pBlue[yi];
            sir[3] = pAlpha[yi];

            rbs = r1 - abs(i);

            rsum += pRed[yi] * rbs;
            gsum += pGreen[yi] * rbs;
            bsum += pBlue[yi] * rbs;
            asum += pAlpha[yi] * rbs;

            if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                ainsum += sir[3];
            } else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                aoutsum += sir[3];
            }

            if (i < hm) {
                yp += w;
            }
        }
        stackpointer = radius;
        for (y = 0; y < h; y++) {
            int offset = stride * y + x * 4;
            pixels[offset] = dv[rsum];
            pixels[offset + 1] = dv[gsum];
            pixels[offset + 2] = dv[bsum];
            pixels[offset + 3] = dv[asum];
            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;
            asum -= aoutsum;

            stackstart = stackpointer - radius + div;
            sir = &stack[(stackstart % div) * 4];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];
            aoutsum -= sir[3];

            if (x == 0) {
                vmin[y] = (MIN(y + r1, hm)) * w;
            }
            p = x + vmin[y];

            sir[0] = pRed[p];
            sir[1] = pGreen[p];
            sir[2] = pBlue[p];
            sir[3] = pAlpha[p];

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];
            ainsum += sir[3];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;
            asum += ainsum;

            stackpointer = (stackpointer + 1) % div;
            sir = &stack[stackpointer * 4];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];
            aoutsum += sir[3];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];
            ainsum -= sir[3];

            yi += w;
        }
    }

    delete[] pRed;
    delete[] pGreen;
    delete[] pBlue;
    delete[] pAlpha;
    delete[] dv;
    AndroidBitmap_unlockPixels(env, bitmap);

    return SUCCESS;
}
}