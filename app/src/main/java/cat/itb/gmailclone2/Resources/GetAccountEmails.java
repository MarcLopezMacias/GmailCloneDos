package cat.itb.gmailclone2.Resources;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class GetAccountEmails {
    static Account[] getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] account = getAccount(accountManager);

        if (account.length == 0) {
            return null;
        } else {
            return account;
        }
    }

    public static Account[] getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return accounts;
    }
}
