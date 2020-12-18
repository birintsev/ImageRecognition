package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.NeighbourClasses;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.NeighbourService;
import ua.edu.sumdu.elit.in71.birintsev.services.Recognizer;
import ua.edu.sumdu.elit.in71.birintsev.services.RecognizerTrainer;
import ua.edu.sumdu.elit.in71.birintsev.services.StatisticVisualizationService;
import ua.edu.sumdu.elit.in71.birintsev.services.criteria.CriteriaMethod;

@Service
public class RecognizerTrainerImpl implements RecognizerTrainer {

    public static final Logger CALCULATION_LOGGER = Logger.getLogger(
        "CALCULATION_LOGGER"
    );

    private static final double MIN_DISTINGUISH_PERCENTAGE = 0.5;

    private final StatisticVisualizationService statisticVisualizationService;

    private final ClassBitmapService classBitmapService;

    private final NeighbourService neighbourService;

    private final CriteriaMethod criteriaMethod;

    public RecognizerTrainerImpl(
        ClassBitmapService classBitmapService,
        NeighbourService neighbourService,
        @Qualifier("CulbacCriteria")
            CriteriaMethod criteriaMethod,
        StatisticVisualizationService statisticVisualizationService
    ) {
        this.classBitmapService = classBitmapService;
        this.neighbourService = neighbourService;
        this.criteriaMethod = criteriaMethod;
        this.statisticVisualizationService = statisticVisualizationService;
    }

    @Override
    public Recognizer train(Collection<String> pathsToImages) {
        Map<Integer, Collection<CriteriaValue>> marginCriteriasStatistic =
            new HashMap<>();
        Set<RecognitionClass> recognitionClasses = pathsToImages
            .stream()
            .map(
                path -> {
                    try {
                        return new RecognitionClass(path);
                    } catch (FileNotFoundException e) {
                        CALCULATION_LOGGER.error(e);
                        throw new UncheckedIOException(e);
                    }
                }
            )
            .collect(Collectors.toSet());
        Set<CriteriaValue> bestCriterias = new HashSet<>();
        for (RecognitionClass baseClass : recognitionClasses) {
            long optimizationByClassStartTime = System.currentTimeMillis();
            Set<CriteriaValue> optimizedByBaseClass = new HashSet<>();
            for (int i = 0; i <= RecognitionClass.MAX_MARGIN; i++) {
                final int currentMargin = i;
                long optimizationByMarginStartTime = System.currentTimeMillis();
                Set<ClassBitmap> classBitmaps =
                    recognitionClasses
                        .stream()
                        .map(
                            recognitionClass -> classBitmapService.createFor(
                                recognitionClass,
                                baseClass,
                                currentMargin
                            )
                        )
                        .collect(Collectors.toSet());
                Set<NeighbourClasses> neighbourClasses = classBitmaps
                    .stream()
                    .map(
                        classBitmap -> neighbourService.getFor(
                            classBitmap,
                            classBitmaps
                        )
                    )
                    .collect(Collectors.toSet());
                Set<CriteriaValue> optimizedByRadiuses = neighbourClasses
                    .parallelStream()
                    .map(this::findOptimalRadius) // hypersphere radius optimization
                    .collect(Collectors.toSet());
                optimizedByBaseClass = maxAverageCriteria(
                    optimizedByRadiuses,
                    optimizedByBaseClass
                );
                CALCULATION_LOGGER.trace(
                    "Optimization by margin = "
                        + currentMargin
                        + " (~"
                        + secondsSince(optimizationByMarginStartTime)
                        + " s)"
                );
                marginCriteriasStatistic.put(
                    currentMargin,
                    optimizedByRadiuses
                );
            }
            CALCULATION_LOGGER.trace(
                "Optimization by base class "
                    + baseClass
                    + " (~"
                    + secondsSince(optimizationByClassStartTime)
                    + " s)"
            );
            bestCriterias = maxAverageCriteria(
                optimizedByBaseClass,
                bestCriterias
            );
        }
        visualizeStatistic(marginCriteriasStatistic, bestCriterias);
        return new RecognizerImpl(bestCriterias, classBitmapService);
    }

