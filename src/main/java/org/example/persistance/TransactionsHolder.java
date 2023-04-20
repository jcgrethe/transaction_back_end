package org.example.persistance;

import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.example.exceptions.AlreadyCreatedTransactionException;
import org.example.exceptions.AlreadyRollbackedException;
import org.example.exceptions.TransactionNotFoundException;
import org.example.model.Transaction;
import org.example.serializer.TransactionSerializerRequest;
import org.example.serializer.TransactionSerializerResponse;

public class TransactionsHolder {

  private final Map<Long, Transaction> transactionMap = new ConcurrentHashMap<>();
  private final Map<String, List<Long>> transactionByTypeMap = new ConcurrentHashMap<>();
  private static final TransactionsHolder transactions = new TransactionsHolder();

  public static TransactionsHolder get() {
    return transactions;
  }

  private TransactionsHolder() {
  }

  public TransactionSerializerResponse addTransaction(long id, TransactionSerializerRequest transactionSerializerRequest) {
    Transaction transaction =
        new Transaction(id, transactionSerializerRequest.getAmount(), transactionSerializerRequest.getType(),
                        transactionSerializerRequest.getParentId());
    addTransaction(transaction);
    return new TransactionSerializerResponse(transactionMap.get(id));
  }

  private void addTransaction(Transaction transaction) {
    runSynchronized(() -> {
      if (transactionMap.containsKey(transaction.getId())) {
        throw new AlreadyCreatedTransactionException();
      }

      Optional<Transaction> parent = transaction.getParentId().map(parentId -> {
        if (!transactionMap.containsKey(parentId)) {
          throw new TransactionNotFoundException("Parent id not found: " + parentId);
        }
        Transaction parentTransaction = transactionMap.get(parentId);
        if (!transactionMap.get(parentId).isActive()) {
          throw new AlreadyRollbackedException();
        }
        return  parentTransaction;
      });

      transactionMap.put(transaction.getId(), transaction);
      parent.ifPresent(p -> p.addChild(transaction));

      if (!transactionByTypeMap.containsKey(transaction.getType())) {
        transactionByTypeMap.put(transaction.getType(), synchronizedList(new LinkedList<>()));
      }

      transactionByTypeMap.get(transaction.getType()).add(transaction.getId());
    });
  }

  public Optional<TransactionSerializerResponse> getTransaction(long id) {
    if (!transactionMap.containsKey(id)) {
      return empty();
    }
    return of(new TransactionSerializerResponse(transactionMap.get(id)));
  }

  public BigDecimal getSum(long id) {
    if (!transactionMap.containsKey(id)) {
      throw new TransactionNotFoundException();
    }
    return getSum(transactionMap.get(id));
  }

  public List<Long> getIdsByType(String type) {
    return unmodifiableList(transactionByTypeMap.getOrDefault(type, emptyList()));
  }

  public TransactionSerializerResponse rollbackTransaction(long id) {
    if (!transactionMap.containsKey(id)) {
      throw new TransactionNotFoundException();
    }
    Transaction transaction = transactionMap.get(id);

    runSynchronized(() -> {
      if (!transaction.isActive()) {
        throw new AlreadyRollbackedException();
      }
      rollbackTransactionsChilds(transaction);
    });
    return new TransactionSerializerResponse(transaction);
  }

  //just for testing
  public void clearTransactionMap() {
    transactionMap.clear();
    transactionByTypeMap.clear();
  }

  private BigDecimal getSum(Transaction transaction) {
    AtomicReference<BigDecimal> currentAmount = new AtomicReference<>(new BigDecimal(0));
    runSynchronized(() -> {
      if (!transaction.isActive()) {
        return;
      }
      transaction.getChilds().forEach(child -> currentAmount.set(currentAmount.get().add(getSum(child))));
      currentAmount.set(currentAmount.get().add(transaction.getAmount()));
    });
    return currentAmount.get();
  }

  private List<Transaction> rollbackTransactionsChilds(Transaction transaction) {
    List<Transaction> transactionsRollbacked = new LinkedList<>();
    try {
      transaction.getChilds().forEach(child -> {
        if (child.isActive()) {
          transactionsRollbacked.addAll(rollbackTransactionsChilds(child));
        }
      });
      transactionsRollbacked.add(transaction);
      transaction.rollback();
    } catch (Exception e) {
      transactionsRollbacked.forEach(Transaction::setActive);
      throw e;
    }
    return transactionsRollbacked;
  }

  private void runSynchronized(Runnable runnable) {
    synchronized (TransactionsHolder.class) {
      runnable.run();
    }
  }
}