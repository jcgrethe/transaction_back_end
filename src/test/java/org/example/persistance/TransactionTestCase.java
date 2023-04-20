package org.example.persistance;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.example.util.Util.DEFAULT_AMOUNT;
import static org.example.util.Util.DEFAULT_ID;
import static org.example.util.Util.DEFAULT_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.example.exceptions.AlreadyCreatedTransactionException;
import org.example.exceptions.AlreadyRollbackedException;
import org.example.exceptions.TransactionNotFoundException;
import org.example.serializer.TransactionSerializerRequest;
import org.example.serializer.TransactionSerializerResponse;
import org.junit.Before;
import org.junit.Test;

public class TransactionTestCase {

  private final TransactionsHolder transactionsHolder = TransactionsHolder.get();

  @Before
  public void before() {
    transactionsHolder.clearTransactionMap();
  }

  @Test
  public void addSimpleTransaction() {
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    TransactionSerializerResponse transaction = transactionsHolder.getTransaction(DEFAULT_ID).get();
    assertThat(transaction.getId(), is(DEFAULT_ID));
    assertThat(transaction.isActive(), is(true));
  }

  @Test
  public void addSimpleTransactionWithParent() {
    long id = 2;
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.addTransaction(id, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));

    TransactionSerializerResponse transaction = transactionsHolder.getTransaction(id).get();
    assertThat(transaction.getId(), is(id));
    assertThat(transaction.isActive(), is(true));

    TransactionSerializerResponse parent = transactionsHolder.getTransaction(DEFAULT_ID).get();
    assertThat(parent.getChilds(), hasSize(1));
    assertThat(parent.getChilds().get(0).getId(), is(id));
  }

  @Test(expected = TransactionNotFoundException.class)
  public void addSimpleTransactionWithNonexistentParent() {
    transactionsHolder.addTransaction(2, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
  }

  @Test(expected = AlreadyCreatedTransactionException.class)
  public void addSimpleTransactionWithRepeatedId() {
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
  }

  @Test(expected = TransactionNotFoundException.class)
  public void addSimpleTransactionWithParentIdEqualToId() {
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
  }

  @Test(expected = AlreadyRollbackedException.class)
  public void addSimpleTransactionWithParentRollbacked() {
    long id = 2;
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.rollbackTransaction(DEFAULT_ID);
    transactionsHolder.addTransaction(id, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
  }

  @Test
  public void getSumTransactions() {
    int id = 1;
    int amount = createMultipleTransactionChilds(id, 25);
    assertThat(transactionsHolder.getSum(id), is(new BigDecimal(amount)));
  }

  @Test (expected = TransactionNotFoundException.class)
  public void getSumNonexistentTransactions() {
    transactionsHolder.getSum(1);
  }

  @Test
  public void getSumOfRollbackedTransactionMustReturnZero() {
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.rollbackTransaction(DEFAULT_ID);
    assertThat(transactionsHolder.getSum(DEFAULT_ID), is(ZERO));
  }

  @Test
  public void getSumOfRollbackedTransactionChildMustReturnOnlyTheParentValue() {
    long id = 2;
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.addTransaction(id, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
    transactionsHolder.rollbackTransaction(id);
    assertThat(transactionsHolder.getSum(DEFAULT_ID), is(DEFAULT_AMOUNT));
  }

  @Test
  public void getTransactionByType() {
    int startId = 1;
    int totalAmount = createMultipleTransactionChilds(startId, 25);
    List<Long> idsByType = transactionsHolder.getIdsByType(DEFAULT_TYPE);
    assertThat(idsByType, hasSize(totalAmount));
    idsByType.forEach(id ->
                          assertThat(id, is(both(greaterThanOrEqualTo((long) startId)).and(lessThanOrEqualTo((long) startId + totalAmount)))));
  }

  @Test
  public void getTransactionByNonexistentType() {
    createMultipleTransactionChilds(1, 2);
    List<Long> idsByType = transactionsHolder.getIdsByType("OTHER");
    assertThat(idsByType, hasSize(0));
  }

  @Test
  public void rollbackTransaction() {
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.rollbackTransaction(DEFAULT_ID);
    assertTransactionIsRollbacked(DEFAULT_ID);
  }

  @Test
  public void rollbackParentTransaction() {
    long id = 2;
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.addTransaction(id, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
    transactionsHolder.rollbackTransaction(DEFAULT_ID);

    assertTransactionIsRollbacked(DEFAULT_ID);
    assertTransactionIsRollbacked(id);
  }

  @Test
  public void rollbackChildTransaction() {
    long id = 2;
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.addTransaction(id, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
    transactionsHolder.rollbackTransaction(id);

    assertTransactionIsNotRollbacked(DEFAULT_ID);
    assertTransactionIsRollbacked(id);
  }

  @Test
  public void rollbackChildTransactionAndThenParent() {
    long id = 2;
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.addTransaction(id, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE, of(DEFAULT_ID)));
    transactionsHolder.rollbackTransaction(id);

    assertTransactionIsNotRollbacked(DEFAULT_ID);
    assertTransactionIsRollbacked(id);

    transactionsHolder.rollbackTransaction(DEFAULT_ID);
    assertTransactionIsRollbacked(DEFAULT_ID);
  }

  @Test(expected = AlreadyRollbackedException.class)
  public void rollbackTransactionAlreadyRollbacked() {
    transactionsHolder.addTransaction(DEFAULT_ID, createTransaction(DEFAULT_AMOUNT, DEFAULT_TYPE));
    transactionsHolder.rollbackTransaction(DEFAULT_ID);
    transactionsHolder.rollbackTransaction(DEFAULT_ID);
  }

  private int createMultipleTransactionChilds(int initialId, int amount) {
    int currentId = initialId;
    int currentValue = 1;
    transactionsHolder.addTransaction(currentId++, createTransaction(new BigDecimal(1), DEFAULT_TYPE));
    for (int p = initialId; p < initialId + amount; p++) {
      for (int i = 0; i < amount; i++) {
        transactionsHolder.addTransaction(currentId++, createTransaction(new BigDecimal(1), DEFAULT_TYPE, of((long) p)));
        currentValue++;
      }
    }
    return currentValue;
  }

  private TransactionSerializerRequest createTransaction(BigDecimal amount, String type) {
    return createTransaction(amount, type, empty());
  }


  private TransactionSerializerRequest createTransaction(BigDecimal amount, String type, Optional<Long> parentId) {
    return new TransactionSerializerRequest(amount, type, parentId);
  }

  private void assertTransactionIsRollbacked(long id) {
    TransactionSerializerResponse parentTransaction = transactionsHolder.getTransaction(id).get();
    assertThat(parentTransaction.getId(), is(id));
    assertThat(parentTransaction.isActive(), is(false));
  }

  private void assertTransactionIsNotRollbacked(long id) {
    TransactionSerializerResponse parentTransaction = transactionsHolder.getTransaction(id).get();
    assertThat(parentTransaction.getId(), is(id));
    assertThat(parentTransaction.isActive(), is(true));
  }
}
