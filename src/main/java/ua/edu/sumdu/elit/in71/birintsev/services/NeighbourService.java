package ua.edu.sumdu.elit.in71.birintsev.services;

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
}
