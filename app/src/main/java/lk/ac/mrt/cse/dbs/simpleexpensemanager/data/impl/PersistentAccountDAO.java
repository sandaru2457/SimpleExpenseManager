package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO extends SQLiteOpenHelper implements AccountDAO {
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "180321J";

    public PersistentAccountDAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.onCreate(this.getReadableDatabase());
    }
    @Override
    public List<String> getAccountNumbersList() {
        ArrayList<String> accountNumberList = new ArrayList<String>();

        SQLiteDatabase dbase = this.getReadableDatabase();
        Cursor res = dbase.rawQuery("select accountNo from account", null);
        res.moveToFirst();

        while (res.isAfterLast()==false){
            accountNumberList.add(res.getString(res.getColumnIndex("accountNo")));
            res.moveToNext();
        }

        return accountNumberList;
    }

    @Override
    public void onCreate(SQLiteDatabase dbase) {
        String sql = "CREATE TABLE IF NOT EXISTS account ( "+
                        "accountNo TEXT PRIMARY KEY,"+
                        "bankName TEXT,"+
                        "accountHolderName TEXT,"+
                        "initialBalance REAL)";
        dbase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase dbase, int oldVersion, int newVersion) {
        dbase.execSQL("DROP TABLE IF EXISTS account");
        onCreate(dbase);
    }

   

    @Override
    public List<Account> getAccountsList() {
        ArrayList<Account> accountList = new ArrayList<Account>();

        SQLiteDatabase dbase = this.getReadableDatabase();
        Cursor res = dbase.rawQuery("select * from account", null);
        res.moveToFirst();

        while (res.isAfterLast()==false){
            Account account = new Account();
            account.setAccountNo(res.getString(res.getColumnIndex("accountNo")));
            account.setBankName(res.getString(res.getColumnIndex("bankName")));
            account.setAccountHolderName(res.getString(res.getColumnIndex("accountHolderName")));
            account.setBalance(res.getFloat(res.getColumnIndex("initialBalance")));
            accountList.add(account);
            res.moveToNext();
        }

        return  accountList;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase dbase = this.getReadableDatabase();
        Cursor res = dbase.rawQuery("select * from account where accountNo="+accountNo+"", null);
        if (res.moveToFirst()) {
            Account account = new Account();
            account.setAccountNo(res.getString(res.getColumnIndex("accountNo")));
            account.setBankName(res.getString(res.getColumnIndex("bankName")));
            account.setAccountHolderName(res.getString(res.getColumnIndex("accountHolderName")));
            account.setBalance(res.getFloat(res.getColumnIndex("initialBalance")));
            return account;
        }
        String msg = "Account " + accountNo + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase dbase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo", account.getAccountNo());
        contentValues.put("bankName", account.getBankName());
        contentValues.put("accountHolderName", account.getAccountHolderName());
        contentValues.put("initialBalance", account.getBalance());

        dbase.insert("account", null, contentValues);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase dbase = this.getWritableDatabase();
        dbase.delete("account", "accountNo = ?", new String[]{accountNo});
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        Account account = this.getAccount(accountNo);
        // specific implementation based on the transaction type
        switch (expenseType) {
            case EXPENSE:
                account.setBalance(account.getBalance() - amount);
                break;
            case INCOME:
                account.setBalance(account.getBalance() + amount);
                break;
        }
        SQLiteDatabase dbase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("initialBalance", account.getBalance());

        dbase.update("account",  contentValues, "accountNo = ?", new String[]{accountNo});
    }

}
