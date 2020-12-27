package ua.edu.sumdu.elit.in71.birintsev.services;

import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;

/**
 * @see ClassBitmap
 * */
public interface ClassBitmapService {

    /**
     * Creates a {@link ClassBitmap bitmap} for a {@link RecognitionClass class}
     * with specified parameters.
     * <p>
     * This method is an encapsulation
     * of a {@link ClassBitmap} constructor logic.
     *
     * @param recognitionClass a class
     * */
    ClassBitmap createFor(
        RecognitionClass recognitionClass,
        RecognitionClass baseClass,
        int margin
    );

    /**
     * This method encapsulates the logic of calculating
     * a {@link ClassBitmap} reference vector
     *
     * @param classBitmap a target class bitmap
     * */
    boolean[] referenceVectorFor(ClassBitmap classBitmap);

    /**
     * Returns the distance between two vectors (in binary Hamming space)
     * */
    int countDistanceBetweenVectors(boolean[] v1, boolean[] v2);

    /**
     * Checks if passed binary vector (a class implementation)
     * belongs to the hypersphere
     * */
    boolean belongsToHypersphere(  // todo    method signature contains
        boolean[] implementation,  // todo    parameters of different
        ClassBitmap classBitmap,   // todo    abstraction levels
        int radius
    );

    /**
     * Counts a percent of {@code implementations} that belong to a hypersphere
     * with a center equal to reference vector of the {@code classBitmap}
     * and radius = {@code radius}
     *
     * @param implementations a set of tested implementations
     * @param classBitmap     a class bitmap for calculation a reference vector
     *                        (hypersphere center)
     * @param radius          a hypersphere radius
     * @return                a number [0...1] that shows
     *                        how much implementations from passed 2D-array
     *                        belong to the hypersphere
     * */
    double belongPercent(
        boolean[][] implementations, // todo    method signature contains
        ClassBitmap classBitmap,     // todo    parameters of different
        int radius                   // todo    abstraction levels
    );

    /**
     * A method-antonym for {@link #belongPercent}
     * <p>
     * See the description of {@link #belongPercent the correlated method}
     *
     * @param implementations a set of tested implementations
     * @param classBitmap     a class bitmap for calculation a reference vector
     *                        (hypersphere center)
     * @param radius          a hypersphere radius
     * @return                a number [0...1] that shows
     *                        how much implementations from passed 2D-array
     *                        <strong>do not</strong> belong to the hypersphere
     * */
    default double notBelongPercent(
        boolean[][] implementations, // todo    method signature contains
        ClassBitmap classBitmap,     // todo    parameters of different
        int radius                   // todo    abstraction levels
    ) {
        return 1 - belongPercent(implementations, classBitmap, radius);
    }
}
