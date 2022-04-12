package com.socgen.exchange.plain;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class PlainExchangeTest {

    public static int CURRENCY_XBT = 1;
    public static int CURRENCY_LTC = 2;
    public static Executor executor = Executors.newFixedThreadPool(100);

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();
        // first user deposits 20 LTC
        for (int i = 1; i < 300; i++) {
            executor.execute(() -> {

                int user = PlainExchangeServer.registerUser();
                PlainExchangeServer.deposit(user, 2_000_000_000L, CURRENCY_LTC);
                PlainExchangeServer.deposit(user, 10_000_000_000L, CURRENCY_XBT);
            });
        }


        // first user places Good-till-Cancel Bid order
        // he assumes BTC-LTC exchange rate 154 LTC for 1 BTC
        // bid price for 1 lot (0.01BTC) is 1.54 LTC => 1_5400_0000 litoshi => 10K * 15_400 (in price steps)
        for (int i = 0; i < 1_500_000; i++) {
            int user = i % 300;
            int factor = Math.abs(ThreadLocalRandom.current().nextInt()) % 15_400;
            executor.execute(() -> {
                PlainExchangeServer.placeBid( user, 12L, factor, factor + 100);
            });
        }


        // second user places Immediate-or-Cancel Ask (Sell) order
        // he assumes wost rate to sell 152.5 LTC for 1 BTC
        for (int i = 0; i < 1_500_000; i++) {
            int user = i % 300;
            int price = Math.abs(ThreadLocalRandom.current().nextInt()) % 15_300;
            executor.execute(() -> {
                PlainExchangeServer.placeOffer( user, 10L, price);
            });
        }

        // wait for a while until some of the bids and offers have been match.
        Thread.sleep(10_000L);


        // request order book
        PlainExchangeServer.requestOrderBook();


        // check balances
        PlainExchangeServer.printAllBalances();

        long end = System.currentTimeMillis();
        BigDecimal duration = BigDecimal.valueOf( ( (double) end - start) / 1000 ) ;
        System.out.println("Duration "+ duration.toPlainString() + " s");
    }
}
