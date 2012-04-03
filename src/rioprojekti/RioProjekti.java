package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RioProjekti
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
	DataLoader dataLoader = new RandomDataLoader(10000000);
    //DataLoader dataLoader = new BinaryDataLoader("testdata/10000.bin");
	long[] data = null;
	
	long dataRead = System.currentTimeMillis();
	try
	{
	    data = dataLoader.readData();
	}
	catch (FileNotFoundException ex)
	{
	    Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	}
	catch (IOException ex)
	{
	    Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
	}

	System.out.println("Data read time: " + (System.currentTimeMillis() - dataRead) + " milliseconds");
	
	RioSort rioMerge = new PMergeSort(copyData(data));
	RioSort rioMerge2 = new RioMergeSortImpl(copyData(data));

	rioMerge2.startSort();
	System.out.println("Serial mergesort time: " + rioMerge2.getTimeInMilliseconds() + " milliseconds");
	rioMerge.startSort();
	

	
	System.out.println("Parallel mergesort time: " + rioMerge.getTimeInMilliseconds() + " milliseconds");
    }
    
    public static long[] copyData(long[] data)
    {
	long[] A = new long[data.length];
	for(int i = 0; i < data.length; ++i)
	    A[i] = data[i];
	return A;
    }

    
}
