package system;

import framework.SinkFilterTemplate;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class JunkSinkFilter extends SinkFilterTemplate {

    public JunkSinkFilter(String filterId) {
        super(filterId);
    }

    @Override
    protected void writeByteToSink(byte dataByte) {
        System.out.println("Discarded byte: " + dataByte);
    }
}
