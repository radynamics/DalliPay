package com.radynamics.dallipay.iso20022.pain001;

import com.radynamics.dallipay.iso20022.creditorreference.ReferenceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class AbaReaderTest {
    @Test
    public void readExample1() throws Exception {
        var input = """
                0                 01BQL       MY NAME                   1111111004231633  230410
                1123-456157108231 530000001234S R SMITH                       TEST BATCH        062-000 12223123MY ACCOUNT      00001200
                1123-783 12312312 530000002200J K MATTHEWS                    TEST BATCH        062-000 12223123MY ACCOUNT      00000030
                1456-789   125123 530003123513P R JONES                       TEST BATCH        062-000 12223123MY ACCOUNT      00000000
                1121-232    11422 530000002300S MASLIN                        TEST BATCH        062-000 12223123MY ACCOUNT      00000000
                7999-999            000312924700031292470000000000                        000004""";

        var reader = new AbaReader(new TestLedger());
        var payments = reader.read(new ByteArrayInputStream(input.getBytes()));

        Assertions.assertEquals(4, payments.length);

        Assertions.assertEquals("157108231", payments[0].getReceiverAccount().getUnformatted());
        Assertions.assertEquals(12.34, payments[0].getAmount());
        Assertions.assertEquals("S R SMITH", payments[0].getReceiverAddress().getName());
        Assertions.assertEquals("12223123", payments[0].getSenderAccount().getUnformatted());
        Assertions.assertEquals("MY ACCOUNT", payments[0].getSenderAddress().getName());
        Assertions.assertEquals(1, payments[0].getStructuredReferences().length);
        Assertions.assertEquals(ReferenceType.Isr, payments[0].getStructuredReferences()[0].getType());
        Assertions.assertEquals("TEST BATCH", payments[0].getStructuredReferences()[0].getUnformatted());

        Assertions.assertEquals("12312312", payments[1].getReceiverAccount().getUnformatted());
        Assertions.assertEquals(22.00, payments[1].getAmount());
        Assertions.assertEquals("J K MATTHEWS", payments[1].getReceiverAddress().getName());
        Assertions.assertEquals("12223123", payments[1].getSenderAccount().getUnformatted());
        Assertions.assertEquals("MY ACCOUNT", payments[1].getSenderAddress().getName());
        Assertions.assertEquals(1, payments[1].getStructuredReferences().length);
        Assertions.assertEquals(ReferenceType.Isr, payments[1].getStructuredReferences()[0].getType());
        Assertions.assertEquals("TEST BATCH", payments[1].getStructuredReferences()[0].getUnformatted());
    }
    @Test
    public void readExample2() throws Exception {
        var input = """
0                 01BQL       USER NAME                 123456WAGES       300916                                       \s
1484-001 32666591 500000015800EMPLOYEE 01                     000005991         124-001234567890WAGES Payment   00000000
1124-101 90988259 500000007800EMPLOYEE 02                     000348383         124-001234567890WAGES Payment   00000000
1062-191 12479074 500000004600EMPLOYEE 03                     000407577         124-001234567890WAGES Payment   00000000
1084-014 52496140 500000021198EMPLOYEE 04                     000501403         124-001234567890WAGES Payment   00000000
1633-000 18656046 500000008600EMPLOYEE 05                     000553305         124-001234567890WAGES Payment   00000000
1014-012 10306718 500000044996EMPLOYEE 06                     001244797         124-001234567890WAGES Payment   00000000
1012-022 60341161 500000004350EMPLOYEE 07                     001691260         124-001234567890WAGES Payment   00000000
1064-147 11609760 500000024200EMPLOYEE 08                     002047942         124-001234567890WAGES Payment   00000000
1034-977100087549 500000027800EMPLOYEE 09                     002086445         124-001234567890WAGES Payment   00000000
1082-013 10517995 500000064000EMPLOYEE 10                     002139012         124-001234567890WAGES Payment   00000000
1063-021 00634226 500044444444EMPLOYEE 11                     000009549         124-001234567890WAGES Payment   00000000
1124-001234567890 130044667788Company Account                 CONTRA WAGES      124-001234567890WAGES Payment   00000000
7999-999            000000000000446677880044667788                        000012                                       \s
""";

        var reader = new AbaReader(new TestLedger());
        var payments = reader.read(new ByteArrayInputStream(input.getBytes()));

        Assertions.assertEquals(12, payments.length);

        Assertions.assertEquals("32666591", payments[0].getReceiverAccount().getUnformatted());
        Assertions.assertEquals(158.00, payments[0].getAmount());
        Assertions.assertEquals("EMPLOYEE 01", payments[0].getReceiverAddress().getName());
        Assertions.assertEquals("34567890", payments[0].getSenderAccount().getUnformatted());
        Assertions.assertEquals("WAGES Payment", payments[0].getSenderAddress().getName());
        Assertions.assertEquals(1, payments[0].getStructuredReferences().length);
        Assertions.assertEquals(ReferenceType.Isr, payments[0].getStructuredReferences()[0].getType());
        Assertions.assertEquals("000005991", payments[0].getStructuredReferences()[0].getUnformatted());

        Assertions.assertEquals("234567890", payments[11].getReceiverAccount().getUnformatted());
        Assertions.assertEquals(446677.88, payments[11].getAmount());
        Assertions.assertEquals("Company Account", payments[11].getReceiverAddress().getName());
        Assertions.assertEquals("34567890", payments[11].getSenderAccount().getUnformatted());
        Assertions.assertEquals("WAGES Payment", payments[11].getSenderAddress().getName());
        Assertions.assertEquals(1, payments[11].getStructuredReferences().length);
        Assertions.assertEquals(ReferenceType.Isr, payments[11].getStructuredReferences()[0].getType());
        Assertions.assertEquals("CONTRA WAGES", payments[11].getStructuredReferences()[0].getUnformatted());
    }
}
