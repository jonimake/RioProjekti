package rioprojekti;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jonimake
 */
public class DataLoaderImpl implements DataLoader {
    
    private final int size = 10000;

    @Override
    public List<Long> readData() {
	
	List<Long> data = new ArrayList<Long>();
	
	for(int i = 0; i < size; i++)
	    data.add((long)Math.random()*size);
	
	return data;
    }
}
