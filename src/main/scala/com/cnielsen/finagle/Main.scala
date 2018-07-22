package com.cnielsen.finagle

object Main extends App {

  new AddressBookServer(
    "localhost:9000",
    "localhost:7201",
    "localhost:7202"
  ).serve()

}
