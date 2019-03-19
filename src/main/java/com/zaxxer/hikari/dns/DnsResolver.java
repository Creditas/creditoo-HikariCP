package com.zaxxer.hikari.dns;

public interface DnsResolver {

   /**
    * Resolve a DNS by HostName.
    * @param hostName
    * @return
    */
   String resolve(String hostName);
}
