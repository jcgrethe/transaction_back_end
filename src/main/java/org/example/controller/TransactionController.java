package org.example.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.persistance.TransactionsHolder;
import org.example.serializer.TransactionSerializerRequest;
import org.example.serializer.TransactionSerializerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

  private static final TransactionsHolder transactionHolder = TransactionsHolder.get();

  @GetMapping("/{id}")
  public ResponseEntity<TransactionSerializerResponse> findTransactionById(@PathVariable(value = "id") long id) {
    Optional<TransactionSerializerResponse> transaction = transactionHolder.getTransaction(id);

    return transaction.map(transactionSerializerResponse -> ResponseEntity.ok().body(transactionSerializerResponse))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransactionSerializerResponse.class)))
  public ResponseEntity<TransactionSerializerResponse> createTransaction(@PathVariable(value = "id") long id,
                                                                         @Validated @RequestBody
                                                                         TransactionSerializerRequest transactionSerializerRequest) {
    return ResponseEntity.status(CREATED).body(transactionHolder.addTransaction(id, transactionSerializerRequest));
  }

  @GetMapping("/sum/{id}")
  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Long.class),
      examples = {@ExampleObject(value = "100")}))
  public ResponseEntity<String> getSum(@PathVariable(value = "id") long id) {
    try {
      return ResponseEntity.ok().body("{ sum: " + transactionHolder.getSum(id) + "}");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/types/{type}")
  public ResponseEntity<List<Long>> getByType(@PathVariable(value = "type") String type) {
    if (isBlank(type)) {
      ResponseEntity.badRequest();
    }
    return ResponseEntity.ok().body(transactionHolder.getIdsByType(type));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<TransactionSerializerResponse> rollback(@PathVariable(value = "id") long id) {
    return ResponseEntity.ok().body(transactionHolder.rollbackTransaction(id));
  }
}
