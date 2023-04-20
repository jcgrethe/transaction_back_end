package org.example.serializer;

import java.math.BigDecimal;

public class SumSerializer {

  private final BigDecimal sum;

  public SumSerializer(BigDecimal sum) {
    this.sum = sum;
  }

  public BigDecimal getSum() {
    return sum;
  }
}
