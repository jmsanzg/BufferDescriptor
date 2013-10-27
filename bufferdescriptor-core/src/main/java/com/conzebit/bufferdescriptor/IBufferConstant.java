package com.conzebit.bufferdescriptor;

public interface IBufferConstant {
	public static final String ERROR_DESCRIPTOR_NOT_FOUND = "Descriptor not found ";
	public static final String ERROR_DESCRIPTOR_LEVEL = "Descriptor must have a greater level number";
	public static final String ERROR_NAME_EXISTS = "The field name already exists";
    public static final String ERROR_NAME_DOESNT_EXIST = "The field name doesn't exist";
	public static final String ERROR_INVALID_PIC = "Incorrect PIC format";
	public static final String ERROR_NO_DESC = "No descriptor found";
	public static final String ERROR_TYPE_INVALID = "Data format incompatible with declared one";
	public static final String ERROR_BUFFER_SIZE = "Buffer with wrong size";
	public static final String ERROR_NULL_FORMAT = "Format null";
	public static final String ERROR_INVALID_VALUE = "Invalid value";
	public static final String ERROR_ILLEGAL_INSERTION =
            "Trying to insert a Descriptor inside a descriptor with length greater than zero";
}
