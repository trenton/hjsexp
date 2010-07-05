package com.amazon.ebs;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
public class DnsGuy {
    private static final String JAVA_ADDRESS_CACHE_FIELD_NAME = "addressCache";
    private static final String JAVA_NEG_CACHE_FIELD_NAME = "negativeCache";

    List<String> domains = new ArrayList<String>();
    private ScheduledExecutorService pool;

    public void addDomain(String domain) {
        domains.add(domain);
    }

    private void init() {
        pool = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
        pool.scheduleWithFixedDelay(new Dumper(), 0, 3, TimeUnit.SECONDS);

        for (String domain : domains) {
            try {
                InetAddress.getByName(domain);
            } catch (UnknownHostException e) {
                System.err.println("Couldn't do it: " + e.getMessage());
            }
        }
    }

    private void shutdown() {
        pool.shutdownNow();
    }

    private void dumpEntries() throws Exception {
        System.out.println(JAVA_ADDRESS_CACHE_FIELD_NAME);
        printDNSCache(JAVA_ADDRESS_CACHE_FIELD_NAME);

        System.out.printf("\n%s\n", JAVA_NEG_CACHE_FIELD_NAME);
        printDNSCache(JAVA_NEG_CACHE_FIELD_NAME);
    }

    private void printDNSCache(String cacheName) throws Exception {
        Class<InetAddress> klass = InetAddress.class;
        Field acf = klass.getDeclaredField(cacheName);
        acf.setAccessible(true);
        Object addressCache = acf.get(null);
        Class cacheKlass = addressCache.getClass();
        Field cf = cacheKlass.getDeclaredField("cache");
        cf.setAccessible(true);
        Map<String, Object> cache = (Map<String, Object>) cf.get(addressCache);

        for (Map.Entry<String, Object> hi : cache.entrySet()) {
            Object cacheEntry = hi.getValue();
            Class cacheEntryKlass = cacheEntry.getClass();
            Field expf = cacheEntryKlass.getDeclaredField("expiration");
            expf.setAccessible(true);
            long expires = (Long) expf.get(cacheEntry);

            Field addressField = cacheEntryKlass.getDeclaredField("address");
            addressField.setAccessible(true);
            InetAddress[] addresses = (InetAddress[]) addressField.get(cacheEntry);

            List<String> ads = new ArrayList<String>(addresses.length);
            for (InetAddress address : addresses) {
                ads.add(address.getHostAddress());
            }

            System.out.println(hi.getKey() + " " + new Date(expires) + " " + ads);
        }
    }

    final class Dumper implements Runnable {
        @Override
        public void run() {
            try {
                dumpEntries();
            } catch (Exception e) {
                System.err.println("Couldn't dump: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final List<String> hosts = Arrays.asList(args);
        if(hosts.isEmpty()) {
            System.err.println("Must pass at least one host to lookup.");
            System.exit(1);
        }

        DnsGuy dnsguy = new DnsGuy();
        for (String host : args) {
            dnsguy.addDomain(host);
        }

        System.out.printf("Looking up %s. Hit ^C to exit", hosts);

        dnsguy.init();
        dnsguy.dumpEntries();

        Thread.sleep(Long.MAX_VALUE);

        dnsguy.shutdown();
    }
}
