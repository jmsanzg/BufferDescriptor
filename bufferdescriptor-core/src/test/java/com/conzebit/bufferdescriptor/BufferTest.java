package com.conzebit.bufferdescriptor;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

public class BufferTest {

    private BufferDescriptor bd = null;

    @Before
    public void before() {
        bd = new BufferBuilder()
                .addDataDescription(1, "BASE")
                .addDataDescription(5, "FIELD_A", "X(11)")
                .addDataDescription(5, "FIELD_B")
                    .addDataDescription(10, "FIELD_B1", "9(4)")
                    .addDataDescription(10, "FIELD_B2", "X(2)")
                .addDataDescription(5, "FIELD_C", 3)
                    .addDataDescription(10, "FIELD_C1", "9(4)")
                    .addDataDescription(10, "FIELD_C2", "X(2)")
                .addDataDescription(5, "FIELD_D", "X(10)", 2)
                .addDataDescription(5, "FIELD_E", 2)
                    .addDataDescription(10, "FIELD_E1", 2)
                        .addDataDescription(15, "FIELD_E11", "X(1)")
                .endDataDescription();
    }

    @Test
    public void testStringOccurs() {

        String d1Value = "field_d_1";
        bd.setString("FIELD_D(1)", d1Value);
        // Field returned will contain 10 characters: "field_d_1 " because FIELD_D was defined as X(10)
        Assert.assertFalse(d1Value.equals(bd.getString("FIELD_D(1)")));
        Assert.assertEquals("field_d_1 ", bd.getString("FIELD_D(1)"));

        String d2Value = "field_d_2v";
        bd.setString("FIELD_D(2)", d2Value);
        Assert.assertEquals(d2Value, bd.getString("FIELD_D(2)"));
    }

    @Test
    public void testNunbers() {

        int fieldB1 = 24;
        bd.setInt("FIELD_B1", fieldB1);
        Assert.assertEquals(fieldB1, bd.getInt("FIELD_B1"));

        long fieldC1_1 = 32;
        bd.setLong("FIELD_C1(1)", fieldC1_1);
        Assert.assertEquals(fieldC1_1, bd.getLong("FIELD_C1(1)"));

        double fieldC1_3 = 2.432;
        bd.setDouble("FIELD_C1(3)", fieldC1_3, 3);
        Assert.assertEquals(fieldC1_3, bd.getDouble("FIELD_C1(3)", 3));
    }

