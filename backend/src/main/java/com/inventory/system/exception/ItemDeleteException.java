package com.inventory.system.exception;

public class ItemDeleteException extends RuntimeException {
    private final int transactionCount;

    public ItemDeleteException(String message, int transactionCount) {
        super(message);
        this.transactionCount = transactionCount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}
