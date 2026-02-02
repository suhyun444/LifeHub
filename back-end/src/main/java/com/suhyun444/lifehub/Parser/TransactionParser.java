package com.suhyun444.lifehub.Parser;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import com.suhyun444.lifehub.Entity.Transaction;

public abstract class TransactionParser {
    abstract public List<Transaction> parse(Sheet sheet);
}
