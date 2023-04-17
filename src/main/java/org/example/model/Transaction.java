package org.example.model;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.example.exceptions.InvalidNumberFormatException;
import org.example.exceptions.InvalidTypeException;
import org.example.exceptions.NegativeIdException;

public class Transaction {

  private final long id;
  private final BigDecimal amount;
  private final Optional<Transaction> parent;
  private final String type;
  private AtomicBoolean isActive = new AtomicBoolean(true);

  public Transaction(long id, BigDecimal amount, String type) {
    this(id, amount, type, null);
  }

  public Transaction(long id, BigDecimal amount, String type, Transaction parent) {
    validateId(id);
    validateDecimals(amount);
    validateType(type);
    this.id = id;
    this.amount = amount;
    this.parent = ofNullable(parent);
    this.type = type;
  }

  public long getId() {
    return id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Optional<Transaction> getParent() {
    return parent;
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

  private void validateId(long id) {
    if (id <= 0) {
      throw new NegativeIdException();
    }
  }

  private void validateDecimals(BigDecimal amount) {
    requireNonNull(amount);
    if (amount.scale() > 2) {
      throw new InvalidNumberFormatException();
    }
  }

  private void validateType(String type) {
    if (isBlank(type)) {
      throw new InvalidTypeException();
    }
  }

}
