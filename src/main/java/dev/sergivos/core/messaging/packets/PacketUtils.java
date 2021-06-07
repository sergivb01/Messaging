package dev.sergivos.core.messaging.packets;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.sergivos.core.messaging.packets.PacketUtils.NettyPreconditions.checkFrame;

/**
 * Credits Velocity Powered (2021)
 * https://github.com/VelocityPowered/Velocity/blob/5ea6728d1a780186a98841d59b88867debf864fa/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java
 * https://github.com/VelocityPowered/Velocity/blob/5ea6728d1a780186a98841d59b88867debf864fa/proxy/src/main/java/com/velocitypowered/proxy/protocol/util/NettyPreconditions.java#L28
 */
public enum PacketUtils {
    ;
    private static final GsonComponentSerializer COMPONENT_SERIALIZER = GsonComponentSerializer.builder().build();
    private static final int DEFAULT_MAX_STRING_SIZE = 65536; // 64KiB

    private PacketUtils() {
    }

    /**
     * Reads a Minecraft-style VarInt from the specified {@code buf}.
     *
     * @param buf the buffer to read from
     * @return the decoded VarInt
     */
    public static int readVarInt(ByteBuf buf) {
        int read = readVarIntSafely(buf);
        if(read == Integer.MIN_VALUE) {
            throw new RuntimeException("Bad varint decoded");
        }
        return read;
    }