    @Test
    public void testDates() {

        Calendar calOriginal = Calendar.getInstance();
        bd.setDate("FIELD_A", calOriginal, "dd/MMM/yyyy");
        Calendar calReturned = bd.getDate("FIELD_A", "dd/MMM/yyyy");
        Assert.assertEquals(calOriginal.get(Calendar.DAY_OF_MONTH), calReturned.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(calOriginal.get(Calendar.MONTH), calReturned.get(Calendar.MONTH));
        Assert.assertEquals(calOriginal.get(Calendar.YEAR), calReturned.get(Calendar.YEAR));

        Locale localeES = new Locale("es");
        bd.setDate("FIELD_A", calOriginal, "dd/MMM/yyyy", localeES);
        calReturned = bd.getDate("FIELD_A", "dd/MMM/yyyy", localeES);
        Assert.assertEquals(calOriginal.get(Calendar.DAY_OF_MONTH), calReturned.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(calOriginal.get(Calendar.MONTH), calReturned.get(Calendar.MONTH));
        Assert.assertEquals(calOriginal.get(Calendar.YEAR), calReturned.get(Calendar.YEAR));

    }

    @Test
    public void testNesting() {

        bd.setString("FIELD_E11(1)(1)", "a");
        bd.setString("FIELD_E11(1)(2)", "b");
        bd.setString("FIELD_E11(2)(1)", "c");
        bd.setString("FIELD_E11(2)(2)", "d");

        Assert.assertEquals("a", bd.getString("FIELD_E11(1)(1)"));
        Assert.assertEquals("b", bd.getString("FIELD_E11(1)(2)"));
        Assert.assertEquals("c", bd.getString("FIELD_E11(2)(1)"));
        Assert.assertEquals("d", bd.getString("FIELD_E11(2)(2)"));
    }

    @Test
    public void testNestingVariableArguments() {

        bd.setString("FIELD_E11", "a", 1, 1); // same as "FIELD_EL1(1)(1)
        bd.setString("FIELD_E11", "b", 1, 2); // same as "FIELD_EL1(1)(2)
        bd.setString("FIELD_E11", "c", 2, 1); // same as "FIELD_EL1(2)(1)
        bd.setString("FIELD_E11", "d", 2, 2); // same as "FIELD_EL1(2)(2)

        Assert.assertEquals("a", bd.getString("FIELD_E11", 1, 1));
        Assert.assertEquals("b", bd.getString("FIELD_E11", 1, 2));
        Assert.assertEquals("c", bd.getString("FIELD_E11", 2, 1));
        Assert.assertEquals("d", bd.getString("FIELD_E11", 2, 2));
    }

    @Test
    public void testBuffer() {
        Calendar cal = Calendar.getInstance();
        bd.setString("FIELD_D(1)", "field_d_1");
        bd.setString("FIELD_D(2)", "field_d_2v");
        bd.setInt("FIELD_B1", 24);
        bd.setLong("FIELD_C1(1)", 32);
        bd.setDouble("FIELD_C1(3)", 2.432, 3);
        bd.setDate("FIELD_A", cal, "dd/MMM/yyyy");
        bd.setString("FIELD_E11(1)(1)", "a");
        bd.setString("FIELD_E11(1)(2)", "b");
        bd.setString("FIELD_E11(2)(1)", "c");
        bd.setString("FIELD_E11(2)(2)", "d");
        byte[] buffer = bd.getBuffer();
        byte[] bufferCloned = new byte[buffer.length];
        System.arraycopy(buffer, 0, bufferCloned, 0, buffer.length);

        bd.setBuffer(buffer);
        Assert.assertEquals(new String(buffer), new String(bd.getBuffer()));
        Assert.assertFalse("field_d_1".equals(bd.getString("FIELD_D(1)")));
        Assert.assertEquals("field_d_1 ", bd.getString("FIELD_D(1)"));
        Assert.assertEquals("field_d_2v", bd.getString("FIELD_D(2)"));
        Assert.assertEquals(24, bd.getInt("FIELD_B1"));
        Assert.assertEquals(32, bd.getLong("FIELD_C1(1)"));
        Assert.assertEquals(2.432, bd.getDouble("FIELD_C1(3)", 3));
        Calendar calReturned = bd.getDate("FIELD_A", "dd/MMM/yyyy");
        Assert.assertEquals(cal.get(Calendar.DAY_OF_MONTH), calReturned.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(cal.get(Calendar.MONTH), calReturned.get(Calendar.MONTH));
        Assert.assertEquals(cal.get(Calendar.YEAR), calReturned.get(Calendar.YEAR));
        Assert.assertEquals("a", bd.getString("FIELD_E11(1)(1)"));
        Assert.assertEquals("b", bd.getString("FIELD_E11(1)(2)"));
        Assert.assertEquals("c", bd.getString("FIELD_E11(2)(1)"));
        Assert.assertEquals("d", bd.getString("FIELD_E11(2)(2)"));

        byte[] biggerBuffer = new byte[buffer.length + 20];
        System.arraycopy(bufferCloned, 0, biggerBuffer, 0, buffer.length);

        try {
            bd.setBuffer(biggerBuffer);
            Assert.assertFalse(true); // never reached, previous line will throw an exception
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), IBufferConstant.ERROR_BUFFER_SIZE);
        }
        bd.setBuffer(biggerBuffer, true);

        Assert.assertEquals(new String(bufferCloned), new String(bd.getBuffer()));

    }
}