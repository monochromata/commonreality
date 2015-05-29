package org.commonreality.netty.service;

/*
 * default logging
 */
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.netty.impl.NettyListener;
import org.commonreality.netty.impl.NettyMultiplexer;

public class ServerService extends AbstractNettyNetworkService implements
    IServerService
{
  /**
   * Logger definition
   */
  static final transient Log LOGGER = LogFactory.getLog(ServerService.class);

  private EventLoopGroup     _serverGroup;

  private ServerBootstrap    _bootstrap;

  private ServerChannel      _serverChannel;

  @SuppressWarnings("unchecked")
  @Override
  public void configure(ITransportProvider transport,
      IProtocolConfiguration protocol,
      Map<Class<?>, IMessageHandler<?>> defaultHandlers,
      ISessionListener defaultListener, ThreadFactory threadFactory)
  {

    _multiplexer = createMultiplexer(defaultHandlers);

    _serverGroup = createServerGroup(threadFactory);
    _workerGroup = createWorkerGroup(4, threadFactory);

    _bootstrap = new ServerBootstrap();
    try
    {
      _bootstrap
          .group(_serverGroup, _workerGroup)
          .channel((Class<? extends ServerChannel>) transport.configureServer())
          .handler(new ChannelInitializer<ServerChannel>() {

            @Override
            protected void initChannel(ServerChannel arg0)
                throws Exception
            {
              if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("server socket opened"));
              _serverChannel = arg0;
            }
          })
          .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception
            {
              if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("New connection received"));

              protocol.configure(ch);

              if (defaultListener != null)
                ch.pipeline().addLast("defaultListener",
                    new NettyListener(defaultListener));

              /*
               * add our multiplexer
               */
              ch.pipeline().addLast(_multiplexer.getClass().getName(),
                  new NettyMultiplexer(_multiplexer));
            }
          });
      // .option(ChannelOption.SO_BACKLOG, 128)
      // .childOption(ChannelOption.SO_KEEPALIVE, true);

    }
    catch (Exception e)
    {
      LOGGER.error("Failed to configure server service", e);
    }
  }

  @Override
  public SocketAddress start(SocketAddress address) throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Binding"));

    ChannelFuture future = _bootstrap.bind(address).sync();
    future.awaitUninterruptibly();
    return future.channel().localAddress();
  }

  @Override
  public void stop(SocketAddress address) throws Exception
  {
    _serverChannel.close().awaitUninterruptibly();
    _serverGroup.shutdownGracefully();
    _workerGroup.shutdownGracefully();
  }

}