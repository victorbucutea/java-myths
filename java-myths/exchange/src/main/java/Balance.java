import exchange.core2.core.ExchangeApi;
import exchange.core2.core.common.api.reports.SingleUserReportQuery;
import exchange.core2.core.common.api.reports.SingleUserReportResult;
import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Balance {


    public static void print(ExchangeApi api, long user) {

        Future<SingleUserReportResult> report1 = api.processReport(new SingleUserReportQuery(user), 0);
        try {
            IntLongHashMap accounts = report1.get().getAccounts();
            long ltc = accounts.get(ExchangeServer.CURRENCY_LTC);
            long xbt = accounts.get(ExchangeServer.CURRENCY_XBT);
            System.out.println("User " + user + " { LTC = " + ltc + ", BTC = " + xbt + " }");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void printAllBalances(ExchangeApi api) {
        for (int i = 1; i < Trader.traderId.intValue(); i++) {
            print(api, i);
        }
    }
}
