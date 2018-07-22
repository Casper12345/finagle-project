package com.cnielsen.finagle

import com.cnielsen.api.addressbookservice.{AddressBookService, Contact, ContactsAndClientInfo}
import com.twitter.finagle.{ListeningServer, Thrift}
import com.twitter.util._
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class AddressBookServer(servingHost: String, extHost1: String, extHost2: String) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def serve(): Unit = {
    logger.info(s"server listening at: $servingHost")

    val server: ListeningServer = Thrift.server.serveIface(
      servingHost, new AddressBookServiceImp(
        new ServiceCombiner(extHost1, extHost2))
    )

    Await.ready(server)

  }

}

class AddressBookServiceImp(
                             serviceCombiner: ServiceCombiner
                           ) extends AddressBookService.MethodPerEndpoint {

  def fetchRecords(id: Long, offset: Option[String]): Future[Seq[(Option[Long], Option[String])]] = {
    serviceCombiner.fetchCombined(id, offset).flatMap(a => Future.collect(a))
  }

  type FutureAlias = Future[(Seq[(Option[Long], Option[String])], Option[String])]

  def fetchAll(id: Long, offset: Option[String])(implicit ec: ExecutionContext): FutureAlias = {
    def go(offset: Option[String], acc: Seq[(Option[Long], Option[String])]): FutureAlias = {
      fetchRecords(id, offset).flatMap { seq =>
        if (seq.last._2 == offset) {
          Future(((acc ++ seq).distinct, None))
        } else {
          if (acc.size > 99) {
            Future((acc, acc.last._2))
          } else {
            go(seq.last._2, (acc ++ seq).distinct)
          }
        }
      }
    }

    go(offset, Seq())
  }

  def mapToReturnType(xs: FutureAlias): Future[ContactsAndClientInfo] = {
    xs.map(a => ContactsAndClientInfo(a._1.map(b => Contact(b._1, b._2)), a._2))
  }

  override def getAllContacts(id: Long, offset: Option[String]): Future[ContactsAndClientInfo] = {
    fetchAll(id, offset).map(a => ContactsAndClientInfo(a._1.map(b => Contact(b._1, b._2)), a._2))
  }

  override def getContactsWithId(id: Long, offset: Option[String]): Future[ContactsAndClientInfo] = {
    fetchAll(id, offset)
      .map(a => ContactsAndClientInfo(a._1.filter(c => c._1.isDefined)
        .map(b => Contact(b._1, b._2)), a._2))
  }

  override def getContactsWithoutId(id: Long, offset: Option[String]): Future[ContactsAndClientInfo] = {
    fetchAll(id, offset)
      .map(a => ContactsAndClientInfo(a._1.filter(c => c._1.isEmpty)
        .map(b => Contact(b._1, b._2)), a._2))
  }

}
