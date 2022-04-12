import exchange.core2.core.ExchangeApi;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportQuery;
import exchange.core2.core.common.api.reports.TotalCurrencyBalanceReportResult;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class LMAXSimpleExchangeTest {


    public static void main(String[] args) throws ExecutionException, InterruptedException {


        ExchangeApi api = ExchangeServer.createCore();

        long user = Trader.registerUser(api);
        long user2 = Trader.registerUser(api);

        // user deposits 20_000 LTC
        Trader.deposit(api, user, 2_000_000_000L, ExchangeServer.CURRENCY_LTC);

        // second user deposits 10 BTC
        Trader.deposit(api, user2, 10_000_000L, ExchangeServer.CURRENCY_XBT);


        // check balances
        // Balance.printAllBalances(api);

        // first user places Good-till-Cancel Bid order
        // he assumes BTC-LTC exchange rate 154 LTC for 1 BTC
        // bid price for 1 lot (0.01BTC) is 1.54 LTC => 1_5400_0000 litoshi => 10K * 15_400 (in price steps)
        Trader.placeBid(api, user, 1L, 15_200_000, 15_400_000);


        // second user places Immediate-or-Cancel Ask (Sell) order
        // he assumes wost rate to sell 152.5 LTC for 1 BTC
        Trader.placeOffer(api, user, 10L, 15_250_000);


        // request order book
        api.requestOrderBookAsync(ExchangeServer.EXCHANGE_SYMBOL, 10);


        // check balances
        // Balance.printAllBalances(api);

        // check fees collected
        Future<TotalCurrencyBalanceReportResult> totalsReport = api.processReport(new TotalCurrencyBalanceReportQuery(), 0);


        System.out.println("LTC fees collected after " + ExchangeServer.EventsHandler.noOfTrades + " trade events : "
                + totalsReport.get().getFees().get(ExchangeServer.CURRENCY_LTC));

    }
}