    /**
     * Reads a Minecraft-style VarInt from the specified {@code buf}. The difference between this
     * method and {@link #readVarInt(ByteBuf)} is that this function returns a sentinel value if the
     * varint is invalid.
     *
     * @param buf the buffer to read from
     * @return the decoded VarInt, or {@code Integer.MIN_VALUE} if the varint is invalid
     */
    public static int readVarIntSafely(ByteBuf buf) {
        int i = 0;
        int maxRead = Math.min(5, buf.readableBytes());
        for(int j = 0; j < maxRead; j++) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if((k & 0x80) != 128) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Writes a Minecraft-style VarInt to the specified {@code buf}.
     *
     * @param buf   the buffer to read from
     * @param value the integer to write
     */
    public static void writeVarInt(ByteBuf buf, int value) {
        while(true) {
            if((value & 0xFFFFFF80) == 0) {
                buf.writeByte(value);
                return;
            }

            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }

    public static String readString(ByteBuf buf) {
        return readString(buf, DEFAULT_MAX_STRING_SIZE);
    }

    public static Component readComponent(ByteBuf buf) {
        return COMPONENT_SERIALIZER.deserialize(readString(buf, DEFAULT_MAX_STRING_SIZE));
    }

    /**
     * Reads a VarInt length-prefixed UTF-8 string from the {@code buf}, making sure to not go over
     * {@code cap} size.
     *
     * @param buf the buffer to read from
     * @param cap the maximum size of the string, in UTF-8 character length
     * @return the decoded string
     */
    public static String readString(ByteBuf buf, int cap) {
        int length = readVarInt(buf);
        return readString(buf, cap, length);
    }

    private static String readString(ByteBuf buf, int cap, int length) {
        checkFrame(length >= 0, "Got a negative-length string (%s)", length);
        // `cap` is interpreted as a UTF-8 character length. To cover the full Unicode plane, we must
        // consider the length of a UTF-8 character, which can be up to 4 bytes. We do an initial
        // sanity check and then check again to make sure our optimistic guess was good.
        checkFrame(length <= cap * 4, "Bad string size (got %s, maximum is %s)", length, cap);
        checkFrame(buf.isReadable(length),
                "Trying to read a string that is too long (wanted %s, only have %s)", length,
                buf.readableBytes());
        String str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.skipBytes(length);
        checkFrame(str.length() <= cap, "Got a too-long string (got %s, max %s)",
                str.length(), cap);
        return str;
    }

    /**
     * Writes the specified {@code str} to the {@code buf} with a VarInt prefix.
     *
     * @param buf the buffer to write to
     * @param str the string to write
     */
    public static void writeString(ByteBuf buf, CharSequence str) {
        int size = ByteBufUtil.utf8Bytes(str);
        writeVarInt(buf, size);
        buf.writeCharSequence(str, StandardCharsets.UTF_8);
    }

    public static void writeComponent(ByteBuf buf, Component component) {
        writeString(buf, COMPONENT_SERIALIZER.serialize(component));
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return readByteArray(buf, DEFAULT_MAX_STRING_SIZE);
    }

    /**
     * Reads a VarInt length-prefixed byte array from the {@code buf}, making sure to not go over
     * {@code cap} size.
     *
     * @param buf the buffer to read from
     * @param cap the maximum size of the string, in UTF-8 character length
     * @return the byte array
     */
    public static byte[] readByteArray(ByteBuf buf, int cap) {
        int length = readVarInt(buf);
        checkFrame(length >= 0, "Got a negative-length array (%s)", length);
        checkFrame(length <= cap, "Bad array size (got %s, maximum is %s)", length, cap);
        checkFrame(buf.isReadable(length),
                "Trying to read an array that is too long (wanted %s, only have %s)", length,
                buf.readableBytes());
        byte[] array = new byte[length];
        buf.readBytes(array);
        return array;
    }

    public static void writeByteArray(ByteBuf buf, byte[] array) {
        writeVarInt(buf, array.length);
        buf.writeBytes(array);
    }

    /**
     * Reads an VarInt-prefixed array of VarInt integers from the {@code buf}.
     *
     * @param buf the buffer to read from
     * @return an array of integers
     */
    public static int[] readIntegerArray(ByteBuf buf) {
        int len = readVarInt(buf);
        checkArgument(len >= 0, "Got a negative-length integer array (%s)", len);
        int[] array = new int[len];
        for(int i = 0; i < len; i++) {
            array[i] = readVarInt(buf);
        }
        return array;
    }

    /**
     * Reads an UUID from the {@code buf}.
     *
     * @param buf the buffer to read from
     * @return the UUID from the buffer
     */
    public static UUID readUuid(ByteBuf buf) {
        long msb = buf.readLong();
        long lsb = buf.readLong();
        return new UUID(msb, lsb);
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads an UUID stored as an Integer Array from the {@code buf}.
     *
     * @param buf the buffer to read from
     * @return the UUID from the buffer
     */
    public static UUID readUuidIntArray(ByteBuf buf) {
        long msbHigh = (long) buf.readInt() << 32;
        long msbLow = (long) buf.readInt() & 0xFFFFFFFFL;
        long msb = msbHigh | msbLow;
        long lsbHigh = (long) buf.readInt() << 32;
        long lsbLow = (long) buf.readInt() & 0xFFFFFFFFL;
        long lsb = lsbHigh | lsbLow;
        return new UUID(msb, lsb);
    }

    /**
     * Writes an UUID as an Integer Array to the {@code buf}.
     *
     * @param buf  the buffer to write to
     * @param uuid the UUID to write
     */
    public static void writeUuidIntArray(ByteBuf buf, UUID uuid) {
        buf.writeInt((int) (uuid.getMostSignificantBits() >> 32));
        buf.writeInt((int) uuid.getMostSignificantBits());
        buf.writeInt((int) (uuid.getLeastSignificantBits() >> 32));
        buf.writeInt((int) uuid.getLeastSignificantBits());
    }

    /**
     * Reads a String array from the {@code buf}.
     *
     * @param buf the buffer to read from
     * @return the String array from the buffer
     */
    public static String[] readStringArray(ByteBuf buf) {
        int length = readVarInt(buf);
        String[] ret = new String[length];
        for(int i = 0; i < length; i++) {
            ret[i] = readString(buf);
        }
        return ret;
    }

    /**
     * Writes a String Array to the {@code buf}.
     *
     * @param buf         the buffer to write to
     * @param stringArray the array to write
     */
    public static void writeStringArray(ByteBuf buf, String[] stringArray) {
        writeVarInt(buf, stringArray.length);
        for(String s : stringArray) {
            writeString(buf, s);
        }
    }

    public static final class NettyPreconditions {
        private NettyPreconditions() {
            throw new AssertionError();
        }

        /**
         * Throws {@link RuntimeException} if {@code b} is false.
         *
         * @param b       the expression to check
         * @param message the message to include in the thrown {@link RuntimeException}
         */
        public static void checkFrame(boolean b, String message) {
            if(!b) {
                throw new RuntimeException(message);
            }
        }

        /**
         * Throws {@link RuntimeException} if {@code b} is false.
         *
         * @param b       the expression to check
         * @param message the message to include in the thrown {@link RuntimeException}, formatted
         *                like {@link com.google.common.base.Preconditions#checkArgument(boolean)} and
         *                friends
         * @param arg1    the first argument to format the message with
         */
        public static void checkFrame(boolean b, String message, Object arg1) {
            if(!b) {
                throw new RuntimeException(Strings.lenientFormat(message, arg1));
            }
        }

        /**
         * Throws {@link RuntimeException} if {@code b} is false.
         *
         * @param b       the expression to check
         * @param message the message to include in the thrown {@link RuntimeException}, formatted
         *                like {@link com.google.common.base.Preconditions#checkArgument(boolean)} and
         *                friends
         * @param arg1    the first argument to format the message with
         * @param arg2    the second argument to format the message with
         */
        public static void checkFrame(boolean b, String message, Object arg1, Object arg2) {
            if(!b) {
                throw new RuntimeException(Strings.lenientFormat(message, arg1, arg2));
            }
        }

        /**
         * Throws {@link RuntimeException} if {@code b} is false.
         *
         * @param b       the expression to check
         * @param message the message to include in the thrown {@link RuntimeException}, formatted
         *                like {@link com.google.common.base.Preconditions#checkArgument(boolean)} and
         *                friends
         * @param args    the arguments to format the message with-
         */
        public static void checkFrame(boolean b, String message, Object... args) {
            if(!b) {
                throw new RuntimeException(Strings.lenientFormat(message, args));
            }
        }
    }

}
