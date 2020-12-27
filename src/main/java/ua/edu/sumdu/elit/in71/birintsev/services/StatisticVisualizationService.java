package ua.edu.sumdu.elit.in71.birintsev.services;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;

public interface StatisticVisualizationService {

    /**
     * Builds a plot of criteria values and returns its URL
     *
     * Margin optimization
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
     * This method is a handy wrapper for
     * {@link #createMarginCorridorPlot(double[], double[], double[])}
     * <p>
     * Calculates {@code bottomBorder}, {@code mean} and {@code topBorder}
     * parameters for the wrapped method.
     *
     * @param  baseClass a source class for calculating the parameters
     * @return           a location of created plot
     * @see              #createMarginCorridorPlot(double[], double[], double[])
     * */
    URL createMarginCorridorPlot(ClassBitmap baseClass);

    /**
     * Creates a black&white resource (e.g. an image) that represents
     * passed bitmap.
     * <p>
     * The method actually maps {@code false}s to black elements
     * (e.g. pixels) and {@code true}s to white elements.
     *
     * @param  classBitmap a bitmap to visualize
     * @return             a location of created resource
     * */
    URL visualizeBitmap(ClassBitmap classBitmap);

    /**
     * Creates a black&white resource (e.g. an image) that represents
     * passed reference vector.
     * <p>
     * The method actually maps {@code false}s to black elements
     * (e.g. pixels) and {@code true}s to white elements.
     *
     * @param  classBitmap a class which reference vector to visualize
     * @return             a location of created resource
     * */
    URL visualizeReferenceVectorOf(ClassBitmap classBitmap);

    /**
     * Builds a plot of criteria values for each radius
     *
     * Radius optimization
     *
     * @param  source         source data for plot creation.
     *                        this parameter represents radius values
     *                        mapped to criteria values
     *                        for the same recognition class
     * @return                a location of created plot
     *                        (image, HTML or other format)
     * */
    URL createWorkspaceRadiusPlot(Map<Integer, CriteriaValue> source);

    /**
     * A handy overridden method for {@link #createWorkspacePlot}
     * that calculates criteria values
     * depending on all the possible radius values automatically
     *
     * @param criteriaValue a container with information for getting max radius
     *                      and criteria method
     *                      (see {@link #createWorkspaceRadiusPlot(Map)
     *                      the method} parameters))
     * @return              a location of created plot
     *                      (image, HTML or other format)
     *
     * @see                 ua.edu.sumdu.elit.in71.birintsev.services.criteria.CriteriaMethod
     * */
    URL createWorkspaceRadiusPlot(CriteriaValue criteriaValue);
}
