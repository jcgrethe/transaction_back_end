package org.example.serializer;

import static java.util.Optional.empty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.model.Transaction;

public class TransactionSerializerResponse extends TransactionSerializerRequest {

  private long id;

  private boolean isActive;

  private List<TransactionSerializerResponse> childs;

  public TransactionSerializerResponse() {}

  public TransactionSerializerResponse(Transaction transaction) {
    super(transaction);
    this.id = transaction.getId();
    this.isActive = transaction.isActive();
    childs = transaction.getChilds().stream().map(TransactionSerializerResponse::new).collect(Collectors.toList());
  }

  public long getId() {
    return id;
  }

  public boolean isActive() {
    return isActive;
  }

  public List<TransactionSerializerResponse> getChilds() {
    return childs;
  }

}
