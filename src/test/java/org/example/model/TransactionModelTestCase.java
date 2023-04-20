package org.example.model;

import static org.example.util.Util.DEFAULT_AMOUNT;
import static org.example.util.Util.DEFAULT_ID;
import static org.example.util.Util.DEFAULT_TYPE;
import static org.example.util.Util.createDefaultTransaction;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;

import org.example.exceptions.InvalidAmountException;
import org.example.exceptions.InvalidNumberFormatException;
import org.example.exceptions.InvalidTypeException;
import org.example.exceptions.NegativeIdException;
import org.junit.Test;


public class TransactionModelTestCase {

  @Test
  public void createSimpleTransaction() {
    Transaction transaction = createDefaultTransaction();
    assertThat(transaction.getId(), is(DEFAULT_ID));
    assertThat(transaction.getAmount(), is(DEFAULT_AMOUNT));
    assertThat(transaction.getType(), is(DEFAULT_TYPE));
  }

  @Test(expected = NegativeIdException.class)
  public void createTransactionWithNegativeId(){
    new Transaction(-1, DEFAULT_AMOUNT, DEFAULT_TYPE);
  }

  @Test(expected = InvalidAmountException.class)
  public void createTransactionWithNegativeAmount() {
    new Transaction(DEFAULT_ID, new BigDecimal("-1"), DEFAULT_TYPE);
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
}
