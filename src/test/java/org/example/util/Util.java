package org.example.util;

import java.math.BigDecimal;

import org.example.model.Transaction;

public class Util {

  public static final long DEFAULT_ID = 1;
  public static final String DEFAULT_STRING_AMOUNT = "100.11";
  public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(DEFAULT_STRING_AMOUNT);

  public static final String DEFAULT_TYPE = "Shopping";

  public static Transaction createDefaultTransaction() {
    return new Transaction(DEFAULT_ID, DEFAULT_AMOUNT, DEFAULT_TYPE);
  }


}
