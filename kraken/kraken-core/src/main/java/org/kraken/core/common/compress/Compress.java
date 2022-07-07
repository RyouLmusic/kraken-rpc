package org.kraken.core.common.compress;

import java.io.IOException;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/13 17:50
 * TODO SPI
 */
public interface Compress {

    /**
     * 压缩
     *
     * @param bytes 原始字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes) throws IOException;

    /**
     * 解压
     *
     * @param bytes 压缩后的字节数组
     * @return 原始字节数组
     */
    byte[] decompress(byte[] bytes) throws IOException;
}
