package org.kraken.core.remoting.net;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.RemotingException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/12 15:47
 */
@Slf4j
public abstract class AbstractClient extends AbstractEndpoint {

    private InetSocketAddress connectAddress;
    private final Lock connectLock = new ReentrantLock();
    private URL url;
    public AbstractClient(URL url) {
        this.url = url;
        connectAddress = url.getInetAddress();
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    protected abstract void doConnect() throws Throwable;

    protected abstract void doConnect(InetSocketAddress address) throws Throwable;

    protected abstract void doDisConnect() throws Throwable;
    public abstract boolean isValidate();
    /**
     * Get the connected channel.
     * @return channel
     */
    public abstract Channel getChannel();
    public abstract boolean isConnect();

    public InetSocketAddress getConnectAddress() {
        return connectAddress;
    }
    public URL getUrl() {
        return url;
    }

    /*--------------------------- TODO 线程安全 ---------------------*/
    public void open() throws Throwable {
        // 若是已经连接上了，无需再次连接
        connectLock.lock();
        try {
            doOpen();
        } catch (Throwable e) {
            throw new RemotingException("failed to connect to server, cause by: ",e);
        } finally {
            connectLock.unlock();
        }
    }
    public void close() throws Throwable {

        // 若是还未连接上了，无法进行关闭操作
        if (!isConnect()) {
            log.info("failed to close connect, cause by not start the connect");
        }
        connectLock.lock();
        try {
            doClose();
            // 完成关闭操作之后还开启着连接，
            if (isConnect()) {
                log.info("failed to close the connect");
                throw new RemotingException("failed to close the connect");
            }
        } catch (Throwable e) {
            throw new RemotingException("failed to close the connect, cause by: ",e);
        } finally {
            connectLock.unlock();
        }


    }
    public void connect() throws Throwable {
        // 若是已经连接上了，无需再次连接
        if (isConnect()) {
            log.info("already connect, is no need to reconnect server address:" + getConnectAddress());
        }
        connectLock.lock();
        try {
            doConnect();
            if (!isConnect()) {
                log.info("failed to connect to server, try reconnect again， server address:" + getConnectAddress());
                throw new RemotingException("failed to connect to server");
            }
        } catch (Throwable e) {
            throw new RemotingException("failed to connect to server, cause by: ",e);
        } finally {
            connectLock.unlock();
        }
    }
    public void disConnect() throws Throwable {
        // 若是还未连接上了，无法进行关闭操作
        if (!isConnect()) {
            log.info("failed to close connect, cause by not start the connect");
        }
        connectLock.lock();
        try {
            doDisConnect();
            // 完成关闭操作之后还开启着连接，
            if (isConnect()) {
                log.info("failed to close the connect");
                throw new RemotingException("failed to close the connect");
            }
        } catch (Throwable e) {
            throw new RemotingException("failed to close the connect, cause by: ",e);
        } finally {
            connectLock.unlock();
        }
    }

    /*---------------------------- AbstractEndpoint --------------------------------*/

    /**
     * Send message by netty and whether to wait the completion of the send.
     *
     * @param message message that need send.
     * @param sent    whether to ack async-sent
     * @throws RemotingException throw RemotingException if wait until timeout or any exception thrown by method body that surrounded by try-catch.
     */
    @Override
    public void sent(Object message, boolean sent) {
        Channel channel = getChannel();
        sent(message, sent, channel);
    }


    @Override
    public void received(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void reconnect(InetSocketAddress address) throws RemotingException {
        connectLock.lock();
        try {
            doDisConnect();
            doConnect(address);
            // 完成关闭操作之后还开启着连接，
            if (!isConnect()) {
                throw new RemotingException("failed to reconnect the server :" + address);
            }
        } catch (Throwable e) {
            throw new RemotingException("failed to reconnect the server :" + address ,e);
        } finally {
            connectLock.unlock();
        }
    }

    private Callable<URL> callable;
    public void setCallable(Callable<URL> callable) {
        this.callable = callable;
    }

    /**
     * 在集群的url中选择一个
     * @return
     * @throws Exception
     */
    public URL selectClusterAddress() throws Exception {
        URL url = callable.call();
        this.url = url;
        this.connectAddress = url.getInetAddress();
        return url;
    }
}
