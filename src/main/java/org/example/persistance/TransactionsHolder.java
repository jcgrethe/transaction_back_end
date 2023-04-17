package org.example.persistance;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.example.model.Transaction;

public class TransactionsHolder {

  private final Map<Long, Transaction> transactionMap = new ConcurrentHashMap<>();
  public static TransactionsHolder transactions = new TransactionsHolder();
  private TransactionsHolder() {
  }

  public void addTransaction(long id, double amount, String type) {
    addTransaction(id, amount, type, null);
  }
  public void addTransaction(long id, double amount, String type, long transactionParentId) {
    addTransaction(id, amount, type, transactionMap.get(transactionParentId));
  }

  public Transaction getTransaction(long id) {
    return transactionMap.get(id);
  }

  private void addTransaction(long id, double amount, String type, Transaction transactionParent) {
    Transaction transaction = new Transaction(id, amount, type, transactionParent);
    transactionMap.put(id, transaction);
  }

}