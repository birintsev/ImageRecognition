package ua.edu.sumdu.elit.in71.birintsev;

import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;

// todo make the class immutable
@Getter
@Setter
public class ClassBitmap {

    private final RecognitionClass recognitionClass;

    private final int margin;

    private final boolean[][] bitmap;

    private final RecognitionClass baseClass;

    /**
     * This constructor should not be used directly.
     * Use
     * {@link ua.edu.sumdu.elit.in71.birintsev.services.ClassBitmapService#createFor(RecognitionClass, RecognitionClass, int)}
     * instead.
     * */
    public ClassBitmap(
        RecognitionClass recognitionClass,
        int margin,
        boolean[][] bitmap,
        RecognitionClass baseClass
    ) {
        this.recognitionClass = recognitionClass;
        this.margin = margin;
        this.bitmap = bitmap;
        this.baseClass = baseClass;
    }

    public RecognitionClass getRecognitionClass() {
        return recognitionClass;
    }

    public int getMargin() {
        return margin;
    }

    public boolean[][] getBitmap() {
        return bitmap;
    }

    public RecognitionClass getBaseClass() {
        return baseClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassBitmap that = (ClassBitmap) o;

        if (margin != that.margin) return false;
        if (!recognitionClass.equals(that.recognitionClass)) return false;
        if (!Arrays.deepEquals(bitmap, that.bitmap)) return false;
        return baseClass.equals(that.baseClass);
    }

    @Override
    public int hashCode() {
        int result = recognitionClass.hashCode();
        result = 31 * result + margin;
        result = 31 * result + Arrays.deepHashCode(bitmap);
        result = 31 * result + baseClass.hashCode();
        return result;
    }
}
