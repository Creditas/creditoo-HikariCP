package com.zaxxer.hikari.dns;

public interface DnsChangedListener {

   /**
    * Callback for DNS Changed.
    * @param newHostAddress
    * @param oldHostAddress
    */
   void changed(String newHostAddress, String oldHostAddress);
}
