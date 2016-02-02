package shared;

import framework.SourceFilterTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A source filter that reads from a file. The file must be specified as an absolute path.
 *
 * @since 1.0.0
 */
public class FileSourceFilter extends SourceFilterTemplate {

    /**
     * The input stream corresponding to the file to read data from.
     */
    private InputStream in;

    /**
     * Default constructor. It will attempt to open the file input stream for the file specified.
     *
     * @param filterId id for this filter
     * @param fileName the absolute file path for the file to be read
     *
     * @throws FileNotFoundException thrown when file cannot be found.
     */
    public FileSourceFilter(final String filterId, final String fileName) throws FileNotFoundException {
        super(filterId);
        in = new FileInputStream(fileName);
    }

    /**
     * read a byte of data from the file. throw an {@link RuntimeException} if {@link IOException} occurs
     * while reading the file.
     *
     * @return byte data read from file.
     */
    @Override
    protected byte readOneByte() {
        try {
            return (byte) in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Whether the file input stream has reached to an end.
     *
     * @return true if there's no more data in the file, false if there's still data to be read.
     */
    @Override
    protected boolean hasReachedEndOfStream() {
        try {
            return in.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }
}
