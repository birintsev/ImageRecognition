package ua.edu.sumdu.elit.in71.birintsev.services;

import java.io.FileNotFoundException;
import java.util.Collection;

public interface RecognizerTrainer {

    /**
     * Trains a {@link Recognizer} to recognize images.
     * <p>
     * The 'train' word here means:
     * <ul>
     *     <li> Explain which classes a {@link Recognizer recognizer}
     *          should {@link Recognizer#recognize} select} from
     *     <li> Explain the way to distinguish
     *          {@link ua.edu.sumdu.elit.in71.birintsev.RecognitionClass classes}
     * </ul>
     *
     * @param pathsToImages a collection of images file names
     *                      for a {@link Recognizer recognizer} training
     * @return              an instance of {@link Recognizer}
     *                      that should be able to find an image
     *                      from {@code pathsToImages}
     *                      that a new image looks like
     * */
    Recognizer train(Collection<String> pathsToImages)
        throws FileNotFoundException;
}
