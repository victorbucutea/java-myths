import exchange.core2.core.ExchangeApi;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.ApiAddUser;
import exchange.core2.core.common.api.ApiAdjustUserBalance;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Trader {
    public static AtomicLong traderId = new AtomicLong(0);
    public static AtomicLong txId = new AtomicLong(1);
    private static AtomicInteger orderId = new AtomicInteger(1);

    public static long registerUser(ExchangeApi api) {

        // create user uid=301
        CompletableFuture<CommandResultCode> future = api.submitCommandAsync(ApiAddUser.builder()
                .uid(traderId.incrementAndGet())
                .build());

//        try {
//            System.out.println("ApiAddUser 1 result: " + future.get());
//
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
        return traderId.get();
    }

    public static void deposit(ExchangeApi api, long user, long amount, int currency) {

        CompletableFuture<CommandResultCode> future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                .uid(user)
                .currency(currency)
                .amount(amount)
                .transactionId(txId.incrementAndGet())
                .build());

//        try {
//            System.out.println("ApiAdjustUserBalance 1 result: " + future.get());
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    public static void placeBid(ExchangeApi api, long user, long lots, long price, long maxPrice) {
        api.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(user)
                .orderId(orderId.incrementAndGet())
                .price(price)
                .reservePrice(maxPrice) // can move bid order up to the 1.56 LTC, without replacing it
                .size(lots) // order size is 12 lots
                .action(OrderAction.BID)
                .orderType(OrderType.GTC) // Good-till-Cancel
                .symbol(ExchangeServer.EXCHANGE_SYMBOL)
                .build());
    }

    public static void placeOffer(ExchangeApi api, long user, long lots, long price) {
         api.submitCommandAsync(ApiPlaceOrder.builder()
                .uid(user)
                .orderId(orderId.incrementAndGet())
                .price(price)
                .size(lots) // order size is 10 lots
                .action(OrderAction.ASK)
                .orderType(OrderType.IOC)
                .symbol(ExchangeServer.EXCHANGE_SYMBOL)
                .build());
    }
}
