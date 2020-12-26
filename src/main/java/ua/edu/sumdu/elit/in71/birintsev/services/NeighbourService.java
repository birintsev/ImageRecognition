package ua.edu.sumdu.elit.in71.birintsev.services;

import java.util.Map;
import java.util.Set;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.NeighbourClasses;

public interface NeighbourService {

    /**
     * Searches the 'nearest' class for the {@code classBitmap}
     * among the set of {@code otherClasses}
     *
     * @param classBitmap  a class for which the neighbour is searched
     * @param otherClasses a set of all classes among which the closest one
     *                     should be searched.
     *                     If there are two or more classes
     *                     equidistant from the {@code classBitmap},
     *                     a random of them is returned.
     *
     * @see NeighbourClasses#getDistance
     * */
    NeighbourClasses getFor(
        ClassBitmap classBitmap,
        Set<ClassBitmap> otherClasses
    );

    /**
     * Creates a matrix of code distances between classes centres
     * <p>
     * The result of this method work is a collection of {@code classes}
     * mapped to themselves (actually, it's a cartesian product)
     * in such a way that the 'final' value
     * (i.e. the value of the inner map)
     * is a distance between the outer-map-key and the inner-map-key.
     * <strong>Simply put, treat the result as a matrix.</strong>
     *
     * @param  classes all classes to be included into the matrix
     * @return         a map of code distances (see the description above)
     * */
    Map<ClassBitmap, Map<ClassBitmap, Integer>> getCodeDistancesMatrix(
        Set<ClassBitmap> classes
    );
}
