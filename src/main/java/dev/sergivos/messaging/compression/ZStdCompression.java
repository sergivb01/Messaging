package dev.sergivos.messaging.compression;

public class ZStdCompression /* implements Compression */ {
//    private static final ByteBufAllocator bufferPool = PooledByteBufAllocator.DEFAULT;
//    private static final double TOLERANCE = 1.1;
//
//    // TODO: migrate
//    public static byte[] compressData(@NonNull ByteBuf data) throws IOException {
//        if(data.capacity() == 0) {
//            return new byte[0];
//        }
//
//        data.readerIndex(0);
//
//        int uncompressedBytes = data.writerIndex();
//        int upperBound = (int) Zstd.compressBound(uncompressedBytes) + 5;
//
//        ByteBuf nd = bufferPool.directBuffer(uncompressedBytes, uncompressedBytes);
//        ByteBuf ndd = bufferPool.directBuffer(upperBound, upperBound);
//
//        try {
//            data.readBytes(nd);
//            ByteBuffer d = nd.nioBuffer(0, uncompressedBytes);
//
//            ByteBuffer dest = ndd.nioBuffer(0, upperBound);
//            long compressedBytes = Zstd.compressDirectByteBuffer(dest, 5, upperBound - 5, d, 0, uncompressedBytes, 9);
//            if(Zstd.isError(compressedBytes)) {
//                throw new IOException(new ZstdException(compressedBytes));
//            }
//
//            if((double) uncompressedBytes / (double) (compressedBytes + 4L) < TOLERANCE) {
//                byte[] out = new byte[uncompressedBytes + 1];
//                out[0] = 0x00;
//                nd.readBytes(out, 1, uncompressedBytes);
//                return out;
//            }
//
//            dest.put(0, (byte) 0x01);
//            dest.putInt(1, uncompressedBytes);
//            dest.rewind();
//
//            byte[] out = new byte[(int) compressedBytes + 5];
//            dest.get(out);
//            return out;
//        } finally {
//            nd.release();
//            ndd.release();
//        }
//    }
//
//    // TODO: migrate
//    public static @NonNull ByteBuf decompressData(@Nullable ByteBuf data) throws IOException {
//        if(data == null || data.capacity() == 0) {
//            return bufferPool.buffer(0, 0);
//        }
//
//        int compressedBytes = data.writerIndex();
//        data.readerIndex(0);
//
//        boolean compressed = data.readByte() != 0x00;
//        if(!compressed) {
//            ByteBuf retVal = bufferPool.buffer(compressedBytes - 1, compressedBytes - 1);
//            data.readBytes(retVal);
//
////            System.out.println("Received (no) compression: " + compressedBytes + "/" + (compressedBytes - 1) + " (" + ratioFormat.format((double) (compressedBytes - 1) / (double) compressedBytes) + ")");
//
//            return retVal;
//        }
//
//        int uncompressedBytes = data.readInt();
//        ByteBuf nd = bufferPool.directBuffer(compressedBytes - 5, compressedBytes - 5);
//        ByteBuf ndd = bufferPool.directBuffer(uncompressedBytes, uncompressedBytes);
//
//        try {
//            data.readBytes(nd);
//            ByteBuffer d = nd.nioBuffer(0, compressedBytes - 5);
//
//            ByteBuffer dest = ndd.nioBuffer(0, uncompressedBytes);
//            long decompressedBytes = Zstd.decompressDirectByteBuffer(dest, 0, uncompressedBytes, d, 0, compressedBytes - 5);
//            if(Zstd.isError(decompressedBytes)) {
//                throw new IOException(new ZstdException(decompressedBytes));
//            }
//
////            System.out.println("Received compression: " + compressedBytes + "/" + uncompressedBytes + " (" + ratioFormat.format((double) uncompressedBytes / (double) compressedBytes) + ")");
//
//            dest.rewind();
//            ByteBuf retVal = bufferPool.buffer(uncompressedBytes, uncompressedBytes);
//            retVal.writeBytes(dest);
//
//            return retVal;
//        } finally {
//            nd.release();
//            ndd.release();
//        }
//    }

}
