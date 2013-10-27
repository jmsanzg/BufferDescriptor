package com.conzebit.bufferdescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the main class that allows to create a BufferDescriptor.<br />
 * <br />
 * For instance, if we have the following COBOL PIC:<br />
 * <br />
 * <code>
 * 1 ROOT.<br />
 * &nbsp;5 A OCCURS 3.<br />
 * &nbsp;&nbsp;10 A-A&nbsp;&nbsp;PIC XX.<br />
 * &nbsp;&nbsp;10 A-B&nbsp;&nbsp;PIC X OCCURS 5.<br />
 * &nbsp;5 B&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PIC X.<br />
 * &nbsp;5 C&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PIC 9 OCCURS 2.<br />
 * </code>
 * <br />
 * To be able to use this on Java, you must write the following code:
 * <br /><br />
 * <code>
 * BufferDescriptor bd = new BufferBuilder()<br />
 * &nbsp;.addDataDescription(1, "ROOT")<br />
 * &nbsp;.addDataDescription(5, "A", 3)<br />
 * &nbsp;.addDataDescription(10, "A-A", "X(2)")<br />
 * &nbsp;.addDataDescription(10, "A-B", "X(1)", 5)<br />
 * &nbsp;.addDataDescription(5, "B", "X(1)")<br />
 * &nbsp;.addDataDescription(5, "C", "9(3)")<br />
 * &nbsp;.endDataDescription();<br />
 * </code>
 * From that point, you can use the Buffer using sets and gets as described by javadoc.
 */
public class BufferBuilder {
	
    private DataDescriptor rootDataDescriptor = null;
    private int bufferSize = 0;
    private Map<String, DataDescriptor> descriptorHash = null;

    public BufferBuilder() {
        this.rootDataDescriptor = null;
        this.bufferSize = 0;
        this.descriptorHash = new HashMap<String, DataDescriptor>(0);
    }
    
    /**
     * Adds a new field to descriptor list. OCCURS will have a value of 1 and PIC will not be set.<br />
     * <br />
     * Example: If we have the following COBOL expression<br />
     * <br />
     * <b><code>1  ROOT.</code></b><br />
     * <br />
     * The call to the method will be:<br />
     * <br />
     * <b><code>.addDataDescription(1, "ROOT")</code></b>
     * @param levelNumber Level number of the field. It must be greater than zero.
     * @param name Name of the field.
     */
    public BufferBuilder addDataDescription(final int levelNumber, final String name) {
        return this.addDataDescription(levelNumber, name, null, 1);
    }

    /**
     * Adds a new field to descriptor list. PIC will not be set.<br />
     * <br />
     * Example: If we have the following COBOL expression<br />
     * <br />
     * <b><code>5  PHONES OCCURS 3.</code></b><br />
     * <br />
     * The call to the method will be:<br />
     * <br />
     * <b><code>.addDataDescription(5, "PHONES", 3);</code></b>
     * @param levelNumber Level number of the field. It must be greater than zero.
     * @param name Name of the field.
     * @param occurs Number of occurrences of the field. It must be greater than zero.
     */
    public final BufferBuilder addDataDescription(final int levelNumber, final String name, final int occurs) {
        return this.addDataDescription(levelNumber, name, null, occurs);
    }

    /**
     * Adds a new field to descriptor list. OCCURS will have a value of 1.<br />
     * <br />
     * Example: If we have the following COBOL expression<br />
     * <br />
     * <b><code>5  NAME PIC X(50).</code></b><br />
     * <br />
     * The call to the method will be:<br />
     * <br />
     * <b><code>.addDataDescription(5, "NAME", "PIC X(50)");</code></b>
     * @param levelNumber Level number of the field. It must be greater than zero.
     * @param name Name of the field.
     * @param pic PIC of the field using the following format <code>PIC [X|9](length)</code>
     */
    public final BufferBuilder addDataDescription(final int levelNumber, final String name, final String pic) {
        return this.addDataDescription(levelNumber, name, pic, 1);
    }

