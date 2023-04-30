package com.radynamics.dallipay.cryptoledger.signing;

import javax.swing.*;
import java.net.URI;

public class TransactionSubmitterInfo {
    private String title;
    private String description;
    private URI detailUri;
    private boolean isNotRecommended;
    private int order = 0;
    private Icon icon;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URI getDetailUri() {
        return detailUri;
    }

    public void setDetailUri(URI detailUri) {
        this.detailUri = detailUri;
    }

    public boolean isNotRecommended() {
        return isNotRecommended;
    }

    public void setNotRecommended(boolean notRecommended) {
        isNotRecommended = notRecommended;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "TransactionSubmitterInfo{" + "title='" + title + '\'' + '}';
    }
}
