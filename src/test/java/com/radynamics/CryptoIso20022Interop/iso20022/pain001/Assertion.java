package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import org.junit.Assert;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Assertion {
    static void assertEquals(Address actual, Address expected) {
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getStreet(), actual.getStreet());
        Assert.assertEquals(expected.getZip(), actual.getZip());
        Assert.assertEquals(expected.getCity(), actual.getCity());
    }

    static void assertEquals(Payment t, String receiverAccount, String receiverWallet, double amount) {
        assertEquals(t, receiverAccount, receiverWallet, amount, null, null);
    }

    static void assertEquals(Payment t, String receiverAccount, String receiverWallet, double amount, ReferenceType type, String referenceUnformatted) {
        assertEquals(t, "sender_CH5481230000001998736", receiverAccount, receiverWallet, amount, type, referenceUnformatted);
    }

    static void assertEquals(Payment t, String senderWallet, String receiverAccount, String receiverWallet, double amount, ReferenceType type, String referenceUnformatted) {
        if (senderWallet == null) {
            assertNull(t.getSenderWallet());
        } else {
            assertNotNull(t.getSenderWallet());
            Assert.assertEquals(senderWallet, t.getSenderWallet().getPublicKey());
        }
        Assert.assertEquals(amount, t.getLedgerAmountSmallestUnit(), 0);
        Assert.assertEquals("TEST", t.getLedgerCcy());
        assertNotNull(t.getReceiverAccount());
        Assert.assertEquals(receiverAccount, t.getReceiverAccount().getUnformatted());
        if (receiverWallet == null) {
            assertNull(t.getReceiverWallet());
        } else {
            assertNotNull(t.getReceiverWallet());
            Assert.assertEquals(receiverWallet, t.getReceiverWallet().getPublicKey());
        }
        assertNull(t.getId());
        assertNull(t.getInvoiceId());
        assertNotNull(t.getMessages());
        Assert.assertEquals(0, t.getMessages().length);
        assertNotNull(t.getStructuredReferences());
        if (referenceUnformatted == null) {
            Assert.assertEquals(0, t.getStructuredReferences().length);
        } else {
            Assert.assertEquals(1, t.getStructuredReferences().length);
            Assert.assertEquals(type, t.getStructuredReferences()[0].getType());
            Assert.assertEquals(referenceUnformatted, t.getStructuredReferences()[0].getUnformatted());
        }
    }
}
