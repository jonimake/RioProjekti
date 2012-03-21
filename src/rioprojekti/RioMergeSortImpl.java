package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonimake
 */
public class RioMergeSortImpl implements RioSort {

    private long lastElapsedTime = 0;
    private long[] data;
    private DataLoader dataLoader = new DataLoaderImpl();
    
    @Override
    public void start() {
	
	try {
	    data = dataLoader.readData();
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(RioMergeSortImpl.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex) {
	    Logger.getLogger(RioMergeSortImpl.class.getName()).log(Level.SEVERE, null, ex);
	}
	
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
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private class ThreadedMergeSort implements Runnable {

	@Override
	public void run() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
	
    }
}
