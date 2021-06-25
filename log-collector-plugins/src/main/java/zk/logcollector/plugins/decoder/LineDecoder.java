package zk.logcollector.plugins.decoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import zk.logcollector.plugin.api.Decoder;
import zk.logcollector.plugin.api.LogRecord;
import zk.logcollector.plugin.api.PluginParameter;

/**
 * @author zoukang60456
 */
@Slf4j
public class LineDecoder implements Decoder<ByteBuf> {

    @PluginParameter(required = false)
    @Setter
    private int maxLength = 4096;

    @PluginParameter(required = false)
    @Setter
    private boolean stripDelimiter = true;

    private String delimiter = "\n";

    private String charsetName = "UTF-8";

    private transient Charset charset = StandardCharsets.UTF_8;

    private byte[] delimiterBytes = delimiter.getBytes(charset);

    private int delimiterLength = delimiterBytes.length;

    private boolean discarding;
    private int discardedBytes;
    private int offset;

    public LineDecoder() {
    }

    @PluginParameter(required = false)
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        delimiterBytes = delimiter.getBytes(charset);
        delimiterLength = delimiterBytes.length;
    }

    @PluginParameter(value = "charset", required = false)
    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        charset = Charset.forName(charsetName);
    }

    @Override
    public void start() {
        // 重建transient变量
        charset = Charset.forName(charsetName);
    }

    @Override
    public LogRecord decode(ByteBuf buffer) {
        ByteBuf byteBuf = doDecode(buffer);
        if (byteBuf == null) {
            return null;
        }
        String messageStr = byteBuf.toString(charset);
        byteBuf.release();
        LogRecord logRecord = new LogRecord();
        logRecord.setField(LogRecord.MESSAGE, messageStr);
        return logRecord;
    }

    protected ByteBuf doDecode(ByteBuf buffer) {
        final int eol = findEndOfLineByDelimiter(buffer);
        if (!discarding) {
            if (eol >= 0) {
                final ByteBuf frame;
                final int length = eol - buffer.readerIndex();
                if (length > maxLength) {
                    buffer.readerIndex(eol + delimiterLength);
                    fail(length);
                    return null;
                }

                if (stripDelimiter) {
                    frame = buffer.readRetainedSlice(length);
                    buffer.skipBytes(delimiterLength);
                } else {
                    frame = buffer.readRetainedSlice(length + delimiterLength);
                }
                return frame;
            } else {
                final int length = buffer.readableBytes();
                if (length > maxLength) {
                    discardedBytes = length;
                    buffer.readerIndex(buffer.writerIndex());
                    discarding = true;
                    offset = 0;
                    fail(length);
                }
                return null;
            }
        } else {
            if (eol >= 0) {
                final int length = discardedBytes + eol - buffer.readerIndex();
                buffer.readerIndex(eol + delimiterLength);
                discardedBytes = 0;
                discarding = false;
                fail(length);
            } else {
                discardedBytes += buffer.readableBytes();
                buffer.readerIndex(buffer.writerIndex());
                // We skip everything in the buffer, we need to set the offset to 0 again.
                offset = 0;
            }
            return null;
        }
    }

    private int findEndOfLineByDelimiter(final ByteBuf buffer) {
        int totalLength = buffer.readableBytes();
        int end = -1;
        int readerIndex = buffer.readerIndex();
        outer: for (int k = readerIndex + offset; k < totalLength + readerIndex; ++k) {
            for (int j = 0; j < delimiter.length(); ++j) {
                if (buffer.getByte(k + j) != delimiterBytes[j]) {
                    continue outer;
                }
            }
            end = k;
            break;
        }
        offset = end >= 0 ? 0 : totalLength;
        return end;
    }

    private void fail(int length) {
        log.error("frame length ({}) exceeds the allowed maximum ({})", length, maxLength);
    }

}
