package com.rama.myasset;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageOptimizer {

    public static Uri optimize(
            Context context,
            Uri imageUri,
            Bitmap.CompressFormat compressFormat,
            float maxWidth,
            float maxHeight,
            boolean useMaxScale,
            int quality,
            int minWidth,
            int minHeight
    ) {
        BitmapFactory.Options bmOptions = decodeBitmapFromUri(context, imageUri);

        float scaleDownFactor = calculateScaleDownFactor(bmOptions, useMaxScale, maxWidth, maxHeight);

        setNearestInSampleSize(bmOptions, scaleDownFactor);

        Matrix matrix = calculateImageMatrix(context, imageUri, scaleDownFactor, bmOptions);
        if (matrix == null) {
            return null;
        }

        Bitmap newBitmap = generateNewBitmap(context, imageUri, bmOptions, matrix);
        if (newBitmap == null) {
            return null;
        }

        int newBitmapWidth = newBitmap.getWidth();
        int newBitmapHeight = newBitmap.getHeight();

        boolean shouldScaleUp = shouldScaleUp(newBitmapWidth, newBitmapHeight, minWidth, minHeight);

        float scaleUpFactor = calculateScaleUpFactor(newBitmapWidth, newBitmapHeight,
                maxWidth, maxHeight, minWidth, minHeight, shouldScaleUp);

        int finalWidth = finalWidth(newBitmapWidth, scaleUpFactor);
        int finalHeight = finalHeight(newBitmapHeight, scaleUpFactor);

        Bitmap finalBitmap = scaleUpBitmapIfNeeded(newBitmap, finalWidth, finalHeight,
                scaleUpFactor, shouldScaleUp);

        String imageFilePath = compressAndSaveImage(finalBitmap, compressFormat, quality);
        if (imageFilePath == null) {
            return null;
        }

        return Uri.fromFile(new File(imageFilePath));
    }

    private static BitmapFactory.Options decodeBitmapFromUri(Context context, Uri imageUri) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        try (InputStream input = context.getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(input, null, bmOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmOptions;
    }

    private static float calculateScaleDownFactor(BitmapFactory.Options bmOptions,
                                                  boolean useMaxScale,
                                                  float maxWidth,
                                                  float maxHeight) {
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;
        float widthRatio = photoW / maxWidth;
        float heightRatio = photoH / maxHeight;
        float scaleFactor = useMaxScale ? Math.max(widthRatio, heightRatio) : Math.min(widthRatio, heightRatio);

        if (scaleFactor < 1) {
            scaleFactor = 1f;
        }

        return scaleFactor;
    }

    private static void setNearestInSampleSize(BitmapFactory.Options bmOptions, float scaleFactor) {
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;

        if (bmOptions.inSampleSize % 2 != 0) {
            int sample = 1;
            while (sample * 2 < bmOptions.inSampleSize) {
                sample *= 2;
            }
            bmOptions.inSampleSize = sample;
        }
    }

    private static Matrix calculateImageMatrix(Context context, Uri imageUri, float scaleFactor, BitmapFactory.Options bmOptions) {
        try (InputStream input = context.getContentResolver().openInputStream(imageUri)) {
            ExifInterface exif = new ExifInterface(input);
            Matrix matrix = new Matrix();
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270f);
                    break;
            }

            float remainingScaleFactor = scaleFactor / bmOptions.inSampleSize;
            if (remainingScaleFactor > 1) {
                matrix.postScale(1.0f / remainingScaleFactor, 1.0f / remainingScaleFactor);
            }

            return matrix;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Bitmap generateNewBitmap(Context context, Uri imageUri, BitmapFactory.Options bmOptions, Matrix matrix) {
        Bitmap bitmap = null;

        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);

            if (bitmap != null) {
                Bitmap matrixScaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (matrixScaledBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = matrixScaledBitmap;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static boolean shouldScaleUp(int photoW, int photoH, int minWidth, int minHeight) {
        return (minWidth != 0 && minHeight != 0 && (photoW < minWidth || photoH < minHeight));
    }

    private static float calculateScaleUpFactor(float photoW, float photoH,
                                                float maxWidth, float maxHeight,
                                                int minWidth, int minHeight,
                                                boolean shouldScaleUp) {
        float scaleUpFactor = Math.max(photoW / maxWidth, photoH / maxHeight);

        if (shouldScaleUp) {
            if (photoW < minWidth && photoH > minHeight) {
                scaleUpFactor = photoW / minWidth;
            } else if (photoW > minWidth && photoH < minHeight) {
                scaleUpFactor = photoH / minHeight;
            } else {
                scaleUpFactor = Math.max(photoW / minWidth, photoH / minHeight);
            }
        }

        return scaleUpFactor;
    }

    private static int finalWidth(float photoW, float scaleUpFactor) {
        return (int) (photoW / scaleUpFactor);
    }

    private static int finalHeight(float photoH, float scaleUpFactor) {
        return (int) (photoH / scaleUpFactor);
    }

    private static Bitmap scaleUpBitmapIfNeeded(Bitmap bitmap, int finalWidth, int finalHeight, float scaleUpFactor, boolean shouldScaleUp) {
        Bitmap scaledBitmap = (scaleUpFactor > 1 || shouldScaleUp) ?
                Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true) :
                bitmap;

        if (scaledBitmap != bitmap) {
            bitmap.recycle();
        }

        return scaledBitmap;
    }

    private static String compressAndSaveImage(Bitmap bitmap, Bitmap.CompressFormat compressFormat, int quality) {
        String uniqueID = UUID.randomUUID().toString();
        String fileName = "test_optimization_" + uniqueID + ".jpg";

        File fileDir = new File("/storage/emulated/0/Download/");
        File imageFile = new File(fileDir, fileName);

        try (FileOutputStream stream = new FileOutputStream(imageFile)) {
            bitmap.compress(compressFormat, quality, stream);
            stream.close();
            bitmap.recycle();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

