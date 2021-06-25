package zk.logcollector.plugins;

import java.util.AbstractQueue;
import java.util.Iterator;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.TimeoutException;
import lombok.extern.slf4j.Slf4j;

import zk.logcollector.plugin.api.LogRecord;

/**
 * @author zoukang
 */
@Slf4j
public class LogRecordQueue extends AbstractQueue<LogRecord> {

    private static final int SIZE = 2048;
    private static final EventTranslatorOneArg<LogRecord, LogRecord> TRANSLATOR = (event, sequence1, msg) -> event.fromAnother(msg);

    private final RingBuffer<LogRecord> ringBuffer;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private final SequenceBarrier sequenceBarrier;
    private long available = sequence.get();

    public LogRecordQueue() {
        ringBuffer = RingBuffer.createMultiProducer(LogRecord::new, SIZE);
        sequenceBarrier = ringBuffer.newBarrier();
        sequenceBarrier.clearAlert();
        ringBuffer.addGatingSequences(sequence);
    }

    @Override
    public boolean offer(LogRecord logRecord) {
        ringBuffer.publishEvent(TRANSLATOR, logRecord);
        return true;
    }

    @Override
    public LogRecord poll() {
        do {
            long next = sequence.get() + 1;
            while (next > available) {
                next = sequence.get() + 1;
                try {
                    available = sequenceBarrier.waitFor(next);
                } catch (AlertException | TimeoutException | InterruptedException e) {
                    log.warn("Exception happened while take from LogRecordQueue", e);
                }
            }
            LogRecord logRecord = ringBuffer.get(next);
            if (sequence.compareAndSet(next - 1, next)) {
                return logRecord;
            }
        } while (true);
    }

    @Override
    public Iterator<LogRecord> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LogRecord peek() {
        throw new UnsupportedOperationException();
    }

}
