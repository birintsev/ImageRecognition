package ua.edu.sumdu.elit.in71.birintsev;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

@AllArgsConstructor
@Getter
@Setter
public class RecognitionClass {

    /**
     * See <a href="https://stackoverflow.com/questions/13428689/whats-the-difference-between-cvtype-values-in-opencv">this Stackoverflow question</a>
     * for more information
     * */
    public static final int MAX_MARGIN = maxGrayscalePixelValue() / 2;

    private static final int CVTYPE = CvType.CV_8U;

    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = new HashSet<>(
        Arrays.asList("bmp", "jpg", "jpeg", "jpe", "png", "bpm")
    );

    static {
        loadOpenCVNativeLib();
    }

    /**
     * This method loads OpenCV library.
     *
     * Make sure, that this native library is available
     * during runtime ({@code java.library.path}
     * {@link System#getProperty(String) system property})
     * <p>
     * See how to use OpenCV in a java project
     * <a href="https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html">here</a>
     * */
    private static void loadOpenCVNativeLib() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static int maxGrayscalePixelValue() {
        int maxGrayscalePixelValue;
        switch (CVTYPE) {
            //noinspection ConstantConditions
            case CvType.CV_8U:
                maxGrayscalePixelValue = 0xFF;
                break;
            case CvType.CV_16U:
                maxGrayscalePixelValue = 0xFFFF;
                break;
            default:
                throw new IllegalArgumentException(
                    "CVTYPE must be equal to CvType.CV_8U or CvType.CV_16U"
                );
        }
        return maxGrayscalePixelValue;
    }

    private final File imageFile;

    private final Mat grayScaleImage;

    public RecognitionClass(String imageFilePath)
    throws FileNotFoundException{
        validatePreConstruct(imageFilePath);
        this.imageFile = new File(imageFilePath);
        grayScaleImage = Imgcodecs.imread(imageFilePath, CvType.CV_8U);
    }

    public File getImageFile() {
        return new File(imageFile.getAbsolutePath());
    }

    public int[][] getGrayScaleImage() {
        return matToInt(grayScaleImage);
    }

    private void validatePreConstruct(String imgFlPth)
    throws FileNotFoundException {
        File file = new File(imgFlPth);
        if (!file.isFile()) {
            throw new FileNotFoundException(imgFlPth + " is not a file");
        }
        if (!isImageFile(file)) {
            throw new IllegalArgumentException(
                "The file "
                    + imgFlPth
                    + " is not an image file." +
                    " Supported image file extensions are: "
                    + SUPPORTED_IMAGE_EXTENSIONS
            );
        }
    }

    private boolean isImageFile(File file) {
        return SUPPORTED_IMAGE_EXTENSIONS.contains(
            FilenameUtils.getExtension(file.getName()).toLowerCase()
        );
    }

    private Mat toGrayScale(Mat image) {
        Mat grayScale = new Mat();
        Imgproc.cvtColor(image, grayScale, CVTYPE);
        return grayScale;
    }

    private int[][] matToInt(Mat mat) {
        int[][] matrix = new int[mat.height()][mat.width()];
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                matrix[i][j] = (int) mat.get(i, j)[0];
            }
        }
        return matrix;
    }

    @Override
    public String toString() {
        return "RecognitionClass{" +
            "imageFile=" + imageFile +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecognitionClass that = (RecognitionClass) o;

        return imageFile.equals(that.imageFile);
    }

    @Override
    public int hashCode() {
        return imageFile.hashCode();
    }
}
