package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RioMergeSortImpl implements RioSort {

    private long lastElapsedTime = 0;
    private long[] data;
    private DataLoader dataLoader = new RandomDataLoader(10000000);
    
    public RioMergeSortImpl(long[] A)
    {
	data = A;
    }

    @Override
    public void startSort() {
	
	if(isSorted())
	    System.out.println("Already sorted!");
	
	MergeSort merge = new MergeSort(getData());

	long startTime = System.currentTimeMillis();
	//do magic
	merge.mergeSort(getData(), 0, getData().length - 1);
	lastElapsedTime = System.currentTimeMillis() - startTime;
	if(isSorted())
	    System.out.println("Correctly sorted!");
    else
        System.out.println("Incorrectly sorted!");

    }

    private boolean isSorted()
    {
	for(int i = 0; i < data.length-1; ++i)
	{
	    if(data[i] > data[i+1])
		return false;
	}
	return true;
    }

    @Override
    public long getTimeInMilliseconds() {
	return lastElapsedTime;
    }

    public long[] getData()
    {
	return data;
    }

    private class MergeSort {

	private long[] data;

	public MergeSort(long[] data) {
	    this.data = data;
	}

    private static final int INSERTION_SORT_THRESHOLD = 32;
	public void mergeSort(long[] data, int left, int right) {
        // Switch to insertion sort for small subarrays.
        // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
	    if (right - left <= INSERTION_SORT_THRESHOLD) {
            for(int i = left + 1; i <= right; i++)
            {
                long key = data[i];
                int j = i - 1;
                while(j >= left && data[j] > key)
                {
                    data[j + 1] = data[j];
                    j--;
                }
                data[j + 1] = key;
            }
        }
        else
        {
            int middle = (left + right) / 2;
            mergeSort(data, left, middle);
            mergeSort(data, middle + 1, right);
            merge(data, left, middle, right);
        }
	}

	private void merge(long[] data, int left, int middle, int right) {
        if(data[middle] <= data[middle + 1]) // do nothing if the data is already sorted.
            return;

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
		} else {
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
}
