package org.example.model;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Transaction {

  private final long id;
  private final double amount;
  private final Optional<Transaction> parent;
  private final String type;
  private AtomicBoolean isActive = new AtomicBoolean(true);

  public Transaction(long id, double amount, String type) {
    this(id, amount, type, null);
  }

  public Transaction(long id, double amount, String type, Transaction parent) {
    requireNonNull(type);
    this.id = id;
    this.amount = amount;
    this.parent = ofNullable(parent);
    this.type = type;
  }

  public long getId() {
    return id;
  }

  public double getAmount() {
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
    if (!isActive.get()) {
      throw new IllegalStateException("ERROR: Cannot rollback transaction id: " + id);
    }
    isActive.set(false);
  }
}
