package ua.edu.sumdu.elit.in71.birintsev.services;

import java.io.FileNotFoundException;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;

public interface Recognizer {

    /**
     * Recognizes to which of the previously determined classes
     * belongs the image specified by the filepath.
     *
     * @param  pathToImage a path to tested image
     * @return             a class to which the image belongs
     *                     or {@code null} if the recognizer
     *                     can not determine the class
     *                     to which the image belongs
     * @throws             FileNotFoundException if the file does not exist
     * @see                RecognizerTrainer
     * */
    RecognitionClass recognize(String pathToImage)
        throws FileNotFoundException;
}
