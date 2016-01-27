package systemA;

import framework.SinkFilterTemplate;

import java.io.*;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class FileSinkFilter extends SinkFilterTemplate {

    private OutputStream out;

    public FileSinkFilter(final String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            boolean result = file.createNewFile();
            if (!result)
                throw new IOException("Failed to create file: " + fileName);
        }
        out = new FileOutputStream(fileName);
    }

    @Override
    protected void writeByteToSink(byte dataByte) {
        try {
            out.write(dataByte);
            out.flush();
        } catch (IOException e) {
            System.out.println("IOException occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
