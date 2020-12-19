package ua.edu.sumdu.elit.in71.birintsev.services;

/**
 * A service providing low-level service operations
 * with numbers, vectors and matrices
 * */
public interface MathService {

    /**
     * Adds passed number to each element the array
     *
     * @param  array  a source array
     * @param  number a number to be added
     * @return        a new array each element of which is a sum of
     *                source array corresponding element
     *                and the {@code number}
     * */
    double[] plus(double[] array, double number);

    /**
     * Returns a set of arithmetic means for each {@code matrix} column
     *
     * @param  matrix a source matrix
     * @return        an array of arithmetic means
     *                for each {@code matrix} column
     * */
    double[] mean(int[][] matrix);

    /**
     * Counts the base 2 logarithm of passed number
     *
     * @param number a number for log2 count
     * @return       a base 2 logarithm value of passed {@code number}
     * */
    double log2(double number);
}
