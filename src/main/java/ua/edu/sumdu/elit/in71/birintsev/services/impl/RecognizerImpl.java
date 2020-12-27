package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.Recognizer;
import static ua.edu.sumdu.elit.in71.birintsev.services.impl.RecognizerTrainerImpl.CALCULATION_LOGGER;

@Service
@AllArgsConstructor
class RecognizerImpl implements Recognizer {

    private final Set<CriteriaValue> criteriaValues;

    private final ClassBitmapService classBitmapService;

    @Override
    public RecognitionClass recognize(String pathToImage)
    throws FileNotFoundException {
        ClassBitmap testedClassBitmap;
        try {
            testedClassBitmap = classBitmapService.createFor(
                new RecognitionClass(pathToImage),
                baseClass(criteriaValues),
                margin(criteriaValues)
            );
        } catch (NoSuchElementException e) {
            CALCULATION_LOGGER.trace(e.getMessage());
            return null;
        }
        return criteriaValues
            .stream()
            .collect(
                Collectors.toMap(
                    cv -> cv,
                    criteriaValue -> {
                        double membershipFunction = countMembershipFunction(
                            testedClassBitmap,
                            criteriaValue
                        );
                        CALCULATION_LOGGER.info(
                            "Membership function ("
                                + testedClassBitmap
                                    .getRecognitionClass()
                                    .getImageFile()
                                    .getName()
                                + " to "
                                + criteriaValue
                                    .getNeighbourClasses()
                                    .getClassBitmap()
                                    .getRecognitionClass()
                                    .getImageFile()
                                    .getName()
                                + ") = "
                                + membershipFunction
                        );
                        return membershipFunction;
                    }
                )
            )
            .entrySet()
            .stream()
            .filter(
                e -> e.getValue() > 0
            )
            .max(
                Comparator.comparingDouble(Map.Entry::getValue)
            )
            .map(
                e -> e
                    .getKey()
                    .getNeighbourClasses()
                    .getClassBitmap()
                    .getRecognitionClass()
            )
            .orElse(
                null
            );
    }

    private double countMembershipFunction(
        ClassBitmap testedClass,
        CriteriaValue criteriaValue
    ) {
        ClassBitmap distanceToClass =
            criteriaValue
                .getNeighbourClasses()
                .getClassBitmap();
        int radius = criteriaValue.getRadius();
        double averageDistance = averageDistance(testedClass, distanceToClass);
        return 1 - averageDistance / radius;
    }

    private double averageDistance(
        ClassBitmap distanceFrom,
        ClassBitmap distanceTo
    ) {
        boolean[] referenceVector = classBitmapService
            .referenceVectorFor(
                distanceTo
            );
        return Arrays.stream(distanceFrom.getBitmap())
            .mapToDouble(
                implementation ->
                    classBitmapService.countDistanceBetweenVectors(
                        implementation,
                        referenceVector
                    )
            )
            .average()
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Not empty bitmap is expected, found: "
                        + Arrays.deepToString(
                            distanceTo.getBitmap()
                        )
                )
            );
    }

    private RecognitionClass baseClass(Set<CriteriaValue> trainingResult) {
        return trainingResult
            .stream()
            .findAny()
            .orElseThrow(
                () -> new NoSuchElementException("Train result is empty")
            )
            .getNeighbourClasses()
            .getClassBitmap()
            .getBaseClass();
    }

    private int margin(Set<CriteriaValue> trainingResult) {
        return trainingResult
            .stream()
            .findAny()
            .orElseThrow(
                () -> new NoSuchElementException("Train result is empty")
            )
            .getNeighbourClasses()
            .getClassBitmap()
            .getMargin();
    }
}
