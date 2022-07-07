package org.kraken.core.common.utils;

import org.kraken.core.common.bean.URL;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/19 22:05
 */
public class NetUtils {

    private NetUtils(){}
    public static String toAddressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    public static InetSocketAddress toAddress(String address) {
        int i = address.indexOf(':');
        String host;
        int port;
        if (i > -1) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
            port = 0;
        }
        return new InetSocketAddress(host, port);
    }

    public static URL toUrl(String address) {
        InetSocketAddress socketAddress = toAddress(address);
        String host = socketAddress.getHostName();
        int port = socketAddress.getPort();
        return new URL(host, port);
    }



    /**
     * 查看本机某端口是否被占用
     * @param port  端口号
     * @return  如果被占用则返回true，否则返回false
     */
    public static boolean portIsUsed(int port){
        boolean flag = true;
        try{
            flag = isPortUsing("127.0.0.1", port);
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 根据IP和端口号，查询其是否被占用
     * @param host  IP
     * @param port  端口号
     * @return  如果被占用，返回true；否则返回false
     * @throws UnknownHostException    IP地址不通或错误，则会抛出此异常
     */
    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        try{
            Socket socket = new Socket(theAddress, port);
            flag = true;
        } catch (IOException e) {
            //如果所测试端口号没有被占用，那么会抛出异常，这里利用这个机制来判断
            //所以，这里在捕获异常后，什么也不用做
        }
        return flag;
    }

    public static List<URL> toUrls(List<String> addresses) {
        return addresses.stream()
                .map(NetUtils::toUrl)
                .collect(Collectors.toList());
    }
}
