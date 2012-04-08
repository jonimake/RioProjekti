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

public class RioQuickSortImpl extends RioSort {

    private static final int INSERTION_SORT_THRESHOLD = 128;
    private final Semaphore available;

    public RioQuickSortImpl(long[] data, int nThreads, int threshold) {
        super(data, nThreads);
        available = new Semaphore(nThreads, true);
    }

    public static void serialQuicksort(long[] array, int leftIndex, int rightIndex) throws InterruptedException {
        // Switch to insertion sort for small subarrays.
        // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
        if (rightIndex - leftIndex <= INSERTION_SORT_THRESHOLD) {
            for (int i = leftIndex + 1; i <= rightIndex; i++) {
                long key = array[i];
                int j = i - 1;
                while (j >= leftIndex && array[j] > key) {
                    array[j + 1] = array[j];
                    j--;
                }
                array[j + 1] = key;
            }
        } else if (leftIndex < rightIndex) {
            int pivotIndex = leftIndex;

            int pivotNewIndex = partition(array, leftIndex, rightIndex, pivotIndex);

            serialQuicksort(array, leftIndex, pivotNewIndex - 1);
            serialQuicksort(array, pivotNewIndex + 1, rightIndex);
        }
    }

    public static int partition(long[] array, int leftIndex, int rightIndex, int pivotIndex) {
        long pivotValue = array[pivotIndex];
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

    private static void swap(long[] array, int pivotIndex, int rightIndex) {
        long dummy;

        dummy = array[pivotIndex];
        array[pivotIndex] = array[rightIndex];
        array[rightIndex] = dummy;
    }

    @Override
    public long[] doSort() {

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            available.acquire();
        } catch (InterruptedException ex) {
        }

        QuickSortTask task = new QuickSortTask(data, 0, data.length - 1);
        pool.invoke(task);
        //task.start();
        //task.join();
        available.release();
        return data;
    }

    private static class QuickSortTask extends RecursiveAction {

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
            } catch (InterruptedException ex) {
                Logger.getLogger(RioQuickSortImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void quicksort(long[] array, int leftIndex, int rightIndex) throws InterruptedException {
            // Switch to insertion sort for small subarrays.
            // Avoids cache misses caused by copying in merge, and avoids the recursion overhead
            if (rightIndex - leftIndex <= INSERTION_SORT_THRESHOLD) {
                for (int i = leftIndex + 1; i <= rightIndex; i++) {
                    long key = array[i];
                    int j = i - 1;
                    while (j >= leftIndex && array[j] > key) {
                        array[j + 1] = array[j];
                        j--;
                    }
                    array[j + 1] = key;
                }
            } else if (leftIndex < rightIndex) {
                int pivotIndex = leftIndex;

                int pivotNewIndex = partition(array, leftIndex, rightIndex, pivotIndex);

                QuickSortTask task = new QuickSortTask(array, leftIndex, pivotNewIndex - 1);
                QuickSortTask task2 = new QuickSortTask(array, pivotNewIndex + 1, rightIndex);

                invokeAll(task, task2);
            }
        }
    }
    /*
     * private class QuickSortTask extends Thread { private static final int INSERTION_SORT_THRESHOLD = 32; long[] data;
     * int low, high;
     *
     * public QuickSortTask(long[] list, int low, int high) { data = list; this.low = low; this.high = high; }
     *
     * @Override public void run() { try { quicksort(data, low, high); } catch (InterruptedException ex) {
     * Logger.getLogger(RioQuickSortImpl.class.getName()).log(Level.SEVERE, null, ex); } }
     *
     * public void quicksort(long[] array, int leftIndex, int rightIndex) throws InterruptedException { // Switch to
     * insertion sort for small subarrays. // Avoids cache misses caused by copying in merge, and avoids the recursion
     * overhead if (rightIndex - leftIndex <= INSERTION_SORT_THRESHOLD) { for (int i = leftIndex + 1; i <= rightIndex;
     * i++) { long key = array[i]; int j = i - 1; while (j >= leftIndex && array[j] > key) { array[j + 1] = array[j];
     * j--; } array[j + 1] = key; } } else if (leftIndex < rightIndex) { int pivotIndex = leftIndex;
     *
     * int pivotNewIndex = partition(array, leftIndex, rightIndex, pivotIndex);
     *
     * QuickSortTask task = null; if (available.tryAcquire()) { task = new QuickSortTask(array, leftIndex, pivotNewIndex
     * - 1); task.start(); } else quicksort(array, leftIndex, pivotNewIndex - 1);
     *
     * QuickSortTask taskB = null; if (false){//available.tryAcquire()) { taskB = new QuickSortTask(array, pivotNewIndex
     * + 1, rightIndex); taskB.start(); } else quicksort(array, pivotNewIndex + 1, rightIndex);
     *
     * if (task != null) { task.join(); available.release(); }
     *
     *
     *
     * }
     * }
     *
     * public int partition(long[] array, int leftIndex, int rightIndex, int pivotIndex) { long pivotValue =
     * array[pivotIndex]; swap(array, pivotIndex, rightIndex); //move pivot to end int storeIndex = leftIndex;
     *
     * for (int i = leftIndex; i < rightIndex; ++i) { if (array[i] < pivotValue) { swap(array, i, storeIndex);
     * storeIndex++; } } swap(array, storeIndex, rightIndex); //move pivot to its final place return storeIndex; }
     *
     * private void swap(long[] array, int pivotIndex, int rightIndex) { long dummy;
     *
     * dummy = array[pivotIndex]; array[pivotIndex] = array[rightIndex]; array[rightIndex] = dummy; } }
     */
}
