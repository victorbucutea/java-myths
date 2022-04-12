import com.sun.jna.platform.unix.X11;
import exchange.core2.core.ExchangeApi;
import exchange.core2.core.IEventsHandler;
import exchange.core2.core.SimpleEventsProcessor;
import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.SymbolType;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.config.ExchangeConfiguration;
import exchange.core2.core.processors.journaling.DummySerializationProcessor;
import exchange.core2.core.processors.journaling.ISerializationProcessor;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class ExchangeServer {

    public static final int CURRENCY_XBT = 11;
    public static final int CURRENCY_LTC = 15;
    public static final int EXCHANGE_SYMBOL = 241;

    public static ExchangeApi createCore() {
        // default exchange configuration
        ExchangeConfiguration conf = ExchangeConfiguration.defaultBuilder().build();

        // no serialization
        Supplier<ISerializationProcessor> serializationProcessorFactory = () -> DummySerializationProcessor.INSTANCE;

        // build exchange core
        exchange.core2.core.ExchangeCore exchangeCore = exchange.core2.core.ExchangeCore.builder()
                .resultsConsumer(new SimpleEventsProcessor(new EventsHandler()))
                .serializationProcessorFactory(serializationProcessorFactory)
                .exchangeConfiguration(conf)
                .build();

        // start up disruptor threads
        exchangeCore.startup();

        // get exchange API for publishing commands
        ExchangeApi api = exchangeCore.getApi();


        // create symbol specification and publish it
        CoreSymbolSpecification symbolSpecXbtLtc = CoreSymbolSpecification.builder()
                .symbolId(EXCHANGE_SYMBOL)         // symbol id
                .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
                .baseCurrency(CURRENCY_XBT)    // base = satoshi (1E-8)
                .quoteCurrency(CURRENCY_LTC)   // quote = litoshi (1E-8)
                .baseScaleK(1_000_000L) // 1 lot = 1M satoshi (0.01 BTC)
                .quoteScaleK(10_000L)   // 1 price step = 10K litoshi
                .takerFee(1900L)        // taker fee 1900 litoshi per 1 lot
                .makerFee(700L)         // maker fee 700 litoshi per 1 lot
                .build();

        api.submitBinaryDataAsync(new BatchAddSymbolsCommand(symbolSpecXbtLtc));

        return api;
    }

    public static class EventsHandler implements IEventsHandler {
        public static AtomicLong noOfTrades = new AtomicLong(0);

        @Override
        public void tradeEvent(IEventsHandler.TradeEvent tradeEvent) {
            long l = noOfTrades.incrementAndGet();
            if (l % 1000 == 0 ) {
                System.out.println("Trade events: " + l);
            }
        }

        @Override
        public void reduceEvent(IEventsHandler.ReduceEvent reduceEvent) {
            System.out.println("Reduce event: " + reduceEvent);
        }

        @Override
        public void rejectEvent(IEventsHandler.RejectEvent rejectEvent) {
//            System.out.println("Reject event: " + rejectEvent);
        }

        @Override
        public void commandResult(IEventsHandler.ApiCommandResult commandResult) {
//                System.out.println("Command result: " + commandResult);
        }

        @Override
        public void orderBook(IEventsHandler.OrderBook orderBook) {
//                System.out.println("OrderBook event: " + orderBook);
        }
    }
}