    /**
     * Adds a new field to descriptor list.<br />
     * <br />
     * Example: If we have the following COBOL expression<br />
     * <br />
     * <b><code>5  PHONES PIC 9(9) OCCURS 3.</code></b><br />
     * <br />
     * The call to the method will be:<br />
     * <br />
     * <b><code>.addDataDescription(5, "PHONES", "PIC 9(9)", 3);</code></b>
     * @param levelNumber Level number of the field. It must be greater than zero.
     * @param name Name of the field.
     * @param pic PIC of the field using the following format <code>PIC [X|9](length)</code>
     * @param occurs Number of occurrences of the field. It must be greater than zero.
     */
    public final BufferBuilder addDataDescription(final int levelNumber,
                                                  final String name,
                                                  final String pic,
                                                  final int occurs) {

        if (this.descriptorHash.containsKey(name)) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_NAME_EXISTS);
        }

        DataDescriptor.Type type = this.getPICType(pic);
        int length = this.getPICLength(pic);

        DataDescriptor dd = new DataDescriptor(levelNumber, name, length, occurs, type);

        if (this.rootDataDescriptor == null) {
            this.rootDataDescriptor = dd;
        } else {
            try {
                boolean ok = appendChildDescriptor(this.rootDataDescriptor, dd);
                if (ok) {
                    this.descriptorHash.put(name, dd);
                } else {
                    throw new IllegalArgumentException(IBufferConstant.ERROR_DESCRIPTOR_LEVEL);
                }
            } catch (CloneNotSupportedException cloneNotSupportedException) {
                // This exception will never be raised as DataDescriptor is cloneable
            }
        }
        return this;
    }
    
    /**
     * Adds a DataDescriptor as a child
     * @param parent Parent of the descriptor to be added
     * @param dd DataDescriptor to be added
     * @return true if DataDescriptor was added, false otherwise
     * @throws CloneNotSupportedException if not able to clone
     */
    private boolean appendChildDescriptor(final DataDescriptor parent, final DataDescriptor dd)
            throws CloneNotSupportedException {

        if (parent.levelNumber >= dd.levelNumber) {
            return false;
        }

        if (parent.length > 0) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_ILLEGAL_INSERTION);
        }

        boolean insertOk = false;
        for (List<DataDescriptor> list : parent.children) {
            int size = list.size() - 1;
            if (size >= 0) {
                insertOk = appendChildDescriptor(list.get(size), dd);
            }
        }
        if (insertOk) {
            return true;
        }

        DataDescriptor clone = (DataDescriptor) dd.clone();
        clone.indexPosition = parent.indexPosition;
        List<DataDescriptor> list = parent.children.get(0);
        list.add(clone);
        if (dd.occurs > 1) {
            clone.length = 0;
            int j = 0;
            for (List<DataDescriptor> clonedList : clone.children) {
                DataDescriptor newClone = (DataDescriptor) dd.clone();
                newClone.indexPosition = clone.indexPosition + '(' + (j + 1) + ')'; // Replaced to StringBuilder by javac, no worries...
                j++;
                newClone.occurs = 1;
                clonedList.add(newClone);
            }
        }
        return true;
    }

    
    /**
     * Ending of definition of field descriptions.
     */
    public final BufferDescriptor endDataDescription() {
        if (this.rootDataDescriptor == null) {
            throw new IllegalStateException(IBufferConstant.ERROR_NO_DESC);
        }

        this.bufferSize = adjustLimit(this.rootDataDescriptor, 0);
        this.descriptorHash = new HashMap<String, DataDescriptor>();
        addToHash(this.rootDataDescriptor, this.descriptorHash);
        return new BufferDescriptor(this.rootDataDescriptor, this.bufferSize, this.descriptorHash);
    }
    
    
    /**
     * Adds current descriptor and all it's child to the map.
     * @param ht map where the Descriptor and all it's child will be added
     */
    protected final void addToHash(final DataDescriptor dd, final Map<String, DataDescriptor> ht) {
        ht.put(dd.getName(), dd);
        for (List<DataDescriptor> children : dd.children) {
            for (DataDescriptor child : children) {
            	addToHash(child, ht);
            }
        }
    }
    
    /**
     * Adjust limits to read current descriptor.
     * @param bi Starting point
     * @return total length
     */
    protected final int adjustLimit(final DataDescriptor dd, final int bi) {
        dd.bufferInit = bi;
        dd.bufferLen = 0;
        for (List<DataDescriptor> childs : dd.children) {
            for (DataDescriptor child : childs) {
                dd.bufferLen += adjustLimit(child, bi + dd.bufferLen);
            }
        }
        dd.bufferLen += dd.length;
        return dd.bufferLen;
    }
    

    
    /**
     * Gets the type given a PIC.
     * @param pic PIC of the field
     * @return type of the field
     */
    private DataDescriptor.Type getPICType(final String pic) {
        if (pic == null) {
            return DataDescriptor.Type.STRING;
        }

        if (pic.length() < 4) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_INVALID_PIC);
        }

        DataDescriptor.Type ret;

        char c = pic.charAt(0);
        if (c == 'x' || c == 'X') {
            ret = DataDescriptor.Type.STRING;
        } else if (c == '9') {
            ret = DataDescriptor.Type.NUMBER;
        } else {
            throw new IllegalArgumentException(IBufferConstant.ERROR_INVALID_PIC);
        }
        return ret;
    }
    
    /**
     * Gets PIC length
     * @param pic PIC of the field
     * @return length of the field
     */
    private int getPICLength(final String pic) {
        if (pic == null) {
            return 0;
        }

        if ((pic.length() < 4) || (pic.charAt(1) != '(') || (pic.charAt(pic.length() - 1) != ')')) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_INVALID_PIC);
        }

        String sLength = pic.substring(2, pic.length() - 1);

        int ret;
        try {
            ret = Integer.parseInt(sLength);
        } catch (Exception e) {
            throw new IllegalArgumentException(IBufferConstant.ERROR_INVALID_PIC);
        }
        return ret;
    }
    
}
