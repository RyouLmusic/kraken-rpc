package org.kraken.core.common.bean;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/27 15:02
 */
public final class Constants {
    /**
     * 配置文件的默认位置
     */
    public final static String CONFIGURATION_DEFAULT_PATH = "HRpcConfig.properties";


    /**
     * ZK默认的地址
     */
    private final static String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    /**
     * ZK注册中心的根节点路径
     */
    public static final String ZK_REGISTER_ROOT_PATH = "/kraken";
    /**
     * 实现的注册中心的协议开头
     */
    public static final String ZK_PROTOCOL_HEAD = "zk:/";

    /**
     * ZK连接超时时间(单位:毫秒)
     */
    public final static int ZK_CONNECTION_TIME_OUT_MS = 10 * 1000;
    /**
     * ZK会话超时时间(单位:毫秒)
     */
    public final static int ZK_SESSION_TIME_OUT_MS = 30 * 1000;
    /**
     * ZK重试的初始等待时间(单位:毫秒)
     */
    public final static int ZK_BASE_SLEEP_TIME_MS = 2 * 1000;
    /**
     * ZK最大重试次数
     */
    public final static int ZK_MAX_RETRIES = 3;
    /**
     * zk节点分隔符
     */
    public final static String SEPARATOR = "/";

    /**
     * 默认线程数 = cpu核数 * 2 +1
     */
    public final static int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2 + 1;


    /**
     * 解码: 最大的长度，如果超过，会直接丢弃
     */
    public final static int MAX_FRAME_LENGTH = 1024 * 1024;

    public final static int LENGTH_FIELD_OFFSET = 16;

    public final static int LENGTH_FIELD_LENGTH = 4;

    public final static int LENGTH_ADJUSTMENT = 0;
    public final static int INITIAL_BYTES_TO_STRIP = 0;



    /**
     * 魔数
     * MAGIC_NUMBER_
     */
    public final static byte MAGIC_NUMBER_1 = (byte) 0x81;
    public final static byte MAGIC_NUMBER_2 = (byte) 0x82;

    /**
     * 魔数长度
     */
    public final static int MAGIC_LENGTH = 1;

    public static final int HEADER_SIZE = 20;

    public final static byte H_RPC_VERSION = (byte) 1;


    public final static String ENDPOINT_KEY = "ENDPOINT_KEY";

    /*-----------------referenceConfig, methodConfigMap 默认值-----------*/

    /**
     * 活跃数
     */
    public static final int ACTIVE = 20;
    /**
     * timeout: 一个接口在执行远程调用的时候可以接受的 超时时间
     */
    public static final long TIMEOUT = 100;
    /**
     * 请求的接口版本
     */
    public static final String VERSION = "1.0";
}
