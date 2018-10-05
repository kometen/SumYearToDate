package no.difi.statsregnskap;

import java.math.BigDecimal;

public class AccountingLine {

    String accountingLine;
    BigDecimal sum;

    AccountingLine(String a, BigDecimal b) {
        accountingLine = a;
        sum = b;
    }

    public String getAccountingLine() {
        return accountingLine;
    }

    public BigDecimal getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return accountingLine + ";" + sum.toString();
    }
}
