/**
 * 
 */
package handemapper.opencv.loader;

import static org.junit.Assert.*;

import org.junit.Test;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * @author Chris Hartley
 *
 */
public class TestOpenCvLoader {

    /**
     * Test method for {@link handemapper.opencv.loader.OpenCvNativeLibrary#load()}.
     */
    @Test
    public final void testLoad() {
        if (OpenCvNativeLibrary.load()) {
            Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
            System.out.println("OpenCV Mat: " + m);
            
            Mat mr1 = m.row(1);
            mr1.setTo(new Scalar(1));
            Mat mc5 = m.col(5);
            mc5.setTo(new Scalar(5));
            System.out.println("OpenCV Mat data:\n" + m.dump());
            
            m.release();
            mr1.release();
            mc5.release();
        }
        else
            fail("OpenCV not loaded correctly!");
    }

}
