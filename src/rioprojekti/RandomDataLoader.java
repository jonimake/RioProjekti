package rioprojekti;

import java.io.FileNotFoundException;
import java.io.IOException;

public class RandomDataLoader implements DataLoader {

    private int size;
    public RandomDataLoader(int s)
    {
        size = s;
    }

    @Override
    public long[] readData() throws FileNotFoundException, IOException {
	long[] data = new long[size];

	for (int i = 0; i < size; i++) {
	    data[i] = (long)(Math.random() * Long.MAX_VALUE);
	}
	return data;
    }
}
