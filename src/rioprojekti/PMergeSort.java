package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;

public class PMergeSort implements RioSort {
    private static int INSERTION_SORT_THRESHOLD = 32;
    private long lastElapsedTime = 0;
    private long[] data, result;
    private int nThreads;
    private final Semaphore available;


    public PMergeSort(long[] A, int nThreads, int threshold) {
	this.nThreads = nThreads;
	available = new Semaphore(nThreads, true);
	data = A;
	INSERTION_SORT_THRESHOLD = threshold;
    }

    @Override
    public long getTimeInMilliseconds() {
	return lastElapsedTime;
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
    public void startSort() throws InterruptedException {
	ForkJoinPool pool = new ForkJoinPool(nThreads);
	if (isSorted(data)) {
	    System.out.println("Already sorted!");
	}

	long startTime = System.currentTimeMillis();

	//available.acquire();
	int s = 0;
	result = new long[s + data.length];
	//ParallelMergeSortTaskB task = new ParallelMergeSortTaskB(data, 0, data.length - 1, result, s);
	ParallelMergeSortTask task = new ParallelMergeSortTask(getData(), 0, getData().length - 1);
	pool.invoke(task);

	lastElapsedTime = System.currentTimeMillis() - startTime;
	if (isSorted(data)) {
	    System.out.println("Correctly sorted!");
	}
	else {
	    System.out.println("Parallel not sorted!");
	}
    }

    private class ParallelMergeSortTask extends RecursiveAction {

	private static final int INSERTION_SORT_THRESHOLD = 32;
	long[] data;
	int left, right;

	public ParallelMergeSortTask(long[] data, int left, int right) {
	    this.data = data;
	    this.left = left;
	    this.right = right;
	}
	
	@Override
	protected void compute() {
	     try {
		mergeSort(data, left, right);
	    }
	    catch (InterruptedException ex) {
		Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	private void mergeSort(long[] data, int left, int right) throws InterruptedException {
	    // Switch to insertion sort for small subarrays.
	    // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
	    if (right - left <= INSERTION_SORT_THRESHOLD) {
		for (int i = left + 1; i <= right; i++) {
		    long key = data[i];
		    int j = i - 1;
		    while (j >= left && data[j] > key) {
			data[j + 1] = data[j];
			j--;
		    }
		    data[j + 1] = key;
		}
	    }
	    else if (left < right) {
		int middle = (left + right) / 2;
		ParallelMergeSortTask task = new ParallelMergeSortTask(data, left, middle);
		ParallelMergeSortTask task2 = new ParallelMergeSortTask(data, middle + 1, right);
		invokeAll(task, task2);
		
		merge(data, left, middle, right);

	    }
	}

	
    }

    private class ParallelMergeSortTaskB extends Thread {

	long[] data, result;
	int left, right, s;

	public ParallelMergeSortTaskB(long[] data, int left, int right, long[] result, int s) {
	    this.data = data;
	    this.left = left;
	    this.right = right;
	    this.s = s;
	    this.result = result;
	}

	@Override
	public void run() {
	    try {
		mergeSort(data, left, right, result, s);
	    }
	    catch (InterruptedException ex) {
		Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	private void mergeSort(long[] data, int left, int right, long[] result, int s) throws InterruptedException {
	    // Switch to insertion sort for small subarrays.
	    // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
	    if (right - left <= INSERTION_SORT_THRESHOLD) {
		for (int i = left + 1; i <= right; i++) {
		    long key = data[i];
		    int j = i - 1;
		    while (j >= left && data[j] > key) {
			data[j + 1] = data[j];
			j--;
		    }
		    data[j + 1] = key;
		}
	    }
	    else {
		int n = right - left + 1; //number of elements in subarray data
		if (n == 1) {
		    result[0] = data[left];
		}
		else {
		    long[] T = new long[n];
		    int middle = (left + right) / 2;
		    int middleB = middle - left + 1;
		    if (available.tryAcquire()) {
			ParallelMergeSortTaskB taskA = new ParallelMergeSortTaskB(data, left, middle, T, 0);
			ParallelMergeSortTaskB taskB = new ParallelMergeSortTaskB(data, middle + 1, right, T, middleB + 1);
			taskA.start();
			taskB.start();

			//try
			//{
			taskA.join();
			taskB.join();
			available.release();
			//}
			//catch (InterruptedException ex)
			//{
			//   Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
			//}
		    }
		    else {
			mergeSort(data, left, middle, T, 0);
			mergeSort(data, middle + 1, right, T, middleB + 1);
		    }
		    ParallelMergeTask mergeTask = new ParallelMergeTask(T, 1, middleB, middleB + 1, n, result, s);
		    mergeTask.start();
		}
	    }
	}
    }

    private class ParallelMergeTask extends Thread {

	long[] T, A;
	int p1, r1, p2, r2, p3;

	ParallelMergeTask(long[] T, int p1, int r1, int p2, int r2, long[] A, int p3) {
	    this.p1 = p1;
	    this.r1 = r1;
	    this.p2 = p2;
	    this.r2 = r2;
	    this.p3 = p3;
	    this.A = A;
	    this.T = T;
	}

	private void merge(long[] T, int p1, int r1, int p2, int r2, long[] A, int p3) throws InterruptedException {
	    int n1 = r1 - p1 + 1;
	    int n2 = r2 - p2 + 1;
	    if (n1 < n2) {
		int dummy;
		dummy = p1;
		p1 = p2;
		p2 = dummy;

		dummy = r1;
		r1 = r2;
		r2 = r1;

		dummy = n1;
		n1 = n2;
		n2 = n1;
	    }
	    if (n1 == 0) {
		return;
	    }
	    else {
		int q1 = (p1 + r1) / 2;
		int q2 = binsearch(T[q1], T, p2, r2);
		int q3 = p3 + (q1 - p1) + (q2 - p2);
		A[q3] = T[q1];
		ParallelMergeTask taskA = null;
		if (available.tryAcquire()) {
		    taskA = new ParallelMergeTask(T, p1, q1 - 1, p2, q2 - 1, A, p3);
		    taskA.start();
		}
		else {
		    merge(T, p1, q1 - 1, p2, q2 - 1, A, p3);
		}
		merge(T, q1 + 1, r1, q2, r2, A, q3 + 1);
		if (taskA != null) {
		    taskA.join();
		    available.release();
		}
	    }
	}

	@Override
	public void run() {
	    try {
		merge(T, p1, r1, p2, r2, A, p3);
	    }
	    catch (InterruptedException ex) {
		Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	public int binsearch(long x, long[] T, int p, int r) {
	    int hi = T.length - 1;
	    int lo = p;
	    while (hi >= lo) {
		int guess = lo + ((hi - lo) / 2);
		if (T[guess] > x) {
		    hi = guess - 1;
		}
		else if (T[guess] < x) {
		    lo = guess + 1;
		}
		else {
		    return guess;
		}
	    }
	    /*
	     * int low = p; int high = Math.max(p, r + 1); while (low < high) {
	     * int mid = (low + high) / 2; if (x <= T[mid]) { high = mid; } else
	     * { low = mid + 1; } } return high;
	     *
	     */
	    return -1;
	}
    }

    private void merge(long[] data, int left, int middle, int right) {

	int a = middle - left + 1;
	int b = right - middle;

	long[] leftData = new long[a + 1];
	long[] rightData = new long[b + 1];

	for (int i = 0; i < a; i++) {
	    leftData[i] = data[left + i];
	}
	leftData[a] = Long.MAX_VALUE;

	for (int i = 0; i < b; i++) {
	    rightData[i] = data[middle + i + 1];
	}
	rightData[b] = Long.MAX_VALUE;

	int i = 0;
	int j = 0;

	for (int k = left; k <= right; k++) {
	    if (leftData[i] <= rightData[j]) {
		data[k] = leftData[i];
		i++;
	    }
	    else {
		data[k] = rightData[j];
		j++;
	    }

	}

    }

    /**
     * @return the data
     */
    public long[] getData() {
	return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(long[] data) {
	this.data = data;
    }
}
