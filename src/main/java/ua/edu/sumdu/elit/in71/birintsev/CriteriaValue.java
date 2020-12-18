package ua.edu.sumdu.elit.in71.birintsev;

import lombok.Getter;
import lombok.Setter;
import ua.edu.sumdu.elit.in71.birintsev.services.criteria.CriteriaMethod;

@Getter
@Setter
public class CriteriaValue {

    private NeighbourClasses neighbourClasses;

    private int radius;

    /**
     * A criteria value for {@link ClassBitmap neighbourClasses.classBitmap}
     * with the selected margin and radius
     *
     * @see ua.edu.sumdu.elit.in71.birintsev.services.criteria.CriteriaMethod
     * @see ua.edu.sumdu.elit.in71.birintsev.services.criteria.CulbacCriteria
     * @see ua.edu.sumdu.elit.in71.birintsev.services.criteria.ShannonCriteria
     * */
    private double criteria;

    /**
     * A percentage for determining the workspace of
     * {@code neighbourClasses.classBitmap}.
     * <p>
     * In other words, it's guaranteed that the arithmetical mean
     * of the values below will be greater than or equal to this value.
     * <ul>
     *     <li> The percent of the {@code neighbourClasses.classBitmap}
     *          implementations that <strong>belong</strong>
     *          to the {@code neighbourClasses.classBitmap} hypersphere
     *          with a radius
     *     <li> The percent of the {@code neighbourClasses.neighbourClassBitmap}
     *          implementations that <strong>do not belong</strong>
     *          to the {@code neighbourClasses.classBitmap} hypersphere
     *          with a radius
     * */
    private double minDistinguishPercentage;

    /**
     * Marks if the {@link #criteria} belongs to the workspace
     * (i.e. the {@link ClassBitmap#getMargin margin}
     * and the radius are selected to
     * fit the {@link #minDistinguishPercentage}
     * */
    private boolean isWorkspace;

    private CriteriaMethod criteriaMethod;

    public CriteriaValue(
        int radius,
        NeighbourClasses neighbourClasses,
        double criteria,
        double minDistinguishPercentage,
        boolean isWorkspace,
        CriteriaMethod criteriaMethod
    ) {
        this.radius = radius;
        this.neighbourClasses = neighbourClasses;
        this.criteria = criteria;
        this.minDistinguishPercentage = minDistinguishPercentage;
        this.isWorkspace = isWorkspace;
        this.criteriaMethod = criteriaMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriteriaValue that = (CriteriaValue) o;

        if (radius != that.radius) return false;
        if (Double.compare(that.criteria, criteria) != 0) return false;
        if (Double.compare(that.minDistinguishPercentage, minDistinguishPercentage) != 0)
            return false;
        if (isWorkspace != that.isWorkspace) return false;
        return neighbourClasses.equals(that.neighbourClasses);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = neighbourClasses.hashCode();
        result = 31 * result + radius;
        temp = Double.doubleToLongBits(criteria);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minDistinguishPercentage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isWorkspace ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CriteriaValue{" +
            "criteria=" + criteria +
            '}';
    }
}
