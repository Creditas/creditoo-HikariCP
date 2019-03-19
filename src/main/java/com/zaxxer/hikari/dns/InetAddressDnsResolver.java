package com.zaxxer.hikari.dns;

import java.net.InetAddress;

public class InetAddressDnsResolver implements DnsResolver {
   @Override
   public String resolve(String hostName) {
      try {
         InetAddress address = InetAddress.getByName(hostName);

         return address.getHostAddress();
      } catch (Exception e) {
         return "UNKNOWN";
      }
   }
}
