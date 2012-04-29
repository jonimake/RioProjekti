package rioprojekti;

public abstract class RioSort {

    protected long[] data;
    protected int numThreads;
    protected long lastElapsedTime;

    public long[] getData() {
        return data;
    }

    protected RioSort(long[] data, int numThreads) {
        this.data = data;
        this.numThreads = numThreads;
    }

    protected boolean isSorted(long[] arr) {
        for (int i = 0; i < arr.length - 1; ++i) {
            if (arr[i] > arr[i + 1])
                return false;
        }
        return true;
    }

    public void startSort() throws InterruptedException {

        long starttime = System.currentTimeMillis();
        long[] result = doSort();
        lastElapsedTime = System.currentTimeMillis() - starttime;

        if (isSorted(result))
            System.out.println("Correctly sorted!");
        else
            System.out.println("Incorrect!");
        data = null; // avoid keeping a reference to the huge array
    }

    protected abstract long[] doSort();

    public long getTimeInMilliseconds() {
        return lastElapsedTime;
    }

    public int getDepth() {
        // log2 of number of threads
        // http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
        return 31 - Integer.numberOfLeadingZeros(numThreads);
    }
}
