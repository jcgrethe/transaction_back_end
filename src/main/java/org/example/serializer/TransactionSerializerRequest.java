package org.example.serializer;

import static java.util.Optional.empty;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.model.Transaction;

public class TransactionSerializerRequest {

  @JsonProperty("parent_id")
  private Optional<Long> parentId = empty();

  @NotNull
  @Positive
  private BigDecimal amount;

  @NotBlank
  @Size(max = 20)
  private String type;

  public TransactionSerializerRequest() {

  }

  public TransactionSerializerRequest(Transaction transaction) {
    this.amount = transaction.getAmount();
    this.type = transaction.getType();
    this.parentId = transaction.getParentId();
  }

  public TransactionSerializerRequest(BigDecimal amount, String type, Optional<Long> parentId) {
    this.parentId = parentId;
    this.amount = amount;
    this.type = type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getType() {
    return type;
  }

  public Optional<Long> getParentId() {
    return parentId;
  }
}
