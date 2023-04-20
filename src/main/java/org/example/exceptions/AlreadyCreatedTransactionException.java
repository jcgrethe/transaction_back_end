package org.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AlreadyCreatedTransactionException extends IllegalArgumentException {

  public AlreadyCreatedTransactionException() {
    super("Transaction Id already created");
  }

}
