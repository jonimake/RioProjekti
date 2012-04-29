package rioprojekti;

public class RioMergeSortImpl extends RioSort {
    // Serial mergesort.

    public RioMergeSortImpl(long[] data, int numThreads) {
        super(data, numThreads);
    }

    @Override
    protected long[] doSort() {
        MergeSort merge = new MergeSort();
        merge.mergeSort(getData(), 0, getData().length - 1);
        return data;
    }

    private class MergeSort {

        private static final int INSERTION_SORT_THRESHOLD = 32;

        // http://www.cs.helsinki.fi/u/floreen/tira2012/tira.pdf, p. 360.
        // Insertion sort from p. 26
        public void mergeSort(long[] data, int left, int right) {
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
            } else {
                int middle = (left + right) / 2;
                mergeSort(data, left, middle);
                mergeSort(data, middle + 1, right);
                merge(data, left, middle, right);
            }
        }

        // http://www.cs.helsinki.fi/u/floreen/tira2012/tira.pdf, p. 364
        private void merge(long[] data, int left, int middle, int right) {
            if (data[middle] <= data[middle + 1]) // do nothing if the data is already sorted.
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
    }
}
