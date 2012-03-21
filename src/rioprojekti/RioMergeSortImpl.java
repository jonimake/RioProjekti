package rioprojekti;

import java.util.List;
import java.util.concurrent.*;

/**
 *
 * @author jonimake
 */
public class RioMergeSortImpl implements RioSort {

    private long lastElapsedTime = 0;
    private List<Long> data;
    private DataLoader dataLoader = new DataLoaderImpl();
    
    @Override
    public void start() {
	data = dataLoader.readData();
	long startTime = System.currentTimeMillis();
	//do magic
	sort();
	lastElapsedTime = System.currentTimeMillis() - startTime;

    }

    @Override
    public long getTimeInMilliseconds() {
	return lastElapsedTime;
    }

    private void sort() {
	java.util.Collections.sort(data);
    }
}
