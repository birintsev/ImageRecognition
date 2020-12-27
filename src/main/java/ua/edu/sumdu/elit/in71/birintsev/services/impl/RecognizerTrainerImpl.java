package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import com.inamik.text.tables.Cell;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.NeighbourClasses;
import ua.edu.sumdu.elit.in71.birintsev.RecognitionClass;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;
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

    public static final double MIN_DISTINGUISH_PERCENTAGE = 0.5;

    private final StatisticVisualizationService statisticVisualizationService;

    private final ClassBitmapService classBitmapService;

    private final NeighbourService neighbourService;

    private final CriteriaMethod criteriaMethod;

    private final MathService mathService;

    public RecognizerTrainerImpl(
        ClassBitmapService classBitmapService,
        NeighbourService neighbourService,
        @Qualifier("ShannonCriteria")
            CriteriaMethod criteriaMethod,
        StatisticVisualizationService statisticVisualizationService,
        MathService mathService
    ) {
        this.classBitmapService = classBitmapService;
        this.neighbourService = neighbourService;
        this.criteriaMethod = criteriaMethod;
        this.statisticVisualizationService = statisticVisualizationService;
        this.mathService = mathService;
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
        RecognitionClass baseClass =
            bestCriterias
                .stream()
                .findAny()
                .orElseThrow(
                    () -> new RuntimeException("No criterias found")
                )
                .getNeighbourClasses()
                .getClassBitmap()
                .getBaseClass();
        int margin = bestCriterias
            .stream()
            .findAny()
            .orElseThrow(
                () -> new RuntimeException("No criterias found")
            )
            .getNeighbourClasses()
            .getClassBitmap()
            .getMargin();
        // code distances matrix
        Map<ClassBitmap, Map<ClassBitmap, Integer>> codeDistanceMatrix =
            neighbourService
                .getCodeDistancesMatrix(
                    bestCriterias
                        .stream()
                        .map(
                            cv -> cv.getNeighbourClasses().getClassBitmap()
                        )
                        .collect(
                            Collectors.toSet()
                        )
                );
        printCodeDistancesMatrix(codeDistanceMatrix);
        // workspace plot
        CALCULATION_LOGGER.info(
            "Workspace plot: "
                + statisticVisualizationService.createWorkspacePlot(
                source
            )
        );
        // margin corridor plot
        CALCULATION_LOGGER.info(
            "Margin corridor: "
                + statisticVisualizationService.createMarginCorridorPlot(
                classBitmapService.createFor(
                    baseClass,
                    baseClass,
                    margin
                )
            )
        );
        // bitmaps and reference vectors of class for study
        for (CriteriaValue criteriaValue : bestCriterias) {
            /*int margin =
                criteriaValue
                    .getNeighbourClasses()
                    .getClassBitmap()
                    .getMargin();*/
            RecognitionClass recognitionClass =
                criteriaValue
                    .getNeighbourClasses()
                    .getClassBitmap()
                    .getRecognitionClass();
            /*RecognitionClass baseClass =
                criteriaValue
                    .getNeighbourClasses()
                    .getClassBitmap()
                    .getBaseClass();*/
            ClassBitmap classBitmap =
                classBitmapService.createFor(
                    recognitionClass,
                    baseClass,
                    margin
                );
            CALCULATION_LOGGER.info(
                "Class of image: ["
                    + recognitionClass.getImageFile().getAbsolutePath()
                    + "]"
                    + System.lineSeparator()
                    // base class reference vector visualization
                    + "Reference vector: "
                    + statisticVisualizationService.visualizeReferenceVectorOf(
                        classBitmap
                    )
                    + System.lineSeparator()
                    // base class bitmap visualization
                    + "Class bitmap: "
                    + statisticVisualizationService.visualizeBitmap(
                        classBitmap
                    )
                    // radius optimization plot
                    + "Radius optimization plot: "
                    + statisticVisualizationService.createWorkspaceRadiusPlot(
                        criteriaValue
                    )
            );
        }
    }

    // this method is responsible for code distances matrix output format
    private void printCodeDistancesMatrix(
        Map<ClassBitmap, Map<ClassBitmap, Integer>> matrix
    ) {
        List<ClassBitmap> classBitmaps =
            matrix
                .keySet()
                .stream()
                .sorted(
                    Comparator.comparing(
                        classBitmap ->
                            classBitmap
                                .getRecognitionClass()
                                .getImageFile()
                                .getAbsolutePath()
                    )
                )
                .collect(
                    Collectors.toList()
                );
        int matrixSize = classBitmaps.size() + 1;
        GridTable table = GridTable.of(matrixSize, matrixSize);
        // filling in rows and cols headers
        for (int i = 1; i < matrixSize; i++) {
            Collection<String> cell = Cell.of(
                classBitmaps.get(i - 1)
                    .getRecognitionClass()
                    .getImageFile()
                    .getName()
            );
            table.put( // columns headers
                0,
                i,
                cell
            );
            table.put( // rows headers
                i,
                0,
                cell
            );
        }
        // filling in code distances
        for (int i = 1; i < table.numRows(); i++) {
            for (int j = 1; j < table.numCols(); j++) {
                table.put(
                    i,
                    j,
                    Cell.of(
                        matrix
                            .get(
                                classBitmaps.get(i - 1)
                            )
                            .get(
                                classBitmaps.get(j - 1)
                            )
                            .toString()
                    )
                );
            }
        }
        table = Border.SINGLE_LINE.apply(table);
        try (
            StringWriter stringWriter = new StringWriter();
            PrintStream printStream = new PrintStream(
                new WriterOutputStream(
                    stringWriter,
                    Charset.defaultCharset()
                ),
                true
            )
        ) {
            Util.print(table, printStream);
            CALCULATION_LOGGER.info("" + System.lineSeparator() + stringWriter);
        } catch (IOException e) {
            CALCULATION_LOGGER.error(e);
            throw new UncheckedIOException(e);
        }
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
        int workspaceComparingResult;
        int avgCriteriaValueComparingResult;
        if (set1.isEmpty() || set2.isEmpty()) {
            return set1.isEmpty() ? set2 : set1;
        }
        Comparator<Set<CriteriaValue>> workspacesComparator =
            new Comparator<Set<CriteriaValue>>() {
                @Override
                public int compare(
                    Set<CriteriaValue> s1,
                    Set<CriteriaValue> s2
                ) {
                    return Integer.compare(
                        belongToWorkspace(s1),
                        belongToWorkspace(s2)
                    );
                }

                // counts how much elements that belong to workspace
                private int belongToWorkspace(Set<CriteriaValue> set) {
                    int belongToWorkspace = 0;
                    for (CriteriaValue cv : set) {
                        if (cv.isWorkspace()) {
                            belongToWorkspace++;
                        }
                    }
                    return belongToWorkspace;
                }
            };
        Comparator<Set<CriteriaValue>> averageCriteriaValuesComparator =
            new Comparator<Set<CriteriaValue>>() {
                @Override
                public int compare(
                    Set<CriteriaValue> s1,
                    Set<CriteriaValue> s2
                ) {
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
        workspaceComparingResult = workspacesComparator.compare(set1, set2);
        if (workspaceComparingResult > 0) {
            return set1;
        } else if (workspaceComparingResult < 0) {
            return set2;
        } else {
            avgCriteriaValueComparingResult =
                averageCriteriaValuesComparator
                    .compare(set1, set2);
        }
        return avgCriteriaValueComparingResult >= 0 ? set1 : set2;
    }
}
