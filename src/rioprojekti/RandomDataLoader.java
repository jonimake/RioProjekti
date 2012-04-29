package rioprojekti;

public class RandomDataLoader implements DataLoader {

    private int size;

    public RandomDataLoader(int s) {
        size = s;
    }

    @Override
    public long[] readData() throws Exception {
        long[] data = new long[size];

        for (int i = 0; i < size; i++) {
            data[i] = (long) (Math.random() * Long.MAX_VALUE);
        }
        return data;
    }
}
