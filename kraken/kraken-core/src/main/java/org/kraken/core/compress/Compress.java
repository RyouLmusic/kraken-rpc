package org.kraken.core.compress;

import org.kraken.core.common.config.AppConfig;
import org.kraken.core.compress.impl.*;
import org.kraken.core.extension.SPI;
import org.kraken.core.serializer.SerializerContext;

import java.io.IOException;

/**
 * @author: hs
 */
@SPI
public interface Compress {
    // TODO 添加其他方式
    enum Type {
        Bzip2((byte) 0x01),
        Deflater((byte) 0x02),
        Gzip((byte) 0x03),
        LZ4((byte) 0x04),
        Lzo((byte) 0x05),
        Snappy((byte) 0x06),
        None((byte) 0x07);

        final byte code;

        Type(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return code;
        }

        public static Type getTypeByCode(byte code) {
            for (Type type : Type.values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }

    }
    Bzip2Compress bzip2 = new Bzip2Compress();
    DeflaterCompress deflater = new DeflaterCompress();
    GzipCompress gzip = new GzipCompress();
    LZ4Compress lz4 = new LZ4Compress();
    LzoCompress lzo = new LzoCompress();
    NoneCompress none = new NoneCompress();
    SnappyCompress snappy = new SnappyCompress();


    static Compress getInstance() {

        AppConfig appConfig = AppConfig.getAppConfig();
        switch (appConfig.getCompressType()) {
            case Bzip2 :
//                System.out.println("Bzip2");
                return bzip2;
            case Deflater :
//                System.out.println("Deflater");
                return deflater;
            case Gzip :
//                System.out.println("Gzip");
                return gzip;
            case LZ4 :
//                System.out.println("LZ4");
                return lz4;
            case Lzo :
//                System.out.println("Lzo");
                return lzo;
            case Snappy :
//                System.out.println("Snappy");
                return snappy;
            default :
//                System.out.println("None");
                return none;
        }
    }


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
