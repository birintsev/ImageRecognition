package ua.edu.sumdu.elit.in71.birintsev.services.criteria;

import lombok.AllArgsConstructor;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.CriteriaValue;
import ua.edu.sumdu.elit.in71.birintsev.NeighbourClasses;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import static ua.edu.sumdu.elit.in71.birintsev.services.impl.RecognizerTrainerImpl.CALCULATION_LOGGER;

@AllArgsConstructor
public abstract class AbstractCriteriaMethod implements CriteriaMethod {

    private final ClassBitmapService classBitmapService;

    @Override
    public CriteriaValue getFor(
        int radius,
        NeighbourClasses hypersphereNeighbourClasses,
        double minDistinguishPercentage
    ) {
        boolean[][] clazz =
            hypersphereNeighbourClasses.getClassBitmap().getBitmap();
        boolean[][] neighbour =
            hypersphereNeighbourClasses.getNeighbourClassBitmap().getBitmap();
        //long t = System.currentTimeMillis();
        double d1 = belongPercent(
            clazz,
            hypersphereNeighbourClasses.getClassBitmap(),
            radius
        );
        //CALCULATION_LOGGER.trace("d1 took " + (System.currentTimeMillis() - t) + "ms");
        double d2 = notBelongPercent(
            neighbour,
            hypersphereNeighbourClasses.getClassBitmap(),
            radius
        );
        double criteria = calc(d1, d2);
        //CALCULATION_LOGGER.error("(" + d1 + ";" + d2 + ")=" + criteria);
        return new CriteriaValue(
            radius,
            hypersphereNeighbourClasses,
            criteria,
            minDistinguishPercentage,
            belongsToWorkspace(d1, d2, minDistinguishPercentage),
            this
        );
    }

    public abstract double calc(double d1, double d2);

    private boolean belongsToWorkspace(
        double d1,
        double d2,
        double minDistinguishPercentage
    ) {
        return d1 >= minDistinguishPercentage && d2 >= minDistinguishPercentage;
    }

    private double belongPercent( // >= 70ms
        boolean[][] implementations,
        ClassBitmap classBitmap,
        int radius
    ) {
        int belongsCount = 0;
        for (boolean[] row : implementations) {
            if (
                classBitmapService.belongsToHypersphere(
                    row,
                    classBitmap,
                    radius
                )
            ) {
                belongsCount++;
            }
        }
        return ((double) belongsCount) / implementations.length;
    }

    private double notBelongPercent(
        boolean[][] implementations,
        ClassBitmap classBitmap,
        int radius
    ) {
        return 1 - belongPercent(implementations, classBitmap, radius);
    }

    // returns the base 2 logarithm of the passed number
    protected double log2(double number) {
        return Math.log10(number) / Math.log10(2);
    }
}
