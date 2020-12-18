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
    boolean belongsToHypersphere(
        boolean[] implementation,
        ClassBitmap classBitmap,
        int radius
    );
}
