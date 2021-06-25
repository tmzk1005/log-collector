package zk.logcollector.etl.hazaealcast;

import java.util.List;
import java.util.Objects;

import com.hazelcast.function.BiConsumerEx;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.SinkBuilder;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;

import zk.logcollector.etl.LogSource;
import zk.logcollector.plugin.api.Extractor;
import zk.logcollector.plugin.api.Input;
import zk.logcollector.plugin.api.LogRecord;
import zk.logcollector.plugin.api.Output;

public class JetPipelineFactory {

    public static Pipeline fromLogSource(LogSource logSource) {
        String name = logSource.getName();
        Pipeline pipeline = Pipeline.create();
        StreamStage<LogRecord> streamStage = pipeline.readFrom(buildStreamSource(name, logSource.getInput()))
                .withoutTimestamps()
                .map(buildMapFunc(logSource.getExtractors()));
        for (Output output : logSource.getOutputs()) {
            streamStage.writeTo(buildSink(name, output));
        }
        return pipeline;
    }

    private static StreamSource<LogRecord> buildStreamSource(final String name, final Input input) {
        SourceBuilder<Input>.Stream<LogRecord> logRecordStream = SourceBuilder
                .stream(
                        name, (FunctionEx<Processor.Context, Input>) context -> {
                            input.start();
                            return input;
                        }
                )
                .fillBufferFn(
                        (BiConsumerEx<Input, SourceBuilder.SourceBuffer<LogRecord>>) (input1, logRecordSourceBuffer) -> {
                            LogRecord record = input1.emit();
                            if (Objects.isNull(record)) {
                                return;
                            }
                            logRecordSourceBuffer.add(record);
                        }
                )
                .destroyFn(Input::stop);
        if (input.supportDistributed()) {
            // 对于支持分布式的input，暂只支持在一个Node上运行一个input实例
            logRecordStream.distributed(1);
        }
        return logRecordStream.build();
    }

    private static FunctionEx<LogRecord, LogRecord> buildMapFunc(List<Extractor> extractors) {
        return logRecord -> {
            for (Extractor extractor : extractors) {
                logRecord = extractor.extract(logRecord);
            }
            return logRecord;
        };
    }

    private static Sink<LogRecord> buildSink(String name, final Output output) {
        return SinkBuilder
                .sinkBuilder(name, (FunctionEx<Processor.Context, Output>) context -> {
                    output.start();
                    return output;
                })
                .receiveFn((BiConsumerEx<Output, LogRecord>) Output::emit)
                .destroyFn(Output::stop)
                .build();
    }

}
