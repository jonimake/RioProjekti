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

    protected boolean isSorted() {
        for (int i = 0; i < data.length - 1; ++i) {
            if (data[i] > data[i + 1])
                return false;
        }
        return true;
    }

    public void startSort() throws InterruptedException {

        long starttime = System.currentTimeMillis();
        doSort();
        lastElapsedTime = System.currentTimeMillis() - starttime;

        if (isSorted())
            System.out.println("Correctly sorted!");
        else
            System.out.println("Incorrect!");
    }

    protected abstract void doSort();

    public long getTimeInMilliseconds() {
        return lastElapsedTime;
    }
}
