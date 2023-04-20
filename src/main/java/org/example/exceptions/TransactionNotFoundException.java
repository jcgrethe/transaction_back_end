package org.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TransactionNotFoundException extends IllegalArgumentException {
  public TransactionNotFoundException(String s) {
    super(s);
  }

  public TransactionNotFoundException() {
    super();
  }

}
