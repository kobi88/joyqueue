package com.jd.journalq.broker.kafka.message.serializer;

import com.jd.journalq.broker.kafka.message.compressor.KafkaCompressionCodec;
import com.jd.journalq.broker.kafka.message.compressor.KafkaCompressionCodecFactory;
import com.jd.journalq.broker.kafka.message.compressor.stream.ByteBufferInputStream;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * AbstractKafkaMessageSerializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/11
 */
public abstract class AbstractKafkaMessageSerializer {

    public static final byte MESSAGE_MAGIC_V0 = 0;
    public static final byte MESSAGE_MAGIC_V1 = 1;
    public static final byte MESSAGE_MAGIC_V2 = 2;
    public static final byte MESSAGE_CURRENT_MAGIC = MESSAGE_MAGIC_V2;

    protected static final byte INVALID_EXTENSION_MAGIC = -1;
    protected static final int EXTENSION_MAGIC_OFFSET = 0;
    protected static final int EXTENSION_CONTENT_OFFSET = 1;
    protected static final int EXTENSION_ATTRIBUTE_OFFSET = EXTENSION_CONTENT_OFFSET + 8;

    // v0, v1: offset + size + crc
    // v2:     offset + size + partitionLeaderEpoch
    protected static final int MAGIC_OFFSET = 8 + 4 + 4;
    protected static final int ATTRIBUTE_OFFSET = MAGIC_OFFSET + 1;

    protected static final int COMPRESSION_CODEC_MASK = 0x07;
    protected static final byte TRANSACTIONAL_FLAG_MASK = 0x10;
    protected static final int CONTROL_FLAG_MASK = 0x20;
    protected static final byte TIMESTAMP_TYPE_MASK = 0x08;

    protected static final int DECOMPRESS_BUFFER_SIZE = 1024;

    protected static byte getExtensionMagic(byte[] extension) {
        if (ArrayUtils.isEmpty(extension)) {
            return INVALID_EXTENSION_MAGIC;
        }
        return extension[EXTENSION_MAGIC_OFFSET];
    }

    protected static void writeExtensionMagic(byte[] extension, byte magic) {
        extension[EXTENSION_MAGIC_OFFSET] = magic;
    }

    protected static void setExtensionMagic(byte[] extension, byte magic) {
        ArrayUtils.add(extension, 0, magic);
    }

    protected static int getCompressionCodecType(short attribute) {
        return attribute & COMPRESSION_CODEC_MASK;
    }

    protected static int isTransactionl(short attribute) {
        return attribute & TRANSACTIONAL_FLAG_MASK;
    }

    protected static int getTimestampType(short attribute) {
        return attribute & TIMESTAMP_TYPE_MASK;
    }

    // TODO 优化，不需要buffer
    protected static ByteBuffer decompress(KafkaCompressionCodec compressionCodec, ByteBuffer buffer, byte messageVersion) throws Exception {
        byte[] intermediateBuffer = new byte[DECOMPRESS_BUFFER_SIZE];
        ByteBufferInputStream sourceInputStream = new ByteBufferInputStream(buffer);
        InputStream inputStream = KafkaCompressionCodecFactory.apply(compressionCodec, sourceInputStream, messageVersion);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            int count;
            while ((count = inputStream.read(intermediateBuffer)) > 0) {
                outputStream.write(intermediateBuffer, 0, count);
            }
        } finally {
            inputStream.close();
        }

        return ByteBuffer.wrap(outputStream.toByteArray());
    }
}