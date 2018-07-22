package com.cnielsen.finagle

import com.twitter.util.{Await, Future}
import org.scalatest.{FreeSpec, Matchers}
import org.scalamock.scalatest.MockFactory
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

// could not make async spec work with com.twitter.util.Future

class AddressBookServerSpec extends FreeSpec with Matchers with MockFactory {

  def fixture(futures: Seq[Future[(Option[Long], Option[String])]]) = new {

    val serverFetcher = stub[ServerFetcher]
    val addressBookServer = new AddressBookServiceImp(serverFetcher)
    (serverFetcher.fetchCombined _).when(*, *).returns(
      Future(
        futures
      ))
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

    "should end loop when " in {

      val serverFetcher = stub[ServerFetcher]

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

    "should return max 100 entries" in {


    }

  }


}