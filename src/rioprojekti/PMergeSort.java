package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;

public class PMergeSort extends RioSort {

    private static final int INSERTION_SORT_THRESHOLD = 32;

    public PMergeSort(long[] A, int nThreads) {
        super(A, nThreads);
    }

    @Override
    public long[] doSort() {
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        long[] result = new long[data.length];
        ParallelMergeSortTaskB task = new ParallelMergeSortTaskB(data, 0, data.length - 1, result, 0);
        pool.invoke(task);
        return result;

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
            } catch (InterruptedException ex) {
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
            } else if (left < right) {
                int middle = (left + right) / 2;
                ParallelMergeSortTask task = new ParallelMergeSortTask(data, left, middle);
                ParallelMergeSortTask task2 = new ParallelMergeSortTask(data, middle + 1, right);
                invokeAll(task, task2);

                merge(data, left, middle, right);

            }
        }
    }

    private class ParallelMergeSortTaskB extends RecursiveAction {

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
        public void compute() {
            try {
                mergeSort(data, left, right, result, s);
            } catch (InterruptedException ex) {
                Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void mergeSort(long[] data, int left, int right, long[] result, int s) throws InterruptedException {
            //System.out.printf("mergesort(%d..%d, %d)\n", left, right, s);
            // Switch to insertion sort for small subarrays.
            // Avoids cache misses caused by copying in merge, and avoids the recursion overhead

            int n = right - left + 1; //number of elements in subarray data
            if (n <= INSERTION_SORT_THRESHOLD) {
                for (int i = 0; i < n; i++) {
                    long key = data[left + i];
                    int j = i - 1;
                    while (j >= 0 && result[s + j] > key) {
                        result[s + j + 1] = result[s + j];
                        j--;
                    }
                    data[s + j + 1] = key;
                }
            } else {

                long[] T = new long[n];
                int middle = (left + right) / 2;
                int middleB = middle - left + 1;
                ParallelMergeSortTaskB taskA = new ParallelMergeSortTaskB(data, left, middle, T, 0);
                ParallelMergeSortTaskB taskB = new ParallelMergeSortTaskB(data, middle + 1, right, T, middleB);
                invokeAll(taskA, taskB);
                //taskA.compute();
                //taskB.compute();
                ParallelMergeTask mergeTask = new ParallelMergeTask(T, 0, middleB - 1, middleB, n - 1, result, s);
                mergeTask.compute();
            }
        }
    }

    private class ParallelMergeTask extends RecursiveAction {

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
            //System.out.println(String.format("merge(%d..%d, %d..%d, %d)", p1, r1, p2, r2, p3));
            // parallel merge T[p1..r1] and T[p2..r2] to A[p3..]
            // TODO: this needs a check for too small subarrays
            int n1 = r1 - p1 + 1;
            int n2 = r2 - p2 + 1;
            if (n1 < n2) { // swap p, r
                int pt = p1;
                int rt = r1;

                p1 = p2;
                r1 = r2;

                p2 = pt;
                r2 = rt;
                n1 = n2;
            }
            if (n1 == 0)
                return;
            int first_index = (p1 + r1) / 2;
            long median = T[first_index];
            int second_index = binsearch(median, T, p2, r2);

            int q3 = p3 + (first_index - p1) + (second_index - p2);
            A[q3] = T[first_index];
            ParallelMergeTask taskA = new ParallelMergeTask(T,
                    p1, first_index - 1,
                    p2, second_index - 1,
                    A, p3);
            taskA.fork();
            new ParallelMergeTask(T,
                    first_index + 1, r1,
                    second_index, r2,
                    A, q3 + 1).compute();
            taskA.join();
        }

        @Override
        public void compute() {
            try {
                merge(T, p1, r1, p2, r2, A, p3);
            } catch (InterruptedException ex) {
                Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public int binsearch(long x, long[] T, int p, int r) {
            int hi = Math.max(p, r + 1);
            int lo = p;
            while (lo < hi) {
                int mid = (hi + lo) / 2;
                if (x <= T[mid]) {
                    hi = mid;
                } else if (T[mid] < x) {
                    lo = mid + 1;
                } else {
                    return mid;
                }
            }
            return hi;
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
            } else {
                data[k] = rightData[j];
                j++;
            }

        }

    }
}
