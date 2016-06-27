package com.parasoft.parabank.dao.jdbc;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.test.annotation.Rollback;

import com.parasoft.parabank.dao.TransactionDao;
import com.parasoft.parabank.domain.Transaction;
import com.parasoft.parabank.domain.Transaction.TransactionType;
import com.parasoft.parabank.domain.TransactionCriteria;
import com.parasoft.parabank.domain.TransactionCriteria.SearchType;
import com.parasoft.parabank.test.util.AbstractParaBankDataSourceTest;

// @SuppressWarnings("deprecation")
@Rollback
public class JdbcTransactionDaoTest extends AbstractParaBankDataSourceTest {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(JdbcTransactionDaoTest.class);

    //private static final int ACCOUNT_ID = 201;
    private static final int ACCOUNT_ID = 12456;

    private static final TransactionType TYPE = TransactionType.Debit;

    private static final Date DATE = new Date(22222);

    private static final BigDecimal AMOUNT = new BigDecimal("33333.00");

    private static final String DESCRIPTION = "44444";

    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;

    private Transaction transaction;

    @Override
    public void onSetUp() throws Exception {
        super.onSetUp();
        transaction = new Transaction();
        transaction.setAccountId(ACCOUNT_ID);
        transaction.setType(TYPE);
        transaction.setDate(DATE);
        transaction.setAmount(AMOUNT);
        transaction.setDescription(DESCRIPTION);
    }

    //    @Override
    //    public void onSetUpInTransaction() throws Exception {
    //        super.onSetUpInTransaction();
    //        super.executeSqlScript("classpath:com/parasoft/parabank/dao/jdbc/sql/insertCustomer.sql", true);
    //        super.executeSqlScript("classpath:com/parasoft/parabank/dao/jdbc/sql/insertAccount.sql", true);
    //    }

    public void setTransactionDao(final TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    @Test
    @Rollback
    public void testCreateTransaction() {
        final int id = transactionDao.createTransaction(this.transaction);
        assertEquals("wrong expected id?", 14476, id);

        final Transaction transaction = transactionDao.getTransaction(id);
        assertFalse(this.transaction == transaction);
        assertEquals(this.transaction, transaction);
    }

    @Test
    public void testGetTransaction() {
        Transaction transaction = transactionDao.getTransaction(12256);
        assertEquals(12256, transaction.getId());
        assertEquals(12345, transaction.getAccountId());
        assertEquals(TransactionType.Debit, transaction.getType());
        assertEquals("2009-12-12", transaction.getDate().toString());
        // changed comparison to floats to remove the precision 1351.12 v.s.
        // 1351.1200 issue
        assertEquals(new BigDecimal("100.00").floatValue(), transaction.getAmount().floatValue(), 0.0001f);
        assertEquals("Check # 1211", transaction.getDescription());

        try {
            transaction = transactionDao.getTransaction(-1);
            fail("did not throw expected DataAccessException");
        } catch (final DataAccessException e) {
        }
    }

    @Test
    public void testGetTransactionsForAccount() {
        List<Transaction> transactions = transactionDao.getTransactionsForAccount(12345);
        assertEquals(7, transactions.size());

        transactions = transactionDao.getTransactionsForAccount(-1);
        assertEquals(0, transactions.size());
    }

    @Test
    public void testGetTransactionsForAccountWithActivityCriterion() {
        final TransactionCriteria criteria = new TransactionCriteria();
        criteria.setSearchType(SearchType.ACTIVITY);

        List<Transaction> transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(7, transactions.size());

        criteria.setMonth("January");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setMonth("December");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(2, transactions.size());

        criteria.setMonth("All");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(7, transactions.size());

        criteria.setMonth("Invalid");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(7, transactions.size());

        criteria.setTransactionType("Debit");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(6, transactions.size());

        criteria.setTransactionType("Credit");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(1, transactions.size());

        criteria.setTransactionType("All");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(7, transactions.size());

        criteria.setMonth("December");
        criteria.setTransactionType("Debit");
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(1, transactions.size());
    }

    @Test
    public void testGetTransactionsForAccountWithAmountCriterion() {
        final TransactionCriteria criteria = new TransactionCriteria();
        criteria.setSearchType(SearchType.AMOUNT);

        List<Transaction> transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setAmount(new BigDecimal(1000));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(3, transactions.size());

        criteria.setAmount(new BigDecimal(-1));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());
    }

    @Test
    public void testGetTransactionsForAccountWithDateCriterion() {
        final TransactionCriteria criteria = new TransactionCriteria();
        criteria.setSearchType(SearchType.DATE);

        List<Transaction> transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setOnDate(convertDate("2000-01-01")); //(new Date(100, 0, 1));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setOnDate(convertDate("2010-08-23")); //(new Date(110, 7, 23));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(2, transactions.size());
    }

    @Test
    public void testGetTransactionsForAccountWithDateRangeCriterion() {
        final TransactionCriteria criteria = new TransactionCriteria();
        criteria.setSearchType(SearchType.DATE_RANGE);

        List<Transaction> transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setFromDate(convertDate("2000-01-01")); //(new Date(100, 0, 1));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setToDate(convertDate("2010-12-31")); //(new Date(110, 11, 31));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(7, transactions.size());

        criteria.setFromDate(convertDate("2000-01-01")); //(new Date(100, 0, 1));
        criteria.setToDate(convertDate("2010-12-31")); //(new Date(110, 11, 31));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(7, transactions.size());

        criteria.setFromDate(convertDate("2010-08-01")); //(new Date(110, 7, 1));
        criteria.setToDate(convertDate("2010-08-31")); //(new Date(110, 7, 31));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(5, transactions.size());

        criteria.setFromDate(convertDate("2010-12-31")); //(new Date(110, 11, 31));
        criteria.setToDate(convertDate("2000-01-01")); //(new Date(100, 0, 1));
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());
    }

    @Test
    public void testGetTransactionsForAccountWithIdCriterion() {
        final TransactionCriteria criteria = new TransactionCriteria();
        criteria.setSearchType(SearchType.ID);

        List<Transaction> transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setTransactionId(12345);
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(0, transactions.size());

        criteria.setTransactionId(14143);
        transactions = transactionDao.getTransactionsForAccount(12345, criteria);
        assertEquals(1, transactions.size());
    }
}
