package com.conzebit.bufferdescriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal class used by BufferDescriptor
 */
public class DataDescriptor implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;

	enum Type {
		NUMBER,
		STRING
	}

    protected int levelNumber;
    protected Type type;
    protected String name;
    protected int length;
    protected int occurs;
    protected List<List<DataDescriptor>> childs;
    protected String indexPosition;
    protected int bufferInit = 0;
    protected int bufferLen = 0;

    protected DataDescriptor(final int levelNumber,
                             final String name,
                             final int length,
                             final int occurs,
                             final Type type) {
        if (levelNumber <= 0) {
            throw new IllegalArgumentException("levelNumber <= 0");
        }
        this.levelNumber = levelNumber;
    	
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name == null");
        }
        this.name = name;

        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        this.length = length;

        if (occurs < 1) {
            throw new IllegalArgumentException("occurs < 1");
        }
        this.occurs = occurs;

        this.childs = new ArrayList<List<DataDescriptor>>(this.occurs);
        for (int i = 0; i < this.occurs; i++) {
            this.childs.add(new ArrayList<DataDescriptor>(0));
        }
        this.indexPosition = "";
        this.type = type;
    }

    protected final Object clone() throws CloneNotSupportedException {
        DataDescriptor clon = (DataDescriptor) super.clone();
        this.childs = new ArrayList<List<DataDescriptor>>(this.occurs);
        for (int i = 0; i < this.occurs; i++) {
            this.childs.add(new ArrayList<DataDescriptor>(0));
        }
        return clon;
    }

    protected final String getName() {
        return this.name + this.indexPosition;
    }
}