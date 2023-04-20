package org.example.model;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.example.exceptions.InvalidAmountException;
import org.example.exceptions.InvalidNumberFormatException;
import org.example.exceptions.InvalidTypeException;
import org.example.exceptions.NegativeIdException;

public class Transaction {

  private final long id;
  private final Optional<Long> parentId;
  private final BigDecimal amount;
  private final List<Transaction> childs;
  private final String type;
  private AtomicBoolean isActive = new AtomicBoolean(true);

  public Transaction(long id, BigDecimal amount, String type) {
    this(id, amount, type, empty());
  }

  public Transaction(long id, BigDecimal amount, String type, Optional<Long> parentId) {
    validateId(id);
    validateDecimals(amount);
    validateType(type);
    this.id = id;
    this.amount = amount;
    this.childs = synchronizedList(new LinkedList<>());
    this.type = type;
    this.parentId = parentId;
  }

  public long getId() {
    return id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getType() {
    return type;
  }

  public boolean isActive() {
    return isActive.get();
  }

  public void rollback() {
    if (!isActive.compareAndSet(true, false)) {
      throw new IllegalStateException("ERROR: Cannot rollback transaction id: " + id);
    }
  }

  public void setActive() {
    isActive.set(true);
  }

  private void validateId(long id) {
    if (id <= 0) {
      throw new NegativeIdException();
    }
  }

  private void validateDecimals(BigDecimal amount) {
    requireNonNull(amount);
    if (amount.compareTo(ZERO) < 0) {
      throw new InvalidAmountException();
    }
    if (amount.scale() > 2) {
      throw new InvalidNumberFormatException();
    }
  }

  private void validateType(String type) {
    if (isBlank(type)) {
      throw new InvalidTypeException();
    }
  }

  public List<Transaction> getChilds() {
    return childs;
  }

  public void addChild(Transaction transaction) {
    childs.add(transaction);
  }

  public Optional<Long> getParentId() {
    return parentId;
  }
}
