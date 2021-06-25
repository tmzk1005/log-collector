package zk.logcollector.plugins.input.tcp;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import zk.logcollector.plugin.api.Decoder;
import zk.logcollector.plugin.api.Input;
import zk.logcollector.plugin.api.LogRecord;
import zk.logcollector.plugin.api.PluginParameter;
import zk.logcollector.plugins.LogRecordQueue;
import zk.logcollector.plugins.decoder.LineDecoder;

/**
 * @author zoukang60456
 */
public class TcpInput implements Input {

    transient LogRecordQueue queue;
    private transient TcpServer tcpServer;
    @Getter
    @Setter
    @PluginParameter
    private int port;

    @Getter
    @Setter
    @PluginParameter
    private String clientIp;

    @Getter
    @Setter
    @PluginParameter(required = false)
    private Decoder<ByteBuf> decoder;

    @Override
    public LogRecord emit() {
        return queue.poll();
    }

    @Override
    public void start() {
        queue = new LogRecordQueue();
        if (Objects.isNull(decoder)) {
            decoder = new LineDecoder();
        }
        decoder.start();
        tcpServer = TcpServer.getOrCreateInstance(port);
        tcpServer.register(this);
    }

    @Override
    public void stop() {
        tcpServer.unregister(this);
    }

}

@Slf4j
class TcpServer extends AbstractService {

    private static final Map<Integer, TcpServer> INSTANCES = new ConcurrentHashMap<>();

    private final int port;

    private final TcpChannelInitializer channelInitializer;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private TcpServer(int port) {
        this.port = port;
        this.channelInitializer = new TcpChannelInitializer();
    }

    public synchronized static TcpServer getOrCreateInstance(int port) {
        if (INSTANCES.containsKey(port)) {
            return INSTANCES.get(port);
        }
        TcpServer tcpServer = new TcpServer(port);
        tcpServer.startAsync();
        tcpServer.awaitRunning();
        INSTANCES.put(port, tcpServer);
        return tcpServer;
    }

    @Override
    protected void doStart() {
        String bossThreadPrefix = "Netty-NioEventLoopGroup-boss-" + port;
        String workerThreadPrefix = "Netty-NioEventLoopGroup-worker-" + port;
        bossGroup = new NioEventLoopGroup(1, new ThreadPerTaskExecutor(new DefaultThreadFactory(bossThreadPrefix)));
        workerGroup = new NioEventLoopGroup(0, new ThreadPerTaskExecutor(new DefaultThreadFactory(workerThreadPrefix)));
        String host = "0.0.0.0";
        int soBacklog = 2048;
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_BACKLOG, soBacklog)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.bind(host, port).sync();
            log.info("Create a new TcpServer listen at {}:{}", host, port);
            notifyStarted();
        } catch (final InterruptedException ex) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            notifyFailed(ex);
        }
    }

    @Override
    protected void doStop() {
        Future<?> f1 = workerGroup.shutdownGracefully();
        Future<?> f2 = bossGroup.shutdownGracefully();
        int waitSeconds = 2;
        try {
            f1.await(waitSeconds, TimeUnit.SECONDS);
            f2.await(waitSeconds, TimeUnit.SECONDS);
            notifyStopped();
        } catch (InterruptedException e) {
            log.debug("TcpServer [port={}] was interrupted.", port, e);
            notifyFailed(e);
        }
    }

    public synchronized void register(TcpInput tcpInput) {
        this.channelInitializer.register(tcpInput);
    }

    public synchronized void unregister(TcpInput tcpInput) {
        this.channelInitializer.unregister(tcpInput);
        if (this.channelInitializer.isEmpty()) {
            log.info("No TcpInput is register under TCpServer [port={}], going to stop it.", port);
            stopAsync();
        }
        INSTANCES.remove(this.port);
    }
}

class TcpChannelInitializer extends ChannelInitializer<Channel> {

    private final Map<String, TcpInput> tcpInputs = new HashMap<>(2);

    boolean isEmpty() {
        return this.tcpInputs.isEmpty();
    }

    void register(TcpInput tcpInput) {
        String clientIp = tcpInput.getClientIp();
        tcpInputs.put(clientIp, tcpInput);
    }

    void unregister(TcpInput tcpInput) {
        tcpInputs.remove(tcpInput.getClientIp());
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        String clientIp = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        if (!tcpInputs.containsKey(clientIp)) {
            throw new Exception(clientIp + " is not allowed to send in logs, refuse connection from it.");
        }
        ChannelPipeline channelPipeline = channel.pipeline();
        TcpInput tcpInput = tcpInputs.get(clientIp);
        channelPipeline.addLast(new LogRecordDecoder(tcpInput.getDecoder(), tcpInput.queue));
    }

}

class LogRecordDecoder extends ByteToMessageDecoder {

    private final Decoder<ByteBuf> decoder;
    private final LogRecordQueue queue;

    LogRecordDecoder(Decoder<ByteBuf> decoder, LogRecordQueue queue) {
        this.decoder = decoder;
        this.queue = queue;
    }

    @Override
    protected void decode(
            ChannelHandlerContext channelHandlerContext,
            ByteBuf byteBuf,
            List<Object> list
    ) {
        LogRecord logRecord = decoder.decode(byteBuf);
        if (Objects.nonNull(logRecord)) {
            queue.add(logRecord);
        }
    }

}
