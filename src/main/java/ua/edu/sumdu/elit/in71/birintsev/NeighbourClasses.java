package ua.edu.sumdu.elit.in71.birintsev;

import lombok.Getter;
import lombok.Setter;

// todo make the class immutable
@Getter
@Setter
public class NeighbourClasses {

    private final ClassBitmap classBitmap;

    private final ClassBitmap neighbourClassBitmap;

    /**
     * A distance between
     * the {@link #classBitmap} and {@link #neighbourClassBitmap}
     * in Hamming space (binary space)
     * */
    private final int distance;

    /**
     * This constructor should not be used directly.
     * Use {@link }
     * */
    public NeighbourClasses(
        ClassBitmap classBitmap,
        ClassBitmap neighbourClassBitmap,
        int distance
    ) {
        this.classBitmap = classBitmap;
        this.neighbourClassBitmap = neighbourClassBitmap;
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public ClassBitmap getClassBitmap() {
        return classBitmap;
    }

    public ClassBitmap getNeighbourClassBitmap() {
        return neighbourClassBitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeighbourClasses that = (NeighbourClasses) o;

        if (distance != that.distance) return false;
        if (!classBitmap.equals(that.classBitmap)) return false;
        return neighbourClassBitmap.equals(that.neighbourClassBitmap);
    }

    @Override
    public int hashCode() {
        int result = classBitmap.hashCode();
        result = 31 * result + neighbourClassBitmap.hashCode();
        result = 31 * result + distance;
        return result;
    }
}
