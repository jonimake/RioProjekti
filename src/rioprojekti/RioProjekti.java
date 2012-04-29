package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;

public class RioProjekti {

    public static void main(String[] args) throws Exception {
        String filelocation = "testdata/allkeys.bin";
        if (args.length > 0)
            filelocation = args[0];

        int nThreads = Runtime.getRuntime().availableProcessors();
        if (args.length > 1)
            nThreads = Integer.parseInt(args[1]);
        System.out.println("Threads: " + nThreads);

        DataLoader dataLoader = new BinaryDataLoader(filelocation);
        long[] data;

        long dataRead = System.currentTimeMillis();
        try {
            data = dataLoader.readData();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            data = new RandomDataLoader(10000000).readData();
        } catch (IOException ex) {
            System.out.println(ex);
            return;
        }
        System.out.println("Data read time: " + (System.currentTimeMillis() - dataRead) + " milliseconds");

        RioSort rioMerge = new PMergeSort(data, nThreads);
        rioMerge.startSort();
        System.out.println("Parallel mergesort time: " + rioMerge.getTimeInMilliseconds() + " milliseconds");

        RioSort rioMerge2 = new RioMergeSortImpl(copyData(data), nThreads);
        rioMerge2.startSort();

        System.out.println(
                "Serial mergesort time: " + rioMerge2.getTimeInMilliseconds() + " milliseconds");
        System.out.println(
                "Mergesort speedup: " + ((double) rioMerge2.getTimeInMilliseconds()
                / rioMerge.getTimeInMilliseconds()));
        System.gc();

        long javasort = System.currentTimeMillis();

        java.util.Arrays.sort(copyData(data));

        System.out.println("Java array sort time: " + (System.currentTimeMillis() - javasort));
        System.gc();

        RioSort quick = new RioQuickSortImpl(copyData(data), nThreads);
        quick.startSort();

        System.out.println(
                "Parallel quicksort time: " + quick.getTimeInMilliseconds() + " milliseconds");
        System.gc();
        RioSort quickSerial = new RioQuickSortImpl(copyData(data), 1);

        quickSerial.startSort();

        System.out.println(
                "Serial quicksort time: " + quickSerial.getTimeInMilliseconds() + " milliseconds");
        System.out.println(
                "Quicksort speedup: " + ((double) quickSerial.getTimeInMilliseconds()
                / quick.getTimeInMilliseconds()));
        System.gc();
    }

    public static long[] copyData(long[] data) {
        long[] A = new long[data.length];
        for (int i = 0; i < data.length; ++i)
            A[i] = data[i];
        return A;
    }
}
