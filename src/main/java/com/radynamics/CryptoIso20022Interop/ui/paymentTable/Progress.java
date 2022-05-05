package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

public class Progress {
    private int count;
    private int total;

    public Progress(int count, int total) {
        this.count = count;
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public int getTotal() {
        return total;
    }

    public boolean isFinished() {
        return getCount() == getTotal();
    }
}
