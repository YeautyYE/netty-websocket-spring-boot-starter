package org.yeauty.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/26 14:43
 */
public class Session {

    private final Channel channel;

    public Session(Channel channel) {
        this.channel = channel;
    }

    public ChannelFuture sendText(String message) {
        return channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    public ChannelFuture sendText(ByteBuf byteBuf) {
        return channel.writeAndFlush(new TextWebSocketFrame(byteBuf));
    }

    public ChannelFuture sendText(ByteBuffer byteBuffer) {
        ByteBuf buffer = channel.alloc().buffer(byteBuffer.remaining());
        return channel.writeAndFlush(new TextWebSocketFrame(buffer));
    }

    public ChannelFuture sendText(TextWebSocketFrame textWebSocketFrame) {
        return channel.writeAndFlush(textWebSocketFrame);
    }

    public ChannelFuture sendBinary(byte[] bytes) {
        ByteBuf buffer = channel.alloc().buffer(bytes.length);
        return channel.writeAndFlush(new BinaryWebSocketFrame(buffer.writeBytes(bytes)));
    }

    public ChannelFuture sendBinary(ByteBuf byteBuf) {
        return channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }

    public ChannelFuture sendBinary(ByteBuffer byteBuffer) {
        ByteBuf buffer = channel.alloc().buffer(byteBuffer.remaining());
        return channel.writeAndFlush(new BinaryWebSocketFrame(buffer));
    }

    public ChannelFuture sendBinary(BinaryWebSocketFrame binaryWebSocketFrame) {
        return channel.writeAndFlush(binaryWebSocketFrame);
    }

    public Channel channel() {
        return channel;
    }

    /**
     * Returns the globally unique identifier of this {@link Channel}.
     */
    public ChannelId id() {
        return channel.id();
    }

    /**
     * Returns the configuration of this channel.
     */
    public ChannelConfig config() {
        return channel.config();
    }

    /**
     * Returns {@code true} if the {@link Channel} is open and may get active later
     */
    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * Returns {@code true} if the {@link Channel} is registered with an {@link EventLoop}.
     */
    public boolean isRegistered() {
        return channel.isRegistered();
    }

    /**
     * Return {@code true} if the {@link Channel} is active and so connected.
     */
    public boolean isActive() {
        return channel.isActive();
    }

    /**
     * Return the {@link ChannelMetadata} of the {@link Channel} which describe the nature of the {@link Channel}.
     */
    public ChannelMetadata metadata() {
        return channel.metadata();
    }

    /**
     * Returns the local address where this channel is bound to.  The returned
     * {@link SocketAddress} is supposed to be down-cast into more concrete
     * type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     *
     * @return the local address of this channel.
     * {@code null} if this channel is not bound.
     */
    public SocketAddress localAddress() {
        return channel.localAddress();
    }

    /**
     * Returns the remote address where this channel is connected to.  The
     * returned {@link SocketAddress} is supposed to be down-cast into more
     * concrete type such as {@link InetSocketAddress} to retrieve the detailed
     * information.
     *
     * @return the remote address of this channel.
     * {@code null} if this channel is not connected.
     * If this channel is not connected but it can receive messages
     * from arbitrary remote addresses (e.g. {@link DatagramChannel},
     * use {@link DatagramPacket#recipient()} to determine
     * the origination of the received message as this method will
     * return {@code null}.
     */
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    /**
     * Returns the {@link ChannelFuture} which will be notified when this
     * channel is closed.  This method always returns the same future instance.
     */
    public ChannelFuture closeFuture() {
        return channel.closeFuture();
    }

    /**
     * Returns {@code true} if and only if the I/O thread will perform the
     * requested write operation immediately.  Any write requests made when
     * this method returns {@code false} are queued until the I/O thread is
     * ready to process the queued write requests.
     */
    public boolean isWritable() {
        return channel.isWritable();
    }

    /**
     * Get how many bytes can be written until {@link #isWritable()} returns {@code false}.
     * This quantity will always be non-negative. If {@link #isWritable()} is {@code false} then 0.
     */
    public long bytesBeforeUnwritable() {
        return channel.bytesBeforeUnwritable();
    }

    /**
     * Get how many bytes must be drained from underlying buffers until {@link #isWritable()} returns {@code true}.
     * This quantity will always be non-negative. If {@link #isWritable()} is {@code true} then 0.
     */
    public long bytesBeforeWritable() {
        return channel.bytesBeforeWritable();
    }

    /**
     * Returns an <em>internal-use-only</em> object that provides unsafe operations.
     */
    public Channel.Unsafe unsafe() {
        return channel.unsafe();
    }

    /**
     * Return the assigned {@link ChannelPipeline}.
     */
    public ChannelPipeline pipeline() {
        return channel.pipeline();
    }

    /**
     * Return the assigned {@link ByteBufAllocator} which will be used to allocate {@link ByteBuf}s.
     */
    public ByteBufAllocator alloc() {
        return channel.alloc();
    }

    public Channel read() {
        return channel.read();
    }

    public Channel flush() {
        return channel.flush();
    }

    public ChannelFuture close() {
        return channel.close();
    }

    public ChannelFuture close(ChannelPromise promise) {
        return channel.close(promise);
    }

}
