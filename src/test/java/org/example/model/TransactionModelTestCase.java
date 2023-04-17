package org.example.model;

import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;

import org.example.exceptions.InvalidNumberFormatException;
import org.example.exceptions.InvalidTypeException;
import org.example.exceptions.NegativeIdException;
import org.junit.Test;


public class TransactionModelTestCase {

  private static final long DEFAULT_ID = 1;
  private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(100);
  private static final String DEFAULT_TYPE = "Shopping";


  @Test
  public void createSimpleTransaction() {
    Transaction transaction = createDefaultTransaction();
    assertThat(transaction.getId(), is(DEFAULT_ID));
    assertThat(transaction.getAmount(), is(DEFAULT_AMOUNT));
    assertThat(transaction.getType(), is(DEFAULT_TYPE));
    assertThat(transaction.getParent(), is(empty()));
  }

  @Test
  public void createSimpleTransactionWithParent() {
    Transaction transactionParent = createDefaultTransaction();
    long transactionId = 2;
    Transaction transaction = new Transaction(transactionId, DEFAULT_AMOUNT, DEFAULT_TYPE, transactionParent);
    assertThat(transaction.getId(), is(transactionId));
    assertThat(transaction.getAmount(), is(DEFAULT_AMOUNT));
    assertThat(transaction.getType(), is(DEFAULT_TYPE));
    assertThat(transaction.getParent().isPresent(), is(true));
    assertThat(transaction.getParent().get().getId(), is(DEFAULT_ID));
  }

  @Test(expected = NegativeIdException.class)
  public void createTransactionWithNegativeId(){
    new Transaction(-1, DEFAULT_AMOUNT, DEFAULT_TYPE);
  }

  @Test(expected = InvalidNumberFormatException.class)
  public void createTransactionWithMoreThanTwoDecimals() {
    new Transaction(DEFAULT_ID, new BigDecimal("123.111"), DEFAULT_TYPE);
  }

  @Test(expected = InvalidTypeException.class)
  public void createTransactionWithEmptyType(){
    new Transaction(DEFAULT_ID, DEFAULT_AMOUNT, "");
  }

  @Test(expected = InvalidTypeException.class)
  public void createTransactionWithNullType(){
    new Transaction(DEFAULT_ID, DEFAULT_AMOUNT, null);
  }

  @Test
  public void rollbackActiveTransaction() {
    Transaction transaction = createDefaultTransaction();
    transaction.rollback();
    assertThat(transaction.isActive(), is(false));
  }

  @Test(expected = IllegalStateException.class)
  public void rollbackNonActiveTransaction() {
    Transaction transaction = createDefaultTransaction();
    transaction.rollback();
    assertThat(transaction.isActive(), is(false));
    transaction.rollback();
  }

  private Transaction createDefaultTransaction() {
    return createDefaultTransaction(null);
  }
  private Transaction createDefaultTransaction(Transaction parent) {
    return new Transaction(DEFAULT_ID, DEFAULT_AMOUNT, DEFAULT_TYPE, parent);
  }


}
