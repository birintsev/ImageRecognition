package ua.edu.sumdu.elit.in71.birintsev;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ua.edu.sumdu.elit.in71.birintsev.services.Recognizer;
import ua.edu.sumdu.elit.in71.birintsev.services.RecognizerTrainer;
import static ua.edu.sumdu.elit.in71.birintsev.services.impl.RecognizerTrainerImpl.CALCULATION_LOGGER;

@SpringBootApplication
@AllArgsConstructor
public class Main implements CommandLineRunner {

    private final RecognizerTrainer recognizerTrainer;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        String pathToTestedImage;
        Recognizer recognizer;
        RecognitionClass recognizedClass;
        validate(args);
        pathToTestedImage = parsePathToTestedImage(args);
        try {
            recognizer = recognizerTrainer.train(
                parsePathsToImagesForTraining(args)
            );
            recognizedClass = recognizer.recognize(
                pathToTestedImage
            );
        } catch (FileNotFoundException e) {
            CALCULATION_LOGGER.error(
                "One or more image files does not exist",
                e
            );
            return;
        }
        if (recognizedClass == null) {
            System.out.println(
                "The class represented by ["
                    + pathToTestedImage
                    + "] does not belong to any of passed image classes"
            );
        } else {
            System.out.println(
                "The class represented by ["
                    + pathToTestedImage
                    + "] belongs to the class" +
                    " represented by ["
                    + recognizedClass.getImageFile().getAbsolutePath()
                    + "]"
            );
        }
        System.exit(0);
    }

    private void validate(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                "At least one image file expected"
            );
        }
        for (String pathToFile : args) {
            File imageFile = new File(pathToFile);
            if (!imageFile.isFile()) {
                throw new UncheckedIOException(
                    new FileNotFoundException(
                        "The file " + pathToFile + " not found"
                    )
                );
            }
        }
    }

    private Set<String> parsePathsToImagesForTraining(String[] args) {
        return new HashSet<>(
            Arrays.asList(
                Arrays.copyOf(args, Math.max(args.length - 1, 1))
            )
        );
    }

    private String parsePathToTestedImage(String[] args) {
        return args[args.length - 1];
    }
}
