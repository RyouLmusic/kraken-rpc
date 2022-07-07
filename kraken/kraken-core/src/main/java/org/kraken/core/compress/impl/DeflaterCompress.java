package org.kraken.core.compress.impl;

import lombok.extern.slf4j.Slf4j;
import org.kraken.core.compress.Compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


@Slf4j
public class DeflaterCompress implements Compress {

    @Override
    public byte[] compress(byte[] bytes) throws IOException {
        Deflater compressor = new Deflater(4);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ) {
            compressor.setInput(bytes);
            compressor.finish();
            final byte[] buf = new byte[2048];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            return bos.toByteArray();
        } finally {
            compressor.end();
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IOException {
        Inflater decompressor = new Inflater();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ) {
            decompressor.setInput(bytes);
            final byte[] buf = new byte[2048];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            return bos.toByteArray();
        } catch (DataFormatException e) {
            log.error(e.getMessage(), e);
        } finally {
            decompressor.end();
        }
        return new byte[0];
    }
}
