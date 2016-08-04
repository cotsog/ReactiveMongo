import reactivemongo.api.{
  MongoConnection,
  MongoConnectionOptions
}, MongoConnection.{ ParsedURI, parseURI }

import reactivemongo.core.nodeset.Authenticate
import reactivemongo.api.commands.WriteConcern

class MongoURISpec extends org.specs2.mutable.Specification {
  "Mongo URI" title

  section("unit")
  "MongoConnection URI parser" should {
    val simplest = "mongodb://host1"

    s"parse $simplest with success" in {
      parseURI(simplest) must beSuccessfulTry(
        ParsedURI(
          hosts = List("host1" -> 27017),
          db = None,
          authenticate = None,
          options = MongoConnectionOptions(),
          ignoredOptions = List()
        )
      )
    }

    val withPort = "mongodb://host1:27018"
    s"parse $withPort with success" in {
      parseURI(withPort) must beSuccessfulTry(
        ParsedURI(
          hosts = List("host1" -> 27018),
          db = None,
          authenticate = None,
          options = MongoConnectionOptions(),
          ignoredOptions = List()
        )
      )
    }

    val withWrongPort = "mongodb://host1:68903"
    s"parse $withWrongPort with failure" in {
      parseURI(withWrongPort).isFailure must beTrue
    }

    val withWrongPort2 = "mongodb://host1:kqjbce"
    s"parse $withWrongPort2 with failure" in {
      parseURI(withWrongPort2).isFailure must beTrue
    }

    val withDb = "mongodb://host1/somedb"
    s"parse $withDb with success" in {
      parseURI(withDb) must beSuccessfulTry(
        ParsedURI(
          hosts = List("host1" -> 27017),
          db = Some("somedb"),
          authenticate = None,
          options = MongoConnectionOptions(),
          ignoredOptions = List()
        )
      )
    }

    val withAuth = "mongodb://user123:passwd123@host1/somedb"
    s"parse $withAuth with success" in {
      parseURI(withAuth) must beSuccessfulTry(
        ParsedURI(
          hosts = List("host1" -> 27017),
          db = Some("somedb"),
          authenticate = Some(Authenticate("somedb", "user123", "passwd123")),
          options = MongoConnectionOptions(),
          ignoredOptions = List()
        )
      )
    }

    val wrongWithAuth = "mongodb://user123:passwd123@host1"
    s"parse $wrongWithAuth with failure" in {
      parseURI(wrongWithAuth).isFailure must beTrue
    }

    val invalidMonRef1 = "mongodb://host1?rm.monitorRefreshMS=A"

    s"fail to parse $invalidMonRef1" in {
      parseURI(invalidMonRef1) must beSuccessfulTry[ParsedURI].like {
        case uri =>
          uri.ignoredOptions.headOption must beSome("rm.monitorRefreshMS")
      }
    }

    val invalidMonRef2 = "mongodb://host1?rm.monitorRefreshMS=50"

    s"fail to parse $invalidMonRef2 (monitorRefreshMS < 100)" in {
      parseURI(invalidMonRef2) must beSuccessfulTry[ParsedURI].like {
        case uri =>
          uri.ignoredOptions.headOption must beSome("rm.monitorRefreshMS")
      }
    }
  }
  section("unit")
}
