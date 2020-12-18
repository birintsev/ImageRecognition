package ua.edu.sumdu.elit.in71.birintsev.services.criteria;

import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.NeighbourClasses;

/**
 * Calculates functional criteria value
 * for passed {@link NeighbourClasses separation of the classes}
 * */
public interface CriteriaMethod {

    /**
     * Counts a {@link CriteriaValue} for two neighbour classes
     * */
    CriteriaValue getFor(
        int radius,
        NeighbourClasses hypersphereNeighbourClasses,
        double minDistinguishPercentage
    );

    /**
     * @return a name of this method
     * */
    String getMethodName();
}
