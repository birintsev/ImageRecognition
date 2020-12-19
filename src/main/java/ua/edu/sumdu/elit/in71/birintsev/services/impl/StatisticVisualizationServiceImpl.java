package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import io.quickchart.QuickChart;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.services.MathService;
import ua.edu.sumdu.elit.in71.birintsev.services.StatisticVisualizationService;
import static ua.edu.sumdu.elit.in71.birintsev.services.impl.RecognizerTrainerImpl.CALCULATION_LOGGER;

@Service
@AllArgsConstructor
public class StatisticVisualizationServiceImpl
implements StatisticVisualizationService {

    private final MathService mathService;

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

    @Override
    public URL visualizeBitmap(
        boolean[][] bitmap
    ) {
        try {
            return new URL("https://example.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public URL visualizeReferenceVector(
        boolean[] vector,
        int height
    ) {
        try {
            return new URL("https://example.com");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    // request POJOs
    @Data
    private static final class QuickChartPlotRequest {

        @JsonSerialize(converter = EnumToStringSerializer.class)
        private Type type;

        private Data data;

        private Options options;

        public QuickChartPlotRequest(
            double[] topBorder,
            double[] mean,
            double[] bottomBorder,
            String plotTitle
        ) {
            this.type = Type.LINE;
            this.data = new Data(
                topBorder,
                mean,
                bottomBorder
            );
            this.options = new Options(
                new Options.Scales(
                    Collections.singletonList(
                        new Options.Axe(
                            true,
                            new Options.Axe.ScaleLabel(
                                true,
                                "Features"
                            ),
                            (double) 0,
                            (double) mean.length - 1
                        )
                    ),
                    Collections.singletonList(
                        new Options.Axe(
                            true,
                            new Options.Axe.ScaleLabel(
                                true,
                                "Margin"
                            ),
                            Arrays.stream(bottomBorder).min().orElse(-1),
                            Arrays.stream(topBorder).max().orElse(101)
                        )
                    )
                ),
                new Options.Title(
                    true,
                    plotTitle
                )
            );
        }

        /**
         * Constructor for generating workspace plot
         *
         * @see StatisticVisualizationServiceImpl#createWorkspacePlot(Map)
         * */
        public QuickChartPlotRequest(
            List<Integer> xValues,
            List<Double> criteriaValues,
            List<Boolean> isWorkspace,
            String datasetLabel
        ) {
            this.type = Type.LINE;
            this.data = new Data(
                xValues,
                criteriaValues,
                filterWorkspaceValues(
                    criteriaValues,
                    isWorkspace
                ),
                datasetLabel
            );
            this.options = new Options(
                new Options.Scales(
                    Collections.singletonList(
                        new Options.Axe(
                            true,
                            new Options.Axe.ScaleLabel(
                                true,
                                "Margin"
                            ),
                            (double) xValues.get(0),
                            (double) xValues.get(xValues.size() - 1)
                        )
                    ),
                    Collections.singletonList(
                        new Options.Axe(
                            true,
                            new Options.Axe.ScaleLabel(
                                true,
                                "Criteria"
                            ),
                            (double) xValues.get(0),
                            (double) xValues.get(xValues.size() - 1)
                        )
                    )
                ),
                null
            );
        }

        private List<Double> filterWorkspaceValues(
            List<Double> criteriaValues,
            List<Boolean> isWorkspace
        ) {
            List<Double> workspaceCriteriaValues = new ArrayList<>(
                criteriaValues.size()
            );
            for (int i = 0; i < criteriaValues.size(); i++) {
                workspaceCriteriaValues
                    .add(isWorkspace.get(i) ? criteriaValues.get(i) : null);
            }
            return workspaceCriteriaValues;
        }

        @lombok.Data
        private static class Data {

            private List<String> labels;

            private List<DataSet> datasets;

            /**
             * Constructor for generating Margin corridor plot
             *
             * @see StatisticVisualizationServiceImpl#createWorkspacePlot(Map)
             * */
            public Data(
                double[] topBorder,
                double[] mean,
                double[] bottomBorder
            ) {
                labels = IntStream
                    .range(0, mean.length)
                    .boxed()
                    .map(
                        Object::toString
                    )
                    .collect(
                        Collectors.toList()
                    );
                datasets = Arrays.asList(
                    new DataSet( // top border
                        Arrays.stream(topBorder)
                            .boxed()
                            .collect(
                                Collectors.toList()
                            ),
                        DataSet.COLOR_TRANSPARENT,
                        DataSet.COLOR_MAIN,
                        "Top border",
                        DataSet.Fill.FALSE,
                        DataSet.NO_POINTS_RADIUS
                    ),
                    new DataSet( // mean line
                        Arrays.stream(mean)
                            .boxed()
                            .collect(
                                Collectors.toList()
                            ),
                        DataSet.COLOR_TRANSPARENT,
                        DataSet.COLOR_SECONDARY,
                        "Mean",
                        DataSet.Fill.START,
                        DataSet.NO_POINTS_RADIUS
                    ),
                    new DataSet( // bottom border
                        Arrays.stream(bottomBorder)
                            .boxed()
                            .collect(
                                Collectors.toList()
                            ),
                        DataSet.COLOR_TRANSPARENT,
                        DataSet.COLOR_MAIN,
                        "Bottom border",
                        DataSet.Fill.FALSE,
                        DataSet.NO_POINTS_RADIUS
                    )
                );
            }

            /**
             * Constructor for generating workspace plot
             *
             * @see StatisticVisualizationServiceImpl#createWorkspacePlot(Map)
             * */
            public Data(
                List<Integer> xValues,
                List<Double> allCriteriaValues,
                List<Double> workspaceCriterias,
                String datasetLabel
            ) {
                labels = xValues
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
                datasets = Arrays.asList(
                    new DataSet( // all criteria values
                        allCriteriaValues,
                        null,
                        DataSet.COLOR_BORDER,
                        datasetLabel,
                        DataSet.Fill.FALSE,
                        DataSet.NO_POINTS_RADIUS
                    ),
                    new DataSet( // workspace color fill
                        workspaceCriterias,
                        DataSet.COLOR_WORKSPACE,
                        DataSet.COLOR_TRANSPARENT,
                        "Workspace",
                        DataSet.Fill.START,
                        DataSet.DEFAULT_POINTS_RADIUS
                    )
                );
            }
        }

        @lombok.Data
        @AllArgsConstructor
        private static class DataSet {

            public static final String COLOR_MAIN = "#9999ff";

            public static final String COLOR_SECONDARY = "#fff70d";

            public static final String COLOR_TRANSPARENT = "transparent";

            public static final String COLOR_BORDER = COLOR_SECONDARY;

            public static final String COLOR_WORKSPACE = COLOR_MAIN;

            public static final int DEFAULT_POINTS_RADIUS = 5;

            public static final int NO_POINTS_RADIUS = 0;

            private List<Double> data;

            private String backgroundColor;

            private String borderColor;

            private String label;

            @JsonSerialize(converter = EnumToStringSerializer.class)
            private Fill fill;

            private int pointRadius;

            private enum Fill {

                START("start"),
                END("end"),
                ORIGIN("origin"),
                FALSE("false");

                private final String fill;

                Fill(String fill) {
                    this.fill = fill;
                }

                @Override
                public String toString() {
                    return fill;
                }
            }
        }

        private enum Type {

            LINE("line");

            private final String type;

            Type(String type) {
                this.type = type;
            }

            @Override
            public String toString() {
                return type;
            }
        }

        @AllArgsConstructor
        @lombok.Data
        private static final class Options {

            private Scales scales;

            private Title title;

            @AllArgsConstructor
            @lombok.Data
            public static class Title {

                private boolean display;

                private String text;
            }

            @AllArgsConstructor
            @lombok.Data
            private static class Scales {

                private List<Axe> xAxes;

                private List<Axe> yAxes;
            }

            @lombok.Data
            @AllArgsConstructor
            private static class Axe {

                private boolean display;

                private ScaleLabel scaleLabel;

                private Double min;

                private Double max;

                @lombok.Data
                @AllArgsConstructor
                private static class ScaleLabel {

                    private boolean display;

                    private String labelString;
                }
            }
        }
    }

    private static class EnumToStringSerializer<T extends Enum<?>>
        extends StdConverter<T, String> {

        @Override
        public String convert(T value) {
            return value.toString();
        }
    }
}
