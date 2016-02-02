package shared;

import framework.SinkFilterTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A sink filter that writes data out to a file.
 *
 * @since 1.0.0
 */
public class FileSinkFilter extends SinkFilterTemplate {

    /**
     * The print stream corresponding to the file to write.
     */
    private PrintStream out;

    /**
     * The cache that is used to cache up a line of data until they can be flushed to the file as a string.
     */
    private ByteArrayOutputStream cache = new ByteArrayOutputStream();

    /**
     * Default constructor. It first tests if the specified file exists or not. If it does not exist,
     * it will attempt to create it.
     *
     * @param filterId id for this filter.
     * @param fileName the absolute path of the file to be written.
     *
     * @throws IOException thrown when a file cannot be created.
     */
    public FileSinkFilter(final String filterId, final String fileName) throws IOException {
        super(filterId);
        File file = new File(fileName);
        if (!file.exists()) {
            boolean result = file.createNewFile();
            if (!result)
                throw new IOException("Failed to create file: " + fileName);
        }
        out = new PrintStream(fileName);
    }

    /**
     * In this method, the sink will attempt to cache up bytes of data that belongs to the same line. It will know
     * the end of line has reached when it has detected the new line character byte. Then it converts the cached bytes
     * to string and writes it out to the file. Finally, it clears the data cache and repeat the process.
     *
     * @param dataByte the data to be written to external resource.
     */
    @Override
    protected void writeByteToSink(byte dataByte) {
        cache.write(dataByte);
        if ("\n".getBytes()[0] == dataByte) {
            String line = new String(cache.toByteArray(), StandardCharsets.UTF_8);
            out.print(line);
            out.flush();
            cache.reset();
        }
    }
}
