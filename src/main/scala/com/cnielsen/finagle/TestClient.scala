package com.cnielsen.finagle

import com.cnielsen.api.addressbookservice.{AddressBookService, ContactsAndClientInfo}
import com.twitter.finagle.Thrift
import com.twitter.util.{Await, Future}
import org.slf4j.{Logger, LoggerFactory}


object Client2 extends App {

  val methodPerEndpoint: AddressBookService.MethodPerEndpoint =
    Thrift.client.build[AddressBookService.MethodPerEndpoint]("localhost:9000")

  val result: Future[ContactsAndClientInfo] = methodPerEndpoint.getAllContacts(1, None)

  val mal = Await.result(result)
  mal._1.foreach(a => println(a))
  println(mal._2)
  println(mal._1.size)


  val result2: Future[ContactsAndClientInfo] = methodPerEndpoint.getContactsWithId(1, None)

  val mal2 = Await.result(result)
  mal2._1.foreach(a => println(a))
  println(mal2._2)
  println(mal2._1.size)

  val result3: Future[ContactsAndClientInfo] = methodPerEndpoint.getContactsWithoutId(1, None)

  val mal3 = Await.result(result)
  mal2._1.foreach(a => println(a))
  println(mal3._2)
  println(mal3._1.size)
}
