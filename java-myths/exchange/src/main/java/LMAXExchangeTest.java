import exchange.core2.core.ExchangeApi;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportQuery;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportResult;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class LMAXExchangeTest {


    public static Executor executor = Executors.newFixedThreadPool(100);

    public static void main(String[] args) throws ExecutionException, InterruptedException {



        ExchangeApi api = ExchangeServer.createCore();

        long start = System.currentTimeMillis();
        for (int i = 1; i < 300; i++) {
            executor.execute(() -> {

                long user = Trader.registerUser(api);

                // user deposits 20_000 LTC
                Trader.deposit(api, user, 2_000_000_000L, ExchangeServer.CURRENCY_LTC);

                // second user deposits 10 BTC
                Trader.deposit(api, user, 10_000_000L, ExchangeServer.CURRENCY_XBT);
            });
        }


        // check balances
//        Balance.printAllBalances(api);

        // first user places Good-till-Cancel Bid order
        // he assumes BTC-LTC exchange rate 154 LTC for 1 BTC
        // bid price for 1 lot (0.01BTC) is 1.54 LTC => 1_5400_0000 litoshi => 10K * 15_400 (in price steps)
        for (int i = 0; i < 1_500_000; i++) {
            int user = i % 300;
            int factor = Math.abs(ThreadLocalRandom.current().nextInt()) % 15_400;
            executor.execute(() -> {
                Trader.placeBid(api, user, 12L, factor, factor + 100);
            });
        }


        // second user places Immediate-or-Cancel Ask (Sell) order
        // he assumes wost rate to sell 152.5 LTC for 1 BTC
        for (int i = 0; i < 1_500_000; i++) {
            int user = i % 300;
            int price = Math.abs(ThreadLocalRandom.current().nextInt()) % 15_300;
            executor.execute(() -> {
                Trader.placeOffer(api, user, 10L, price);
            });
        }


        // wait for a while until some of the bids and offers have been match.
        Thread.sleep(10_000L);


        // request order book
        api.requestOrderBookAsync(ExchangeServer.EXCHANGE_SYMBOL, 10);


        // check balances
//        Balance.printAllBalances(api);

        // check fees collected
        Future<TotalCurrencyBalanceReportResult> totalsReport = api.processReport(new TotalCurrencyBalanceReportQuery(), 0);


        System.out.println("LTC fees collected after " + ExchangeServer.EventsHandler.noOfTrades + " trade events : "
                + totalsReport.get().getFees().get(ExchangeServer.CURRENCY_LTC));

        long end = System.currentTimeMillis();
        BigDecimal duration = BigDecimal.valueOf( ( (double) end - start) / 1000 ) ;
        System.out.println("Duration "+ duration.toPlainString() + " s");

    }
}
