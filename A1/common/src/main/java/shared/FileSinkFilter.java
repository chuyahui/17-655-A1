package shared;

import framework.SinkFilterTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class FileSinkFilter extends SinkFilterTemplate {

    private PrintStream out;
    private ByteArrayOutputStream cache = new ByteArrayOutputStream();

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
