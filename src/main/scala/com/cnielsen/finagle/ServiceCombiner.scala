package com.cnielsen.finagle

import com.krowd9.api.addressbookdb.{AddressBookDbService, GetContactsFilter, OffsetAndContact}
import com.krowd9.api.usermanager.{GetYakatakUserIdResult, UserManagerService}
import com.twitter.finagle.Thrift
import com.twitter.util.Future

class ServiceCombiner(addressHost: String, contactHost: String) {

  def fetchAdresses(userId: Long, filter: Option[String]): Future[Seq[OffsetAndContact]] = {
    Thrift.client.build[AddressBookDbService.MethodPerEndpoint](addressHost)
      .getContacts(userId, GetContactsFilter(filter))
  }

  private def fetchIds(externalId: String): Future[GetYakatakUserIdResult] = {
    Thrift.client.build[UserManagerService.MethodPerEndpoint](contactHost)
      .getYakatakUserId(externalId)
  }

  def fetchCombined(userId: Long, filter: Option[String]): Future[Seq[Future[(Option[Long], Option[String])]]] = {
    fetchAdresses(userId, filter)
      .map(_.map(a => fetchIds(a._2._1).map(b => (b._1, a._2._2))))
  }

}

