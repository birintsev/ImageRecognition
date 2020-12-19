package ua.edu.sumdu.elit.in71.birintsev.services;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;

public interface StatisticVisualizationService {

    /**
     * Builds a plot of criteria values and returns
     *
     * @param  source         source data for plot creation.
     *                        this parameter represents margin values
     *                        mapped to criteria values
     *                        for each recognition class
     * @return                a location of created plot
     *                        (image, HTML or other format)
     * */
    URL createWorkspacePlot(Map<Integer, Collection<CriteriaValue>> source);

    /**
     * Builds a plot that provides a possibility to visualise
     * so called margin corridor (for mapping an image to Hamming space)
     *
     * @param mean a center of the corridor
     * @param bottomBorder a bottom border of the corridor
     * @param topBorder a bottom border of the corridor
     *
     * @return a location of created plot
     * */
    URL createMarginCorridorPlot(
        double[] bottomBorder,
        double[] mean,
        double[] topBorder
    );

    /**
     * Creates a black&white resource (e.g. an image) that represents
     * passed bitmap.
     * <p>
     * The method actually maps {@code false}s to black elements
     * (e.g. pixels) and {@code true}s to white elements.
     *
     * @param  bitmap a bitmap to visualize
     * @return        a location of created resource
     * */
    URL visualizeBitmap(boolean[][] bitmap);

    /**
     * Creates a black&white resource (e.g. an image) that represents
     * passed reference vector.
     * <p>
     * The method actually maps {@code false}s to black elements
     * (e.g. pixels) and {@code true}s to white elements.
     *
     * @param  vector a reference vector to visualize
     * @param  height a height of resulting resource
     * @return        a location of created resource
     * */
    URL visualizeReferenceVector(boolean[] vector, int height);
}
