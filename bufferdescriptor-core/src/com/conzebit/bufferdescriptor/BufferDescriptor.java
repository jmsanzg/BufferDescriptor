package com.conzebit.bufferdescriptor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import com.conzebit.bufferdescriptor.DataDescriptor.Type;

/**
 * BufferDescriptor that contains both the structure and the values of a Buffer.
 */
public class BufferDescriptor implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

    protected DataDescriptor rootDataDescriptor = null;
    private byte[] buffer = null;
    private Map<String, DataDescriptor> descriptorHash = null;

    protected BufferDescriptor(DataDescriptor rootDataDescriptor, int bufferSize, Map<String, DataDescriptor> descriptorHash) {
        this.rootDataDescriptor = rootDataDescriptor;
        this.buffer = new byte[bufferSize];
        this.descriptorHash = descriptorHash;
        this.clearBuffer();
    }

    /**
     * Clear the data buffer. Initializes all fields to their default value.
     * PIC X are initialized to blank spaces and PIC 9 are initialized to zeroes.
     */
    public final void clearBuffer() {
        for (DataDescriptor dd : this.descriptorHash.values()) {
            String value = null;
            if (dd.length > 0) {
                char[] b = new char[dd.length];
                if (dd.type == Type.NUMBER) {
                    Arrays.fill(b, '0');
                } else {
                    Arrays.fill(b, ' ');
                }
                value = new String(b);
            }
            if (value != null) {
                System.arraycopy(value.getBytes(), 0, this.buffer, dd.bufferInit, dd.bufferLen);
            }
        }
    }
    
    /**
     * Returns the data buffer as a byte[]
     * @return Data buffer 
     */
    public final byte[] getBuffer() {
        return this.buffer;
    }

    /**
     * Returns a Calendar instance.
     * For instance, given a field with PIC 9(8) in which we have a date with yyyyMMdd format, to get a Calendar
     * from that field then we must call to this method this way:<br />
     * <br />
     * <b><code>
     * bd.getDate("BIRTH-DATE", "yyyyMMdd");
     * </code></b>
     * @param name Name of the field
     * @param format Format as used by SimpleDateFormat.
     * @return a Calendar instance or an exception if unable to parse the field
     */
    public final Calendar getDate(final String name, final String format) {
        return getDate(name, format, Locale.getDefault());
    }

    /**
     * Returns a Calendar instance.
     * Given a field with PIC 9(8) in which we have a date with yyyyMMdd format, to get a Calendar
     * from that field then we must call to this method this way:<br />
     * <br />
     * <b><code>
     * bd.getDate("BIRTH-DATE", "yyyyMMdd");
     * </code></b>
     * @param name Name of the field
     * @param format Format as used by SimpleDateFormat
     * @param locale Locale to be used
     * @return a Calendar instance or an exception if unable to parse the field
     */
    public final Calendar getDate(final String name, final String format, final Locale locale) {
        String value = this.getString(name);
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        try {
            Date d = sdf.parse(value);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        } catch (Exception e) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_INVALID_VALUE);
        }
    }

    /**
     * Returns a field as a double value
     * <br />
     * <b><code>5  TOTAL-INCOME PIC 9(4)V99.</code></b><br />
     * <br />
     * <b><code>bd.getDouble("TOTAL-INCOME", 2);</code></b><br />
     * <br />
     * @param name Name of the field
     * @param numDecimal Number of decimal positions of the double field
     * @return Value as double type
     */
    public final double getDouble(final String name, final int numDecimal) {
        return (this.getInt(name) / Math.pow(10, numDecimal));
    }

    /**
     * Returns a field as an int value
     * @param name Name of the field
     * @return Integer value of the field
     */
    public final int getInt(final String name) {
        int ret = -1;
        String sRet = this.getValue(name);
        if (sRet != null) {
            try {
                ret = Integer.parseInt(sRet);
            } catch (Exception e) {
                throw new IllegalArgumentException(IBufferConstant.ERROR_TYPE_INVALID);
            }
        }
        return ret;
    }

    /**
     * Returns a field as a long value
     * @param name Name of the field
     * @return Long value of the field
     */
    public final long getLong(final String name) {
        long ret = -1;
        String sRet = this.getValue(name);
        if (sRet != null) {
            try {
                ret = Long.parseLong(sRet);
            } catch (Exception e) {
                throw new IllegalArgumentException(IBufferConstant.ERROR_TYPE_INVALID);
            }
        }
        return ret;
    }

    /**
     * Returns a field as string-
     * @param name Name of the variable
     * @return String value of the field
     */
    public final String getString(final String name) {
        return this.getValue(name);
    }

    /**
     * Sets a buffer. If the size of the Buffer doesn't match with
     * the one expected then an IllegalArgumentException is thrown.
     * @param buffer Buffer to be set
     */
    public final void setBuffer(final byte[] buffer) {
        if (buffer.length != this.buffer.length) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_BUFFER_SIZE);
        }
        this.buffer = buffer;
    }

    /**
     * Sets a buffer to get data but allowing to specify if the buffer can be truncated if the size doesn't match
     * with this defined BufferDescriptor .
     * @param buffer Buffer to be set
     * @param resize true resize the buffer, false do not resize
     */
    public final void setBuffer(final byte[] buffer, final boolean resize) {
        byte[] resizeBuffer;
        if (resize) {
            this.clearBuffer();
            resizeBuffer = new byte[this.buffer.length];
            System.arraycopy(this.buffer, 0, resizeBuffer, 0, this.buffer.length);
            int size = resizeBuffer.length;
            if (buffer.length < resizeBuffer.length) {
                size = buffer.length;
            }
            System.arraycopy(buffer, 0, resizeBuffer, 0, size);
        } else {
            resizeBuffer = buffer;
        }
        this.setBuffer(resizeBuffer);
    }

    /**
     * Sets a date as value for a field<br />
     * @param name Name of the field
     * @param calendar Calendar instance
     * @param format Formato as used by SimpleDateFormat
     */
    public final void setDate(final String name, final Calendar calendar, final String format) {
        this.setDate(name, calendar, format, Locale.getDefault());
    }

    /**
     * Sets a date as value for a field<br />
     * @param name Name of the field
     * @param calendar Calendar instance
     * @param format Format as used by SimpleDateFormat
     * @param locale Locale to be used
     */
    public final void setDate(final String name, final Calendar calendar, final String format, final Locale locale) {
        if (format == null) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_NULL_FORMAT);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        String value = "";
        if (calendar != null) {
            value = sdf.format(calendar.getTime());
        }

        this.setValue(name, value, DataDescriptor.Type.STRING);
    }

    /**
     * Sets a double to a field
     * <br />
     * <b><code>5 INCOME PIC 9(4)V99.</code></b><br />
     * <br />
     * <b><code>bd.setDouble("INCOME", 12.345, 2);</code></b><br />
     * <br />
     * Notice that the buffer doesn't really contain decimal separator. For instance, given the above code, the field
     * would really contain <b><code>001234</code></b>
     * @param name Name of the field
     * @param value Value to be set
     * @param numDecimal Number of digits of the decimal part
     */
    public final void setDouble(final String name, final double value, final int numDecimal) {
        int val = (int) (value * (Math.pow(10, numDecimal)));
        this.setInt(name, val);
    }

    /**
     * Sets an int value to a field
     * <br />
     * <b><code>5  INCOME PIC 9(8).</code></b><br />
     * <br />
     * <b><code>bd.setInt("INCOME", 12345);</code></b><br />
     * <br />
     * The field would be filled with a value of <b><code>00012345</code></b>
     * @param name Name of the field
     * @param value Value to be set
     */
    public final void setInt(final String name, final int value) {
        this.setValue(name, String.valueOf(value), DataDescriptor.Type.NUMBER);
    }

    /**
     * Sets a long to a field
     * @param name Name of the field
     * @param value Value to be set
     */
    public final void setLong(final String name, final long value) {
        this.setValue(name, String.valueOf(value), DataDescriptor.Type.NUMBER);
    }

    /**
     * Sets a string to a field. If the string is bigger than the field size it'll be truncated. if it's shorter than
     * the field size then it's filled by right spaces.
     * @param name Field name
     * @param value Value to be set
     */
    public final void setString(final String name, final String value) {
        this.setValue(name, value, DataDescriptor.Type.STRING);
    }

    /**
     * Returns the value of the field
     * @param name Name of the field
     * @return value of the field or null if it doesn't exist
     */
    private String getValue(final String name) {
        DataDescriptor dd = this.descriptorHash.get(name);
        if (dd == null) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_NAME_DOESNT_EXIST);
        }
        return new String(this.buffer, dd.bufferInit, dd.bufferLen);
    }
    
    /**
     * Sets the value of the field
     * @param name Name of the field
     * @param value vañie of the field
     * @param type Data Type
     */
    private void setValue(final String name, final String value, final DataDescriptor.Type type) {
        DataDescriptor dd = this.descriptorHash.get(name);
        if (dd == null) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_DESCRIPTOR_NOT_FOUND + name);
        }

        String _value = value;
        if (value == null) {
            switch (type) {
            case NUMBER:
                _value = "0";
                break;
            default:
                _value = " ";
                break;
            }
        }

        String processedValue = _value;
        if (dd.bufferLen < _value.length()) {
            processedValue = _value.substring(0, dd.bufferLen);
        } else if (dd.bufferLen > _value.length()) {
            char[] c = new char[dd.bufferLen - _value.length()];
            switch (type) {
            case NUMBER:
                Arrays.fill(c, '0');
                processedValue = String.valueOf(c) + _value;
                break;
            default:
                Arrays.fill(c, ' ');
                processedValue = _value + String.valueOf(c);
            }
        }

        System.arraycopy(processedValue.getBytes(), 0, this.buffer, dd.bufferInit, dd.bufferLen);
    }
}