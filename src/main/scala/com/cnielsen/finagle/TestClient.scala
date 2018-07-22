package com.cnielsen.finagle

import com.cnielsen.api.addressbookservice.{AddressBookService, ContactsAndClientInfo}
import com.twitter.finagle.Thrift
import com.twitter.util.{Await, Future}

object TestClient extends App {

  val methodPerEndpoint: AddressBookService.MethodPerEndpoint =
    Thrift.client.build[AddressBookService.MethodPerEndpoint]("localhost:9000")

  val result: Future[ContactsAndClientInfo] = methodPerEndpoint.getAllContacts(1, None)

  val resolved = Await.result(result)
  resolved._1.foreach(a => println(a))
  println(resolved._2)
  println(resolved._1.size)


  val result2: Future[ContactsAndClientInfo] = methodPerEndpoint.getContactsWithId(1, None)

  val resolved2 = Await.result(result)
  resolved2._1.foreach(a => println(a))
  println(resolved2._2)
  println(resolved2._1.size)

  val result3: Future[ContactsAndClientInfo] = methodPerEndpoint.getContactsWithoutId(1, None)

  val resolved3 = Await.result(result)
  resolved2._1.foreach(a => println(a))
  println(resolved3._2)
  println(resolved3._1.size)
}
