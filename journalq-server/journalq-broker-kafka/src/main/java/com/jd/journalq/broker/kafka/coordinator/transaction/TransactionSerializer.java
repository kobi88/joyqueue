package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.toolkit.lang.Charsets;

import java.nio.ByteBuffer;

/**
 * TransactionSerializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
public class TransactionSerializer {

    private static final int MAGIC = 0xCAFEBABE;
    private static final byte VERSION_V0 = 0;
    private static final byte CURRENT_VERSION = VERSION_V0;

    private static final byte PREPARE_TYPE = 0;
    private static final byte MARKER_TYPE = 1;
    private static final byte OFFSET_TYPE = 2;

    private static final int HEADER_FIX_LENGTH =
                    + 4 // magic
                    + 1 // version
                    + 1 // type
            ;

    private static final int PREPARE_FIX_LENGTH =
                    + 2 // topic length
                    + 2 // partition
                    + 2 // app length
                    + 4 // brokerId
                    + 1 // broker host length
                    + 4 // brokerPort
                    + 2 // transactionId length
                    + 8 // producerId
                    + 2 // producerEpoch
                    + 4 // timeout
                    + 8 // createTime
            ;

    private static final int MARKER_FIX_LENGTH =
                    + 2 // app length
                    + 2 // transactionId length
                    + 8 // producerId
                    + 2 // producerEpoch
                    + 4 // timeout
                    + 1 // state
                    + 8 // createTime
            ;

    private static final int OFFSET_FIX_LENGTH =
                    + 2 // topic length
                    + 2 // partition length
                    + 8 // offset length
                    + 2 // app length
                    + 2 // transactionId length
                    + 8 // producerId
                    + 2 // producerEpoch
                    + 4 // timeout
                    + 8 // createTime
            ;

    protected static int sizeOfPrepare(TransactionPrepare prepare) throws Exception {
        int size = PREPARE_FIX_LENGTH;

        byte[] bytes = Serializer.getBytes(prepare.getTopic(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(prepare.getApp(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(prepare.getBrokerHost(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(prepare.getTransactionId(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        return size;
    }

    protected static byte[] serializePrepare(ByteBuffer buffer, TransactionPrepare prepare) throws Exception {
        Serializer.write(prepare.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.putShort(prepare.getPartition());
        Serializer.write(prepare.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.putInt(prepare.getBrokerId());
        Serializer.write(prepare.getBrokerHost(), buffer, Serializer.BYTE_SIZE);
        buffer.putInt(prepare.getBrokerPort());
        Serializer.write(prepare.getTransactionId(), buffer, Serializer.BYTE_SIZE);
        buffer.putLong(prepare.getProducerId());
        buffer.putShort(prepare.getProducerEpoch());
        buffer.putInt(prepare.getTimeout());
        buffer.putLong(prepare.getCreateTime());
        return buffer.array();
    }

    protected static TransactionPrepare deserializePrepare(ByteBuffer buffer) throws Exception {
        TransactionPrepare prepare = new TransactionPrepare();
        prepare.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        prepare.setPartition(buffer.getShort());
        prepare.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        prepare.setBrokerId(buffer.getInt());
        prepare.setBrokerHost(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        prepare.setBrokerPort(buffer.getInt());
        prepare.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        prepare.setProducerId(buffer.getLong());
        prepare.setProducerEpoch(buffer.getShort());
        prepare.setTimeout(buffer.getInt());
        prepare.setCreateTime(buffer.getLong());
        return prepare;
    }

    protected static int sizeOfMarker(TransactionMarker marker) throws Exception {
        int size = MARKER_FIX_LENGTH;

        byte[] bytes = Serializer.getBytes(marker.getApp(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(marker.getTransactionId(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        return size;
    }

    protected static byte[] serializeMarker(ByteBuffer buffer, TransactionMarker marker) throws Exception {
        Serializer.write(marker.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(marker.getTransactionId(), buffer, Serializer.BYTE_SIZE);
        buffer.putLong(marker.getProducerId());
        buffer.putShort(marker.getProducerEpoch());
        buffer.put((byte) marker.getState().getValue());
        buffer.putInt(marker.getTimeout());
        buffer.putLong(marker.getCreateTime());
        return buffer.array();
    }

    protected static TransactionMarker deserializeMarker(ByteBuffer buffer) throws Exception {
        TransactionMarker marker = new TransactionMarker();
        marker.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        marker.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        marker.setProducerId(buffer.getLong());
        marker.setProducerEpoch(buffer.getShort());
        marker.setState(TransactionState.valueOf(buffer.get()));
        marker.setTimeout(buffer.getInt());
        marker.setCreateTime(buffer.getLong());
        return marker;
    }

    protected static int sizeOfOffset(TransactionOffset offset) throws Exception {
        int size = OFFSET_FIX_LENGTH;

        byte[] bytes = Serializer.getBytes(offset.getTopic(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(offset.getApp(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(offset.getTransactionId(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;
        return size;
    }

    protected static byte[] serializeOffset(ByteBuffer buffer, TransactionOffset offset) throws Exception {
        Serializer.write(offset.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.putShort(offset.getPartition());
        buffer.putLong(offset.getOffset());
        Serializer.write(offset.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(offset.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        buffer.putLong(offset.getProducerId());
        buffer.putShort(offset.getProducerEpoch());
        buffer.putInt(offset.getTimeout());
        buffer.putLong(offset.getCreateTime());
        return buffer.array();
    }

    protected static TransactionOffset deserializeOffset(ByteBuffer buffer) throws Exception {
        TransactionOffset transactionOffset = new TransactionOffset();
        transactionOffset.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionOffset.setPartition(buffer.getShort());
        transactionOffset.setOffset(buffer.getLong());
        transactionOffset.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionOffset.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionOffset.setProducerId(buffer.getLong());
        transactionOffset.setProducerEpoch(buffer.getShort());
        transactionOffset.setTimeout(buffer.getInt());
        transactionOffset.setCreateTime(buffer.getLong());
        return transactionOffset;
    }

    public static byte[] serialize(TransactionDomain transactionDomain) throws Exception {
        ByteBuffer buffer = null;
        if (transactionDomain instanceof TransactionPrepare) {
            int size = sizeOfPrepare((TransactionPrepare) transactionDomain) + HEADER_FIX_LENGTH;
            buffer = ByteBuffer.allocate(size);
            serializeHeader(buffer, transactionDomain);
            serializePrepare(buffer, (TransactionPrepare) transactionDomain);
        } else if (transactionDomain instanceof TransactionMarker) {
            int size = sizeOfMarker((TransactionMarker) transactionDomain) + HEADER_FIX_LENGTH;
            buffer = ByteBuffer.allocate(size);
            serializeHeader(buffer, transactionDomain);
            serializeMarker(buffer, (TransactionMarker) transactionDomain);
        } else if (transactionDomain instanceof TransactionOffset) {
            int size = sizeOfOffset((TransactionOffset) transactionDomain) + HEADER_FIX_LENGTH;
            buffer = ByteBuffer.allocate(size);
            serializeHeader(buffer, transactionDomain);
            serializeOffset(buffer, (TransactionOffset) transactionDomain);
        } else {
            throw new UnsupportedOperationException(String.format("unsupported transaction, type: %s", transactionDomain.getClass()));
        }
        return buffer.array();
    }

    public static TransactionDomain deserialize(ByteBuffer buffer) throws Exception {
        TransactionHeader header = deserializeHeader(buffer);
        switch (header.getType()) {
            case PREPARE_TYPE:
                return deserializePrepare(buffer);
            case MARKER_TYPE:
                return deserializeMarker(buffer);
            case OFFSET_TYPE:
                return deserializeOffset(buffer);
            default:
                throw new UnsupportedOperationException(String.format("unsupported transaction, type: %s", header.getType()));
        }
    }

    protected static void serializeHeader(ByteBuffer buffer, TransactionDomain transactionDomain) throws Exception {
        byte type = -1;
        if (transactionDomain instanceof TransactionPrepare) {
            type = PREPARE_TYPE;
        } else if (transactionDomain instanceof TransactionMarker) {
            type = MARKER_TYPE;
        } else if (transactionDomain instanceof TransactionOffset) {
            type = OFFSET_TYPE;
        } else {
            throw new UnsupportedOperationException(String.format("unsupported transaction, type: %s", transactionDomain.getClass()));
        }

        buffer.putInt(MAGIC);
        buffer.put(CURRENT_VERSION);
        buffer.put(type);
    }

    protected static TransactionHeader deserializeHeader(ByteBuffer buffer) {
        int magic = buffer.getInt();
        byte version = buffer.get();
        byte type = buffer.get();
        return new TransactionHeader(version, type);
    }

    private static class TransactionHeader {
        private byte type;
        private byte version;

        TransactionHeader() {

        }

        TransactionHeader(byte type, byte version) {
            this.type = type;
            this.version = version;
        }

        public byte getType() {
            return type;
        }

        public void setType(byte type) {
            this.type = type;
        }

        public byte getVersion() {
            return version;
        }

        public void setVersion(byte version) {
            this.version = version;
        }
    }
}