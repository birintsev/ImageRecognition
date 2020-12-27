package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quickchart.QuickChart;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;
import ua.edu.sumdu.elit.in71.birintsev.services.StatisticVisualizationService;
import ua.edu.sumdu.elit.in71.birintsev.services.criteria.CriteriaMethod;
import ua.edu.sumdu.elit.in71.birintsev.services.impl.quickchart.QuickChartPlotRequest;
import static ua.edu.sumdu.elit.in71.birintsev.services.impl.RecognizerTrainerImpl.CALCULATION_LOGGER;
import static ua.edu.sumdu.elit.in71.birintsev.services.impl.RecognizerTrainerImpl.MIN_DISTINGUISH_PERCENTAGE;

@Service
@AllArgsConstructor
public class StatisticVisualizationServiceImpl
implements StatisticVisualizationService {

    private final MathService mathService;

    private final ClassBitmapService classBitmapService;

    public static final String IMAGES_FORMAT = "bmp";

    public static final String STATISTIC_OUTPUT_DIRECTORY_NAME =
        "ImageRecognitionStatistic";

    private static final int PIXELS_REFERENCE_VECTOR_HEIGHT = 50;

    @Override
    public URL createWorkspacePlot(
        Map<Integer, Collection<CriteriaValue>> source
    ) {
        return send(
            buildRequestForWorkspacePlot(source)
        );
    }

    @Override
    public URL createMarginCorridorPlot(
        double[] bottomBorder,
        double[] mean,
        double[] topBorder
    ) {
        return send(
            buildRequestForMarginCorridor(topBorder, mean, bottomBorder)
        );
    }

    @Override
    public URL visualizeBitmap(
        ClassBitmap classBitmap
    ) {
        try {
            return writeMatrix(
                createMatrix(
                    classBitmap.getBitmap()
                ),
                defaultOutputFileFor(
                    classBitmap,
                    "_bitmap_"
                        + System.currentTimeMillis()
                        + "." + IMAGES_FORMAT
                )
                    .getAbsolutePath()
            );
        } catch (IOException e) {
            CALCULATION_LOGGER.error(e);
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public URL visualizeReferenceVectorOf(
        ClassBitmap classBitmap
    ) {
        try {
            return writeMatrix(
                createMatrix(
                    classBitmapService.referenceVectorFor(classBitmap)
                ),
                defaultOutputFileFor(
                    classBitmap,
                    "_reference_vector_"
                        + System.currentTimeMillis()
                        + "."
                        + IMAGES_FORMAT
                )
                    .getAbsolutePath()
            );
        } catch (IOException e) {
            CALCULATION_LOGGER.error(e);
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public URL createMarginCorridorPlot(ClassBitmap baseClass) {
        int margin = baseClass.getMargin();
        double[] mean = mathService
            .mean(
                baseClass
                    .getRecognitionClass()
                    .getGrayScaleImage()
            );
        return createMarginCorridorPlot(
            mathService.plus(mean, -margin),
            mean,
            mathService.plus(mean, margin)
        );
    }

    @Override
    public URL createWorkspaceRadiusPlot(Map<Integer, CriteriaValue> source) {
        return send(
            buildRequestForWorkspaceByRadiusPlot(source)
        );
    }

    @Override
    public URL createWorkspaceRadiusPlot(CriteriaValue bestCriteria) {
        return createWorkspaceRadiusPlot(
            calcCriteriaValuesOfPossibleRadiuses(
                bestCriteria
            )
        );
    }

    private Map<Integer, CriteriaValue> calcCriteriaValuesOfPossibleRadiuses(
        CriteriaValue bestCriteria
    ) {
        Map<Integer, CriteriaValue> source = new HashMap<>();
        CriteriaMethod criteriaMethod = bestCriteria.getCriteriaMethod();
        int maxRadius =
            bestCriteria
                .getNeighbourClasses()
                .getClassBitmap()
                .getBitmap()[0]
                .length;
        for (int radius = 0; radius <= maxRadius; radius++) {
            source.put(
                radius,
                criteriaMethod.getFor(
                    radius,
                    bestCriteria.getNeighbourClasses(),
                    MIN_DISTINGUISH_PERCENTAGE
                )
            );
        }
        return source;
    }

    private URL send(QuickChartPlotRequest request) {
        try {
            QuickChart chart = new QuickChart();
            String requestConfig =
                new ObjectMapper()
                    .writeValueAsString(request);
            chart.setConfig(requestConfig);
            CALCULATION_LOGGER.trace("Sending the request: " + requestConfig);
            return new URL(chart.getShortUrl());
        } catch (MalformedURLException | JsonProcessingException e) {
            CALCULATION_LOGGER.error(e);
            throw new UncheckedIOException(e);
        }
    }

    private QuickChartPlotRequest buildRequestForWorkspaceByRadiusPlot(
        Map<Integer, CriteriaValue> source
    ) {
        return new QuickChartPlotRequest(
            source.keySet()
                .stream()
                .sorted()
                .collect(
                    Collectors.toList()
                )
            ,
            source.entrySet()
                .stream()
                .sorted(
                    Comparator.comparingInt(
                        Map.Entry::getKey
                    )
                )
                .map(
                    kv -> kv.getValue().getCriteria()
                )
                .collect(
                    Collectors.toList()
                ),
            source.entrySet()
                .stream()
                .sorted(
                    Comparator.comparingInt(
                        Map.Entry::getKey
                    )
                )
                .map(
                    kv -> kv.getValue().isWorkspace()
                )
                .collect(
                    Collectors.toList()
                ),
            source.entrySet()
                .stream()
                .findAny()
                .map(
                    kv -> kv
                        .getValue()
                        .getNeighbourClasses()
                        .getClassBitmap()
                        .getRecognitionClass()
                        .getImageFile()
                        .getName()
                )
                .orElse("")
        );
    }

    private QuickChartPlotRequest buildRequestForWorkspacePlot(
        Map<Integer, Collection<CriteriaValue>> source
    ) {
        return new QuickChartPlotRequest(
            source.keySet()
                .stream()
                .sorted()
                .collect(
                    Collectors.toList()
                ),
            source.entrySet()
                .stream()
                .sorted(
                    Comparator.comparingInt(
                        Map.Entry::getKey
                    )
                )
                .map(
                    kv -> kv.getValue()
                        .stream()
                        .mapToDouble(
                            CriteriaValue::getCriteria
                        )
                        .average()
                )
                .map(
                    o -> o.orElse(0)
                )
                .collect(
                    Collectors.toList()
                ),
            source.entrySet()
                .stream()
                .sorted(
                    Comparator.comparingInt(
                        Map.Entry::getKey
                    )
                )
                .map(
                    kv ->
                        kv.getValue()
                            .stream()
                            .mapToDouble(
                                cv -> cv.isWorkspace() ? 1 : 0
                            )
                            .average()
                            .orElse(0) == 1
                )
                .collect(
                    Collectors.toList()
                ),
            source.entrySet()
                .stream()
                .findAny()
                .map(
                    e -> e.getValue()
                        .stream()
                        .findAny()
                        .map(
                            cv -> cv.getCriteriaMethod().getMethodName()
                        )
                        .orElse("")
                )
                .orElse("")
        );
    }

    private QuickChartPlotRequest buildRequestForMarginCorridor(
        double[] topBorder,
        double[] mean,
        double[] bottomBorder
    ) {
        return new QuickChartPlotRequest(
            topBorder,
            mean,
            bottomBorder,
            "Margin corridor"
        );
    }

    private URL writeMatrix(BufferedImage matrix, String fileName)
    throws IOException {
        File file = new File(fileName);
        ImageIO.write(
            matrix,
            "bmp",
            file
        );
        return file.toURI().toURL();
    }

    private File defaultOutputFileFor(
        ClassBitmap classBitmap,
        String fileNameSuffix
    ) {
        File originalImageFile =
            classBitmap
                .getRecognitionClass()
                .getImageFile();
        File statisticDirectory = new File(
            originalImageFile.getParent(),
            STATISTIC_OUTPUT_DIRECTORY_NAME
        );
        if (!statisticDirectory.isDirectory() && statisticDirectory.mkdir()) {
            CALCULATION_LOGGER.info(
                "Statistic output directory has been created: "
                    + statisticDirectory
            );
        }
        return new File(
            statisticDirectory,
            originalImageFile.getName() + fileNameSuffix
        );
    }

    private BufferedImage createMatrix(boolean[][] matrix) {
        Color primaryColor = Color.decode(
            QuickChartPlotRequest.DataSet.COLOR_PRIMARY
        );
        Color secondaryColor = Color.decode(
            QuickChartPlotRequest.DataSet.COLOR_SECONDARY
        );
        BufferedImage bufferedImage = new BufferedImage(
            matrix[0].length,
            matrix.length,
            BufferedImage.TYPE_INT_RGB
        );
        for (int y = 0, i = 0; y < bufferedImage.getHeight(); y++, i++) {
            for (int x = 0, j = 0; x < bufferedImage.getWidth(); x++, j++) {
                bufferedImage.setRGB(
                    x,
                    y,
                    (matrix[i][j] ? primaryColor : secondaryColor).getRGB()
                );
            }
        }
        return bufferedImage;
    }

    private BufferedImage createMatrix(
        boolean[] vector
    ) {
        boolean[][] matrix = new boolean[PIXELS_REFERENCE_VECTOR_HEIGHT][];
        for (int i = 0; i < PIXELS_REFERENCE_VECTOR_HEIGHT; i++) {
            matrix[i] = Arrays.copyOf(vector, vector.length);
        }
        return createMatrix(matrix);
    }
}
