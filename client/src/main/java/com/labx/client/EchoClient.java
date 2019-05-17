/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.labx.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public final class EchoClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private ConcurrentHashMap<String, Channel> channels;

    public EchoClient() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        channels = new ConcurrentHashMap<String, Channel>();

        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
//                    p.addLast(new LoggingHandler(LogLevel.INFO));
                    p.addLast(new LineBasedFrameDecoder(2048, false, true));
                    p.addLast(new StringDecoder());
                    p.addLast(new StringEncoder());
                    p.addLast(new EchoClientHandler());
                }
            });
    }

    public Channel getChannel(String address) {
        Channel channel = channels.get(address);

        if (null != channel) {
            if (channel.isActive()) {
                return channel;
            } else {
                channels.remove(address);
            }
        }

        String[] segments = address.split(":");

        try {
            ChannelFuture future = bootstrap.connect(segments[0], Integer.parseInt(segments[1])).sync();
            if (future.await(3000)) {
                if (null == channels.putIfAbsent(address, future.channel())) {
                    return future.channel();
                } else {
                    future.channel().close();
                    return channels.get(address);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // connect failed.
        return null;
    }

    public void shutdown() {
        for (ConcurrentMap.Entry<String, Channel> entry : channels.entrySet()) {
            try {
                entry.getValue().close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        group.shutdownGracefully();
    }

    public void ping(String content) throws InterruptedException {
        Channel channel = getChannel(HOST + ":" + PORT);
        if (null != channel) {
            channel.writeAndFlush(content + "\r\n").sync();
        }
    }
}
