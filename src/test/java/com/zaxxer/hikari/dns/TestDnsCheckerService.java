package com.zaxxer.hikari.dns;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDnsCheckerService {

   public class MockDnsResolver implements DnsResolver {

      private boolean flag;

      @Override
      public String resolve(String hostName) {

         flag = !flag;

         if(flag) {
            return "10.10.10.1";
         } else {
           return "10.10.10.2";
         }
      }
   }

   @Test
   public void testRunningNormally()
   {
      try (DnsCheckerService dsc = new DnsCheckerService("google.com", 10, new MockDnsResolver()))
      {
         dsc.start();

         assertTrue(dsc.isRunning());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   public void testDnsChanged() {
      AtomicBoolean hasChanged = new AtomicBoolean(false);
      AtomicReference<String> newHostAddressValue = new AtomicReference<>();
      AtomicReference<String> oldHostAddressValue = new AtomicReference<>();

      try (DnsCheckerService dsc = new DnsCheckerService("google.com", 2, new MockDnsResolver()))
      {
         dsc.addListener((newHostAddress, oldHostAddress) -> {
            if(!hasChanged.get()) {
               newHostAddressValue.set(newHostAddress);
               oldHostAddressValue.set(oldHostAddress);
               hasChanged.set(true);
            }
         });

         dsc.start();

         Thread.sleep(4500);

         assertEquals("10.10.10.1", oldHostAddressValue.get());
         assertEquals("10.10.10.2", newHostAddressValue.get());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
