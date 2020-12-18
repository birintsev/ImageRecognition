package ua.edu.sumdu.elit.in71.birintsev.services.impl;

import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.elit.in71.birintsev.ClassBitmap;
import ua.edu.sumdu.elit.in71.birintsev.NeighbourClasses;
import ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService;
import ua.edu.sumdu.elit.in71.birintsev.services.NeighbourService;

@Service
@AllArgsConstructor
public class NeighbourServiceImpl implements NeighbourService {

    private final ClassBitmapService classBitmapService;

    @Override
    public NeighbourClasses getFor(
        ClassBitmap classBitmap,
        Set<ClassBitmap> otherClasses
    ) {
        ClassBitmap neighbour = findNeighbour(classBitmap, otherClasses);
        return new NeighbourClasses(
            classBitmap,
            neighbour,
            countDistanceBetweenClassCenters(classBitmap, neighbour)
        );
    }

    /**
     * Finds the closest class to the passed one.
     * The words 'the closest' here means 'the class
     * distance to center (reference vector) of which is the smallest
     * comparing to all other passed classes'
     *
     * @see #countDistanceBetweenClassCenters(ClassBitmap, ClassBitmap)
     * */
    private ClassBitmap findNeighbour(
        ClassBitmap classBitmap,
        Set<ClassBitmap> otherClasses
    ) {
        ClassBitmap closestClass = null;
        int distanceToClosestClass = Integer.MAX_VALUE;
        for (ClassBitmap candidate : otherClasses) {
            int distanceToCandidate;
            if (classBitmap.equals(candidate)) {
                continue;
            }
            distanceToCandidate = countDistanceBetweenClassCenters(
                classBitmap,
                candidate
            );
            if (distanceToCandidate < distanceToClosestClass) {
                closestClass = candidate;
                distanceToClosestClass = distanceToCandidate;
            }
        }
        return closestClass;
    }

    private int countDistanceBetweenClassCenters(
        ClassBitmap class1,
        ClassBitmap class2
    ) {
        return countDistanceBetweenReferenceVectors(
            classBitmapService.referenceVectorFor(class1),
            classBitmapService.referenceVectorFor(class2)
        );
    }

    private int countDistanceBetweenReferenceVectors(
        boolean[] rv1,
        boolean[] rv2
    ) {
        return classBitmapService.countDistanceBetweenVectors(rv1, rv2);
    }
}
