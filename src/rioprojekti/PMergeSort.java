package rioprojekti;

import java.util.logging.Level;
import java.util.logging.Logger;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;

public class PMergeSort extends RioSort {

    public PMergeSort(long[] A, int nThreads) {
        super(A, nThreads);
    }

    @Override
    public long[] doSort() {
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        long[] result = new long[data.length];
        ParallelMergeSortTask task = new ParallelMergeSortTask(data, 0, data.length - 1, result, 0, this.getDepth());
        pool.invoke(task);
        return result;

    }

    private static class ParallelMergeSortTask extends RecursiveAction {
        // Parallel mergesort: Introduction to algorithms, 3rd ed., p. 803
        private long[] data, result;
        private int left, right, s;
        private int maxDepth;

        public ParallelMergeSortTask(long[] data, int left, int right, long[] result, int s, int maxDepth) {
            this.data = data;
            this.left = left;
            this.right = right;
            this.s = s;
            this.result = result;
            this.maxDepth = maxDepth;
        }

        @Override
        public void compute() {
            try {
                mergeSort(data, left, right, result, s, maxDepth);
            } catch (InterruptedException ex) {
                Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void mergeSort(long[] data, int left, int right, long[] result, int s, int maxDepth) throws InterruptedException {
            //System.out.printf("> %d: mergesort(%d..%d) @ %d (%d)\n", Thread.currentThread().getId(), left, right, maxDepth, System.currentTimeMillis());
            // Switch to insertion sort for small subarrays.
            // Avoids cache misses caused by copying in merge, and avoids the recursion overhead

            int n = right - left + 1;
            if (maxDepth == 0) {
                System.arraycopy(data, left, result, s, n);
                RioQuickSortImpl.serialQuicksort(result, s, s + n - 1);
            } else {

                long[] T = new long[n];
                int middle = (left + right) / 2;
                int middleB = middle - left + 1;
                ParallelMergeSortTask taskA = new ParallelMergeSortTask(data, left, middle, T, 0, maxDepth - 1);
                ParallelMergeSortTask taskB = new ParallelMergeSortTask(data, middle + 1, right, T, middleB, maxDepth - 1);
                invokeAll(taskA, taskB);
                ParallelMergeTask mergeTask = new ParallelMergeTask(T, 0, middleB - 1, middleB, n - 1, result, s, maxDepth);
                invokeAll(mergeTask);
                //System.out.printf("< mergesort(%d..%d) @ %d (%d)\n", left, right, maxDepth, System.currentTimeMillis());
            }
        }
    }

    private static class ParallelMergeTask extends RecursiveAction {

        private long[] T, A;
        private int p1, r1, p2, r2, p3;
        private int maxDepth;

        ParallelMergeTask(long[] T, int p1, int r1, int p2, int r2, long[] A, int p3, int maxDepth) {
            this.p1 = p1;
            this.r1 = r1;
            this.p2 = p2;
            this.r2 = r2;
            this.p3 = p3;
            this.A = A;
            this.T = T;
            this.maxDepth = maxDepth;
        }

        private static void merge(long[] T, int p1, int r1, int p2, int r2, long[] A, int p3, int maxDepth) throws InterruptedException {
            //System.out.printf("> %d: merge(%d..%d, %d..%d) @ %d (%d)\n", Thread.currentThread().getId(), p1, r1, p2, r2, maxDepth, System.currentTimeMillis());

            // parallel merge T[p1..r1] and T[p2..r2] to A[p3..]
            if (maxDepth == 0) {
                serialMerge(T, p1, r1, p2, r2, A, p3);
                //System.out.printf("< merge(%d..%d, %d..%d) @ %d (%d)\n", p1, r1, p2, r2, maxDepth, System.currentTimeMillis());
                return;
            }

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
                    A, p3, maxDepth - 1);
            ParallelMergeTask taskB = new ParallelMergeTask(T,
                    first_index + 1, r1,
                    second_index, r2,
                    A, q3 + 1, maxDepth - 1);
            invokeAll(taskA, taskB);
            //System.out.printf("< merge(%d..%d, %d..%d) @ %d (%d)\n", p1, r1, p2, r2, maxDepth, System.currentTimeMillis());
        }

        @Override
        public void compute() {
            try {
                merge(T, p1, r1, p2, r2, A, p3, maxDepth);
            } catch (InterruptedException ex) {
                Logger.getLogger(PMergeSort.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public static int binsearch(long x, long[] T, int p, int r) {
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

    private static void serialMerge(long[] data, int p1, int r1, int p2, int r2, long[] dest, int d) {
        // merge data[p1..r1] and data[p2..r2] to dest[d..]
        while (p1 <= r1 && p2 <= r2) {
            if (data[p1] < data[p2])
                dest[d++] = data[p1++];
            else
                dest[d++] = data[p2++];
        }
        while (p1 <= r1)
            dest[d++] = data[p1++];
        while (p2 <= r2)
            dest[d++] = data[p2++];
    }
}
