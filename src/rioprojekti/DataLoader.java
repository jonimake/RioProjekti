package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author jonimake
 */
public interface DataLoader {

    long[] readData() throws Exception;
}
