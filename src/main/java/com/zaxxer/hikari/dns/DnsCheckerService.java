package com.zaxxer.hikari.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DnsCheckerService implements Closeable, AutoCloseable {

   private final Logger logger = LoggerFactory.getLogger(DnsCheckerService.class);
   private final String hostName;
   private final int delay;
   private final ScheduledExecutorService checkDnsExecutorService;
   private final List<DnsChangedListener> listeners = new ArrayList<>();
   private final DnsResolver dnsResolver;

   private String currentHostAddress;
   private boolean isRunning;

   public DnsCheckerService(String hostName, int delay, DnsResolver dnsResolver) {

      if(hostName == null)
         throw new IllegalArgumentException("HostName cannot be null");

      if(delay < 1)
         throw new IllegalArgumentException("Delay cannot be less than 1.");

      if(dnsResolver == null)
         throw new IllegalArgumentException("DnsResolver cannot be null");

      this.hostName = hostName;
      this.delay = delay;
      this.dnsResolver = dnsResolver;
      this.checkDnsExecutorService = Executors.newScheduledThreadPool(1);
   }

   /**
    * Start a scheduled task that checks DNS Changes and Evict all connections.
    */
   public void start() {

      try {

         java.security.Security.setProperty("networkaddress.cache.ttl", String.valueOf(delay));

         this.checkDnsExecutorService.scheduleWithFixedDelay(() -> checkChanges(hostName), delay, delay, SECONDS);

         this.isRunning = true;

      } catch (Exception e){
         logger.error("Error on try start DNS Checker", e);
      }
   }

   /**
    * Check if Service is running.
    * @return
    */
   public boolean isRunning() { return this.isRunning; }

   /**
    * Add a Dns Change Listeners.
    * @param listener
    */
   public void addListener(DnsChangedListener listener) {
      if(listener == null)
         throw new IllegalArgumentException("Listener cannot be null.");

      this.listeners.add(listener);
   }

   @Override
   public void close() {
      this.checkDnsExecutorService.shutdown();
      this.isRunning = false;
   }

   /**
    * Check if DNS has been changed evicting all connections.
    * @param hostName
    */
   private void checkChanges(String hostName){
      try {
         String newHostAddress = this.dnsResolver.resolve(hostName);

         if(newHostAddress == null) {
            logger.warn("DNS Checker - Inet Address not found.");
            return;
         }

         if(currentHostAddress == null) {
            logger.info("DNS Checker - Current Address: {}", newHostAddress);
            currentHostAddress = newHostAddress;
         }

         if(!currentHostAddress.equals(newHostAddress)) {
            logger.info("DNS Checker - Address Changed from {} to {} ", currentHostAddress, newHostAddress);

            notifyListeners(newHostAddress, currentHostAddress);

            currentHostAddress = newHostAddress;
         }
      } catch (Exception e) {
         logger.error("Error on try check DNS.", e);
      }
   }

   /**
    * Notify all listeners about DNS Change.
    * @param newHostAddress
    * @param oldHostAddress
    */
   private void notifyListeners(String newHostAddress, String oldHostAddress) {

      for (DnsChangedListener listener : this.listeners) {
         listener.changed(newHostAddress, oldHostAddress);
      }
   }
}