    private void visualizeStatistic(
        Map<Integer, Collection<CriteriaValue>> source,
        Set<CriteriaValue> bestCriterias
    ) {
        // workspace plot
        CALCULATION_LOGGER.info(
            statisticVisualizationService.createWorkspacePlot(
                source
            )
        );
        bestCriterias
            .stream()
            .findAny()
            .ifPresent(
                criteriaValue -> {
                    URL baseClassBitmapPlotURL;
                    URL bestCriteriasPlotURL;
                    RecognitionClass bsClss =
                        criteriaValue
                            .getNeighbourClasses()
                            .getClassBitmap()
                            .getBaseClass();
                    // base class reference vector visualization
                    bestCriteriasPlotURL = statisticVisualizationService
                        .visualizeReferenceVector(
                            classBitmapService.referenceVectorFor(
                                classBitmapService.createFor(
                                    bsClss,
                                    bsClss,
                                    criteriaValue
                                        .getNeighbourClasses()
                                        .getClassBitmap()
                                        .getMargin()
                                )
                            ),
                            criteriaValue
                                .getNeighbourClasses()
                                .getClassBitmap()
                                .getBitmap()
                                .length
                        );
                    // base class bitmap visualization
                    baseClassBitmapPlotURL = statisticVisualizationService
                        .visualizeBitmap(
                            classBitmapService.createFor(
                                bsClss,
                                bsClss,
                                criteriaValue
                                    .getNeighbourClasses()
                                    .getClassBitmap()
                                    .getMargin()
                            )
                                .getBitmap()
                        );
                    CALCULATION_LOGGER.info(
                        "Best criterias plot: "
                            + bestCriteriasPlotURL
                            + System.lineSeparator()
                            + "Base class bitmap: "
                            + baseClassBitmapPlotURL
                    );
                }
            );
    }

    private double secondsSince(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }

    /**
     * Hypersphere radius optimization
     * */
    private CriteriaValue findOptimalRadius(NeighbourClasses neighbours) {
        int maxRadius = neighbours.getClassBitmap().getBitmap()[0].length;
        Set<CriteriaValue> radiusCriteriaValue = new HashSet<>();
        for (int radius = 0; radius <= maxRadius; radius++) {
            radiusCriteriaValue.add(
                criteriaMethod.getFor(
                    radius,
                    neighbours,
                    MIN_DISTINGUISH_PERCENTAGE
                )
            );
        }
        return radiusCriteriaValue
            .stream()
            .max(
                (cv1, cv2) -> {
                    if (cv1.isWorkspace() == cv2.isWorkspace()) {
                        return Double.compare(
                            cv1.getCriteria(),
                            cv2.getCriteria()
                        );
                    } else {
                        return cv1.isWorkspace() ? 1 : -1;
                    }
                }
            )
            .orElseThrow(
                () -> new RuntimeException(
                    "Can not find optimal radius for "
                        + neighbours.getClassBitmap()
                )
            );
    }

    /**
     * Returns one of passed, depending on the average criteria values in it
     * */
    private Set<CriteriaValue> maxAverageCriteria(
        Set<CriteriaValue> set1,
        Set<CriteriaValue> set2
    ) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return set1.isEmpty() ? set2 : set1;
        }
        Comparator<Set<CriteriaValue>> averageCriteriaValuesComparator =
            new Comparator<Set<CriteriaValue>>() {
                @Override
                public int compare(
                    Set<CriteriaValue> s1,
                    Set<CriteriaValue> s2
                ) {
                    //CALCULATION_LOGGER.error("avg1 =" + avgCriteria(s1) + " avg2 =" + avgCriteria(s2));
                    return Double.compare(avgCriteria(s1), avgCriteria(s2));
                }

                private double avgCriteria(Set<CriteriaValue> set) {
                    double totalCriteria = 0;
                    for (CriteriaValue cv : set) {
                        totalCriteria += cv.getCriteria();
                    }
                    return totalCriteria / set.size();
                }
            };
        return averageCriteriaValuesComparator.compare(set1, set2) >= 0
            ? set1
            : set2;
    }
}
