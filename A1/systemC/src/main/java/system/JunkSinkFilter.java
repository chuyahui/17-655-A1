package system;

import framework.SinkFilterTemplate;

/**
 * Simple sink filter to just discard any received data
 *
 * @since 1.0.0
 */
public class JunkSinkFilter extends SinkFilterTemplate {

    public JunkSinkFilter(String filterId) {
        super(filterId);
    }

    /**
     * Print to console and not write to any external resource
     *
     * @param dataByte the data
     */
    @Override
    protected void writeByteToSink(byte dataByte) {
        System.out.println("Discarded byte: " + dataByte);
    }
}
