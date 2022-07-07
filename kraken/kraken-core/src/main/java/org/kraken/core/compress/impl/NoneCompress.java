package org.kraken.core.compress.impl;


import lombok.extern.slf4j.Slf4j;
import org.kraken.core.compress.Compress;
@Slf4j
public class NoneCompress implements Compress {
    @Override
    public byte[] compress(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        return bytes;
    }
}
