package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import java.util.Arrays;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;

@Service
public class MathServiceImpl implements MathService {

    /**
     * {@inheritDoc}
     * */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public double[] mean(int[][] matrix) {
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

    /**
     * {@inheritDoc}
     * */
    public double[] plus(double[] array, double number) {
        return Arrays.stream(array).map(e -> e + number).toArray();
    }


    /**
     * {@inheritDoc}
     * */
    public double log2(double number) {
        return Math.log10(number) / Math.log10(2);
    }
}
