package ua.edu.sumdu.elit.in71.birintsev.services.impl.quickchart;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import ua.edu.sumdu.elit.in71.birintsev.services.impl.StatisticVisualizationServiceImpl;

// request POJOs
@Data
public final class QuickChartPlotRequest {

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
                    DataSet.COLOR_PRIMARY,
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
                    DataSet.COLOR_PRIMARY,
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
    public static class DataSet {

        public static final String COLOR_PRIMARY = "#ff7b00";

        public static final String COLOR_SECONDARY = "#0022ff";

        public static final String COLOR_TRANSPARENT = "transparent";

        public static final String COLOR_BORDER = COLOR_SECONDARY;

        public static final String COLOR_WORKSPACE = COLOR_PRIMARY;

        public static final int DEFAULT_POINTS_RADIUS = 5;

        public static final int NO_POINTS_RADIUS = 0;

        private List<Double> data;

        private String backgroundColor;

        private String borderColor;

        private String label;

        @JsonSerialize(converter = EnumToStringSerializer.class)
        private DataSet.Fill fill;

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

        private Options.Scales scales;

        private Options.Title title;

        @AllArgsConstructor
        @lombok.Data
        public static class Title {

            private boolean display;

            private String text;
        }

        @AllArgsConstructor
        @lombok.Data
        private static class Scales {

            private List<Options.Axe> xAxes;

            private List<Options.Axe> yAxes;
        }

        @lombok.Data
        @AllArgsConstructor
        private static class Axe {

            private boolean display;

            private Options.Axe.ScaleLabel scaleLabel;

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
