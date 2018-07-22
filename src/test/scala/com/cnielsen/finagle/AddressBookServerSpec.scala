package com.cnielsen.finagle

import com.cnielsen.api.addressbookservice.{Contact, ContactsAndClientInfo}
import com.twitter.util.{Await, Future}
import org.scalatest.{FreeSpec, Matchers}
import org.scalamock.scalatest.MockFactory
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

// could not make async spec work with com.twitter.util.Future
class AddressBookServerSpec extends FreeSpec with Matchers with MockFactory {

  def fixture(futures: Seq[Future[(Option[Long], Option[String])]] = Seq()) = new {

    val serverFetcher = stub[ServiceCombiner]
    val addressBookServer = new AddressBookServiceImp(serverFetcher)
    (serverFetcher.fetchCombined _).when(*, *).returns(
      Future(
        futures
      ))
  }

  def defaultFutures = {
    Seq(
      Future((None, Some("Harry"))),
      Future((None, Some("Peter"))),
      Future((Some(12L), Some("Otto"))),
      Future((Some(13L), Some("John"))),
      Future((Some(14L), Some("James"))),
      Future((Some(15L), Some("Sam")))
    )
  }

  "fetch all" - {

    "should call service method correctly" in {

      val f = fixture(Seq())

      val fetched = f.addressBookServer.fetchAll(12L, None)

      (f.serverFetcher.fetchCombined _).verify(12L, None)

    }


    "should removes duplicates from result list" in {

      val futures = Seq(
        Future((Some(12L), Some("Harry"))),
        Future((Some(13L), Some("John"))),
        Future((Some(13L), Some("John"))),
        Future((Some(14L), Some("James"))),
        Future((Some(13L), Some("John"))),
        Future((Some(15L), Some("Sam")))
      )

      val f = fixture(futures)

      val fetched = f.addressBookServer.fetchAll(12L, None)

      val result = Await.result(fetched)

      result._1.size shouldEqual 4

    }

    "should end loop when last line matches offset" in {

      val serverFetcher = stub[ServiceCombiner]

      val addressBookServer = new AddressBookServiceImp(serverFetcher)

      val gen = for (_ <- 0 until 9) yield
        Future((Some(Random.nextInt(40).toLong), Some(Random.nextString(50))))


      (serverFetcher.fetchCombined _).when(*, *).returns(
        Future(gen :+ Future((Some(12L), Some("LASTLINE")))
        )).anyNumberOfTimes()

      val fetched = addressBookServer.fetchAll(12L, None)

      val result = Await.result(fetched)

      result._1.size shouldEqual 10

    }
  }

  "map to return type" - {

    "should map input correctly to future of ContactsAndClientInfo" in {

      val futures = Seq(
        (Some(12L), Some("Harry")),
        (Some(13L), Some("John")),
        (Some(14L), Some("James")),
        (Some(15L), Some("Sam"))
      )

      val f = fixture()

      val fetched: Future[ContactsAndClientInfo] =
        f.addressBookServer.mapToReturnType(Future((futures, Option("hello"))))

      val result = Await.result(fetched)

      result shouldEqual ContactsAndClientInfo(
        Seq(
          Contact(Some(12L), Some("Harry")),
          Contact(Some(13L), Some("John")),
          Contact(Some(14L), Some("James")),
          Contact(Some(15L), Some("Sam"))
        ), Option("hello")
      )
    }

  }

  "get all contacts" - {

    "should return all contacts" in {

      val f = fixture(defaultFutures)

      val fetched = f.addressBookServer.getAllContacts(12L, None)

      val result = Await.result(fetched)

      result._1.size shouldEqual 6

    }

  }

  "get all contacts with id" - {

    "should only return contacts with ids" in {

      val f = fixture(defaultFutures)

      val fetched = f.addressBookServer.getContactsWithId(12L, None)

      val result = Await.result(fetched)

      result._1.size shouldEqual 4

    }
  }

  "get all contacts without id" - {

    "should only return contacts without ids" in {

      val f = fixture(defaultFutures)

      val fetched = f.addressBookServer.getContactsWithoutId(12L, None)

      val result = Await.result(fetched)

      result._1.size shouldEqual 2

    }
  }

}
