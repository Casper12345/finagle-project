namespace * com.cnielsen.api.addressbookservice
include "common.thrift"

struct Contact {
   1: optional i64 yakatakId;
   2: optional string name;
}

struct ContactsAndClientInfo {
    1: list<Contact> contacts;
    /**
     * Option, that if empty, indicates that all contacts were fetched from server,
     * else if defined string offset can be input to fetch remaining contacts.
     **/
    2: optional string contactsLeft;
}

service AddressBookService {

  ContactsAndClientInfo getAllContacts(1: i64 id, 2: optional string offset)
    throws(1: common.UserNotFoundException ex)
  ContactsAndClientInfo getContactsWithId(1: i64 id, 2: optional string offset)
    throws(1: common.UserNotFoundException ex)
  ContactsAndClientInfo getContactsWithoutId(1: i64 id, 2: optional string offset)
    throws(1: common.UserNotFoundException ex)
}
