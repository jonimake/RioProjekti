package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PMergeSort implements RioSort
{

    private long lastElapsedTime = 0;
    private long[] data;

    private final int nThreads = Runtime.getRuntime().availableProcessors();
    private final int MAX_AVAILABLE = nThreads / 2; //jaetaan kahdella koska yksi semaforin varaus spawnaa kaksi threadia
    private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);

    public PMergeSort(long[] A)
    {
	data = A;
	System.out.println("Number of threads for parallel mergesort: "+ nThreads);
    }

    @Override
    public long getTimeInMilliseconds()
    {
	return lastElapsedTime;
    }

    private boolean isSorted()
    {
	for (int i = 0; i < data.length - 1; ++i)
	{
	    if (data[i] > data[i + 1])
	    {
		return false;
	    }
	}
	return true;
    }

    @Override
    public void startSort()
    {
	
	if (isSorted())
	{
	    System.out.println("Already sorted!");
	}

	long startTime = System.currentTimeMillis();
	try
	{
	    available.acquire();
	}
	catch (InterruptedException ex)
	{
	    Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	}
	ParallelMergeSortTask task = new ParallelMergeSortTask(getData(), 0, getData().length - 1);
	task.start();
	try
	{
	    task.join();
	}
	catch (InterruptedException ex)
	{
	    Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	}
	lastElapsedTime = System.currentTimeMillis() - startTime;
	if (isSorted())
	{
	    System.out.println("Correctly sorted!");
	}
	else
	{
	    System.out.println("Parallel not sorted!");
	    for (int i = 0; i < data.length; ++i)
	    {
		System.out.println(data[i]);
	    }
	}
    }

    private class ParallelMergeSortTask extends Thread
    {

	long[] data;
	int left, right;

	public ParallelMergeSortTask(long[] data, int left, int right)
	{
	    this.data = data;
	    this.left = left;
	    this.right = right;
	}

	@Override
	public void run()
	{
	    mergeSort(data, left, right);
	}

	private void mergeSort(long[] data, int left, int right)
	{
	    if (left < right)
	    {
		int middle = (left + right) / 2;
		if (available.tryAcquire()) //jos on jäljellä prosessoreja, muutoin serial mergesort
		{

		    ParallelMergeSortTask task = new ParallelMergeSortTask(data, left, middle);
		    task.start();
		    ParallelMergeSortTask task2 = new ParallelMergeSortTask(data, middle + 1, right);
		    task2.start();
		    try
		    {
			task.join();
			task2.join();
			available.release();
		    }
		    catch (InterruptedException ex)
		    {
			Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
		else
		{
		    mergeSort(data, left, middle);
		    mergeSort(data, middle + 1, right);
		}
		//ParallelMergeTask mergeTask = new ParallelMergeTask(data, 0, right,)
		merge(data, left, middle, right);

	    }
	    
	}
    }

    private class ParallelMergeTask extends Thread
    {

	long[] data, mergedArray;
	int leftA, rightA, leftB, rightB, mergedArrayLeft;

	public ParallelMergeTask(long[] data, int leftA, int rightA, int leftB, int rightB, long[] mergedArray, int mergedArrayLeft)
	{
	    this.data = data;
	    this.leftA = leftA;
	    this.rightA = rightA;
	    this.leftB = leftB;
	    this.rightB = rightB;
	    this.mergedArray = mergedArray;
	    this.mergedArrayLeft = mergedArrayLeft;
	}

	private void merge(long[] data, int leftA, int rightA, int leftB, int rightB, long[] mergedArray, int mergedArrayLeft)
	{
	    int lengthA = rightA - leftA + 1;
	    int lengthB = rightB - leftB + 1;
	    if (lengthA < lengthB)
	    {
		int dummy = leftA;
		leftA = leftB;
		leftB = dummy;

		dummy = rightA;
		rightA = leftA;
		leftA = dummy;

		dummy = lengthA;
		lengthA = lengthB;
		lengthB = dummy;
	    }
	    if (lengthA == 0) //tyhjä taulukko
	    {
		return;
	    }
	    else
	    {
		int medianA = (leftA + rightA) / 2; //ensimmäisen järjestetyn alitaulukon mediaani on keskellä taulukkoa
		int lowerThanMedianB = binSearch(data[medianA], data, leftB, rightB); //haetaan toisesta alitaulukosta mediaanin indeksi
		int mergedArrayMedianPos = mergedArrayLeft + (medianA - leftA) + (lowerThanMedianB - leftB);
		mergedArray[mergedArrayMedianPos] = data[medianA]; //siirretään mediaani yhdistettyyn taulukkoon

		ParallelMergeTask task = new ParallelMergeTask(data, leftA, medianA - 1, leftB, lowerThanMedianB - 1, mergedArray, mergedArrayLeft);
		task.start();
		merge(data, medianA + 1, rightA, lowerThanMedianB, rightB, mergedArray, mergedArrayMedianPos + 1);
		try
		{
		    task.join();
		}
		catch (InterruptedException ex)
		{
		    Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	}

	@Override
	public void run()
	{
	    merge(data, leftA, rightA, leftB, rightB, mergedArray, mergedArrayLeft);

	}

	private int binSearch(long medianA, long[] data, int leftB, int rightB)
	{
	    int low = leftB;
	    int high = Math.max(leftB, rightB + 1);
	    int mid;
	    while (low < high)
	    {
		mid = (low + high) / 2;
		if (medianA <= data[mid])
		{
		    high = mid;
		}
		else
		{
		    low = mid + 1;
		}
	    }
	    return high;
	}
    }

    private void merge(long[] data, int left, int middle, int right)
    {

	int a = middle - left + 1;
	int b = right - middle;

	long[] leftData = new long[a + 1];
	long[] rightData = new long[b + 1];

	for (int i = 0; i < a; i++)
	{
	    leftData[i] = data[left + i];
	}
	leftData[a] = Long.MAX_VALUE;

	for (int i = 0; i < b; i++)
	{
	    rightData[i] = data[middle + i + 1];
	}
	rightData[b] = Long.MAX_VALUE;

	int i = 0;
	int j = 0;

	for (int k = left; k <= right; k++)
	{
	    if (leftData[i] <= rightData[j])
	    {
		data[k] = leftData[i];
		i++;
	    }
	    else
	    {
		data[k] = rightData[j];
		j++;
	    }

	}

    }

    /**
     * @return the data
     */
    public long[] getData()
    {
	return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(long[] data)
    {
	this.data = data;
    }
}
