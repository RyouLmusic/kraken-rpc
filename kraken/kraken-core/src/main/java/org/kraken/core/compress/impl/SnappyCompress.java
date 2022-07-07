package org.kraken.core.compress.impl;

import lombok.extern.slf4j.Slf4j;
import org.kraken.core.compress.Compress;
import org.xerial.snappy.Snappy;

import java.io.IOException;

@Slf4j
public class SnappyCompress implements Compress {

    @Override
    public byte[] compress(byte[] bytes) throws IOException {
        return Snappy.compress(bytes);
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException {
        return Snappy.uncompress(bytes);
    }
}
