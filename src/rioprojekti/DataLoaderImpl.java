package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonimake
 */
public class DataLoaderImpl implements DataLoader {

    private int size = 10000000;
    private String fileLocation = "testdata/10000.bin";
   // private String fileLocation = "testdata/allkeys.bin";

    @Override
    public long[] readData() throws FileNotFoundException, IOException {
	//RandomAccessFile file = new RandomAccessFile(fileLocation, "r");
	//size = (int) (file.length() / Long.SIZE);

	long[] data = new long[size];

	for (int i = 0; i < size; i++) {
	    data[i] = (long)(Math.random() * Long.MAX_VALUE);
	//    data[i] = file.readLong();
	}
	return data;
    }
}
