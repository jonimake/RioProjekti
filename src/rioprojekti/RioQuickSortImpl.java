package rioprojekti;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveAction;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;


/**
 *
 * @author jonimake
 */
public class RioQuickSortImpl implements RioSort {

    private static int INSERTION_SORT_THRESHOLD = 32;
    private long lastElapsedTime = 0;
    private long[] data, result;
    private int nThreads = Runtime.getRuntime().availableProcessors();
    private final Semaphore available;

    public RioQuickSortImpl(long[] data, int nThreads, int threshold) {
	this.data = data;
	this.nThreads = nThreads;
	available = new Semaphore(nThreads, true);
	INSERTION_SORT_THRESHOLD = threshold;
    }
    
    private boolean isSorted(long[] data) {
	for (int i = 0; i < data.length - 1; ++i) {
	    if (data[i] > data[i + 1]) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public long getTimeInMilliseconds() {
	return lastElapsedTime;
    }

    @Override
    public void startSort() throws InterruptedException {

	ForkJoinPool pool = new ForkJoinPool(nThreads);
	

	available.acquire();
	
	QuickSortTask task = new QuickSortTask(data, 0, data.length - 1);
	long starttime = System.currentTimeMillis();
	pool.invoke(task);
	//task.start();
	//task.join();
	available.release();

	lastElapsedTime = System.currentTimeMillis() - starttime;

	long lastval = Long.MIN_VALUE;

	if(isSorted(data))
	    System.out.println("Correctly sorted!");
	else 
	    System.out.println("Incorrect!");
	

    }
    private class QuickSortTask extends RecursiveAction {
	
	long[] data;
	int low, high;

	public QuickSortTask(long[] list, int low, int high) {
	    data = list;
	    this.low = low;
	    this.high = high;
	}
	
	@Override
	protected void compute() {
	    try {
		quicksort(data, low, high);
	    }
	    catch (InterruptedException ex) {
		Logger.getLogger(RioQuickSortImpl.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	/*
	public void run() {
	    try {
		quicksort(data, low, high);
	    }
	    catch (InterruptedException ex) {
		Logger.getLogger(RioQuickSortImpl.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
*/
	public void quicksort(long[] array, int leftIndex, int rightIndex) throws InterruptedException {
	    // Switch to insertion sort for small subarrays.
	    // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
	    if (rightIndex - leftIndex <= INSERTION_SORT_THRESHOLD)
	    {
		for (int i = leftIndex + 1; i <= rightIndex; i++)
		{
		    long key = array[i];
		    int j = i - 1;
		    while (j >= leftIndex && array[j] > key)
		    {
			array[j + 1] = array[j];
			j--;
		    }
		    array[j + 1] = key;
		}
	    }
	    else if (leftIndex < rightIndex) {
		int pivotIndex = leftIndex;
		
		int pivotNewIndex = partition(array, leftIndex, rightIndex, pivotIndex);
		
		QuickSortTask task = new QuickSortTask(array, leftIndex, pivotNewIndex - 1);;
		QuickSortTask task2 = new QuickSortTask(array, pivotNewIndex + 1, rightIndex);
		
		invokeAll(task, task2);
		/*if (available.tryAcquire()) {
		    
		    //task = new QuickSortTask(array, leftIndex, pivotNewIndex - 1);
		    //task.start();
		}
		else quicksort(array, leftIndex, pivotNewIndex - 1);
		
		QuickSortTask taskB = null;
		if (false){//available.tryAcquire()) {
		    taskB = new QuickSortTask(array, pivotNewIndex + 1, rightIndex);
		    taskB.start();
		}
		else quicksort(array, pivotNewIndex + 1, rightIndex);
		
		if (task != null) {
		    task.join();
		    available.release();
		}*/
		
		
		
	    }
	}

	public int partition(long[] array, int leftIndex, int rightIndex, int pivotIndex) {
	    long pivotValue =  array[pivotIndex];
	    swap(array, pivotIndex, rightIndex); //move pivot to end
	    int storeIndex = leftIndex;

	    for (int i = leftIndex; i < rightIndex; ++i) {
		if (array[i] < pivotValue) {
		    swap(array, i, storeIndex);
		    storeIndex++;
		}
	    }
	    swap(array, storeIndex, rightIndex); //move pivot to its final place
	    return storeIndex;
	}

	private void swap(long[] array, int pivotIndex, int rightIndex) {
	    long dummy;

	    dummy = array[pivotIndex];
	    array[pivotIndex] = array[rightIndex];
	    array[rightIndex] = dummy;
	}

	
    }
/*
    private class QuickSortTask extends Thread {
	private static final int INSERTION_SORT_THRESHOLD = 32;
	long[] data;
	int low, high;

	public QuickSortTask(long[] list, int low, int high) {
	    data = list;
	    this.low = low;
	    this.high = high;
	}

	@Override
	public void run() {
	    try {
		quicksort(data, low, high);
	    }
	    catch (InterruptedException ex) {
		Logger.getLogger(RioQuickSortImpl.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	public void quicksort(long[] array, int leftIndex, int rightIndex) throws InterruptedException {
	    // Switch to insertion sort for small subarrays.
	    // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
	    if (rightIndex - leftIndex <= INSERTION_SORT_THRESHOLD)
	    {
		for (int i = leftIndex + 1; i <= rightIndex; i++)
		{
		    long key = array[i];
		    int j = i - 1;
		    while (j >= leftIndex && array[j] > key)
		    {
			array[j + 1] = array[j];
			j--;
		    }
		    array[j + 1] = key;
		}
	    }
	    else if (leftIndex < rightIndex) {
		int pivotIndex = leftIndex;
		
		int pivotNewIndex = partition(array, leftIndex, rightIndex, pivotIndex);
		
		QuickSortTask task = null;
		if (available.tryAcquire()) {
		    task = new QuickSortTask(array, leftIndex, pivotNewIndex - 1);
		    task.start();
		}
		else quicksort(array, leftIndex, pivotNewIndex - 1);
		
		QuickSortTask taskB = null;
		if (false){//available.tryAcquire()) {
		    taskB = new QuickSortTask(array, pivotNewIndex + 1, rightIndex);
		    taskB.start();
		}
		else quicksort(array, pivotNewIndex + 1, rightIndex);
		
		if (task != null) {
		    task.join();
		    available.release();
		}
		
		
		
	    }
	}

	public int partition(long[] array, int leftIndex, int rightIndex, int pivotIndex) {
	    long pivotValue =  array[pivotIndex];
	    swap(array, pivotIndex, rightIndex); //move pivot to end
	    int storeIndex = leftIndex;

	    for (int i = leftIndex; i < rightIndex; ++i) {
		if (array[i] < pivotValue) {
		    swap(array, i, storeIndex);
		    storeIndex++;
		}
	    }
	    swap(array, storeIndex, rightIndex); //move pivot to its final place
	    return storeIndex;
	}

	private void swap(long[] array, int pivotIndex, int rightIndex) {
	    long dummy;

	    dummy = array[pivotIndex];
	    array[pivotIndex] = array[rightIndex];
	    array[rightIndex] = dummy;
	}
    }
    */
}
