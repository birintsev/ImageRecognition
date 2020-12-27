package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;

@Service
@AllArgsConstructor
public class ClassBitmapServiceImpl implements ClassBitmapService {

    private final MathService mathService;

    @Override
    public ClassBitmap createFor(
        RecognitionClass recognitionClass,
        RecognitionClass baseClass,
        int margin
    ) {
        double[] geometricalClassCenter =
            mathService.mean(baseClass.getGrayScaleImage());
        double[] topBorder =
            mathService.plus(geometricalClassCenter, margin);
        double[] bottomBorder =
            mathService.plus(geometricalClassCenter, -margin);
        boolean[][] bitmap = bitmap(bottomBorder, topBorder, recognitionClass);
        return new ClassBitmap(
            recognitionClass,
            margin,
            bitmap,
            baseClass
        );
    }

    @Override
    public boolean[] referenceVectorFor(
        ClassBitmap classBitmap
    ) {
        return referenceVectorFor(classBitmap.getBitmap());
    }

    @Override
    public int countDistanceBetweenVectors(
        boolean[] v1,
        boolean[] v2
    ) {
        int distance = 0;
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] != v2[i]) {
                distance++;
            }
        }
        return distance;
    }

    @Override
    public boolean belongsToHypersphere( // <= 1ms
        boolean[] implementation,
        ClassBitmap classBitmap,
        int radius
    ) {
        return countDistanceBetweenVectors(
            referenceVectorFor(classBitmap),
            implementation
        ) <= radius;
    }

    @Override
    public double belongPercent( // >= 70ms
      boolean[][] implementations,
      ClassBitmap classBitmap,
      int radius
    ) {
        int belongsCount = 0;
        for (boolean[] row : implementations) {
            if (
                belongsToHypersphere(
                    row,
                    classBitmap,
                    radius
                )
            ) {
                belongsCount++;
            }
        }
        return ((double) belongsCount) / implementations.length;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private boolean[] referenceVectorFor(
        boolean[][] bitmap
    ) {
        boolean[] referenceVector = new boolean[bitmap[0].length];
        for (int j = 0; j < referenceVector.length; j++) {
            int truesCount = 0;
            for (int i = 0; i < bitmap.length; i++) {
                if (bitmap[i][j]) {
                    truesCount++;
                }
            }
            referenceVector[j] = truesCount > bitmap.length / 2;
        }
        return referenceVector;
    }

    /**
     * @param     bottomBorder             Is an array each element of which
     *                                     represents lower border
     *                                     of an interval for the matrix column.
     *                                     Must be of the same length that
     *                                     passed matrix is
     * @param     topBorder                Is an array each element of which
     *                                     represents higher border
     *                                     of an interval for the matrix column.
     *                                     Must be of the same length
     *                                     that passed matrix is
     * @return                             A bitmap that represents belonging
     *                                     each matrix element to the interval,
     *                                     specified by {@code bottomBorder}
     *                                     and {@code topBorder} arrays.
     *                                     So that each element
     *                                     of a resulting matrix is
     *                                     {@code true} if matrix[i][j]
     *                                     belongs to interval
     *                                     bottomBorder[j]...topBorder[j]
     *                                     (the borders are exclusive),
     *                                     {@code false} otherwise
     * @exception IllegalArgumentException if one or more parameter(s)
     *                                     is/are invalid (does not satisfy
     *                                     the requirements above)
     * */
    private boolean[][] bitmap(
        double[] bottomBorder,
        double[] topBorder,
        RecognitionClass recognitionClass
    ) {
        int[][] image = recognitionClass.getGrayScaleImage();
        if (
            image.length == 0
                && (bottomBorder.length != 0 || topBorder.length != 0)
        ) {
            throw new IllegalArgumentException(
                "The arrays and the matrix must be of the same length"
            );
        }
        if (
            !(image[0].length == bottomBorder.length
                && bottomBorder.length == topBorder.length)
        ) {
            throw new IllegalArgumentException(
                "The arrays and the matrix must be of the same length"
            );
        }
        boolean[][] bitmap = new boolean[image.length][];
        for (int i = 0; i < image.length; i++) {
            bitmap[i] = new boolean[image[i].length];
            for (int j = 0; j < image[i].length; j++) {
                bitmap[i][j] =
                    image[i][j] > bottomBorder[j]
                        && image[i][j] < topBorder[j];
            }
        }
        return bitmap;
    }
}
