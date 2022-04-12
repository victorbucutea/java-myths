package com.socgen.exchange.plain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlainExchangeServer {

    public static AtomicInteger userId = new AtomicInteger(0);
    public static List<Integer> users = new ArrayList<>();
    public static Map<Integer, Long> balance = new HashMap<>(10_000_000);
    public static ArrayList<Offer> offers = new ArrayList<>(10_000_000);
    public static ArrayList<Bid> bids = new ArrayList<>(10_000_000);


    public static synchronized int registerUser(){
        users.add(userId.incrementAndGet());
        return userId.get();
    }

    public static synchronized void deposit(int userId, long amount, int currencyXbt){
        if ( balance.containsKey(userId))
            balance.put(userId, balance.get(userId) + amount);
        else {
            balance.put(userId, amount);
        }
    }


    public static synchronized void placeOffer(int currency, long qty, long price){
        Offer off = new Offer();
        off.currency = currency;
        off.qty = qty;
        off.price = price;
        offers.add(off);
    }

    public static synchronized void placeBid(int currency, long qty, long price, long reservePrice){
        Bid off = new Bid();
        off.currency = currency;
        off.qty = qty;
        off.price = price;
        off.reservePrice = reservePrice;
        bids.add(off);
    }

    public static synchronized void requestOrderBook() {

    }

    public static void printAllBalances() {
        System.out.println("Balances and orders not calculated!");
        System.out.println("Just measuring contention ... ");
        System.out.println("Orders placed " + offers.size()+ ". ");
        System.out.println("Bids placed " + bids.size()+ ". ");
    }
}


