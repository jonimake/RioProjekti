package rioprojekti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class BinaryDataLoader implements DataLoader {
    private String fileLocation;
    public BinaryDataLoader(String file) {
        fileLocation = file;
    }

    @Override
    public long[] readData() throws FileNotFoundException, IOException {
        File file = new File(fileLocation);
        long sizeInBytes = file.length();

        FileInputStream in = new FileInputStream(file);
        ByteBuffer buf = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, sizeInBytes);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long count = sizeInBytes / 8;
        long[] data = new long[(int)count];
        buf.asLongBuffer().get(data);

        in.close();
        return data;
    }
}
