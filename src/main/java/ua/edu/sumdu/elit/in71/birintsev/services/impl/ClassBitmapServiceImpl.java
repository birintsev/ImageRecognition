package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import java.util.Arrays;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;

@Service
public class ClassBitmapServiceImpl implements ClassBitmapService {

    @Override
    public ClassBitmap createFor(
        RecognitionClass recognitionClass,
        RecognitionClass baseClass,
        int margin
    ) {
        double[] geometricalClassCenter = mean(baseClass);
        double[] topBorder = plus(geometricalClassCenter, margin);
        double[] bottomBorder = plus(geometricalClassCenter, -margin);
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

    private double[] mean(RecognitionClass recognitionClass) {
        int[][] matrix = recognitionClass.getGrayScaleImage();
        if (matrix.length == 0) {
            return new double[0];
        }
        double[] mean = new double[matrix[0].length];
        for (int j = 0; j < matrix[0].length; j++) {
            double columnSum = 0;
            for (int i = 0; i < matrix.length; i++) {
                columnSum += matrix[i][j];
            }
            mean[j] = columnSum / matrix.length;
        }
        return mean;
    }

    private double[] plus(double[] array, double number) {
        return Arrays.stream(array).map(e -> e + number).toArray();
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
                bitmap[i][j] = image[i][j] > bottomBorder[j] && image[i][j] < topBorder[j];
            }
        }
        return bitmap;
    }
}
