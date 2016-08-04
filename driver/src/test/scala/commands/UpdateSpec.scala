import scala.concurrent.duration.FiniteDuration

import reactivemongo.api.commands.{ UpdateWriteResult, WriteResult, Upserted }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.bson.BSONUpdateCommand._
import reactivemongo.api.commands.bson.BSONUpdateCommandImplicits._
import reactivemongo.bson._

import org.specs2.concurrent.{ ExecutionEnv => EE }

class UpdateSpec extends org.specs2.mutable.Specification {
  "Update" title

  sequential

  import Common._

  lazy val col1 = db(s"update1${System identityHashCode db}")
  lazy val col2 = db(s"update2${System identityHashCode slowDb}")

  case class Person(firstName: String, lastName: String, age: Int)

  implicit object PersonReader extends BSONDocumentReader[Person] {
    def read(doc: BSONDocument): Person =
      Person(
        doc.getAs[String]("firstName").getOrElse(""),
        doc.getAs[String]("lastName").getOrElse(""),
        doc.getAs[Int]("age").getOrElse(0)
      )
  }

  implicit object PersonWriter extends BSONDocumentWriter[Person] {
    def write(person: Person): BSONDocument =
      BSONDocument(
        "firstName" -> person.firstName,
        "lastName" -> person.lastName,
        "age" -> person.age
      )
  }

  "Update" should {
    {
      def spec(c: BSONCollection, timeout: FiniteDuration)(implicit ee: EE) = {
        val jack = Person("Jack", "London", 27)

        c.update(jack, BSONDocument("$set" -> BSONDocument("age" -> 33)),
          upsert = true).map(_ => {}) must beEqualTo({}).await(1, timeout)

      }

      "upsert a person with the default connection" in { implicit ee: EE =>
        spec(col1, timeout)
      }
    }

    {
      def spec(c: BSONCollection, timeout: FiniteDuration)(implicit ee: EE) = {
        val doc = BSONDocument("_id" -> "foo", "bar" -> 2)

        c.update(BSONDocument.empty, doc, upsert = true).
          map(_ => {}) must beEqualTo({}).await(1, timeout)
      }

      "upsert a document with the default connection" in { implicit ee: EE =>
        spec(col2, timeout)
      }
    }

    {
      def spec(c: BSONCollection, timeout: FiniteDuration)(implicit ee: EE) = {
        val jack = Person("Jack", "London", 33)

        c.runCommand(Update(UpdateElement(
          q = jack, u = BSONDocument("$set" -> BSONDocument("age" -> 66))
        ))).
          aka("result") must beLike[UpdateWriteResult]({
            case result => result.nModified mustEqual 1 and (
              c.find(BSONDocument("age" -> 66)).
              one[Person] must beSome(jack.copy(age = 66)).await(1, timeout)
            )
          }).await(1, timeout)
      }

      "update a person with the default connection" in { implicit ee: EE =>
        spec(col1, timeout)
      }
    }

    "update a document" in { implicit ee: EE =>
      val doc = BSONDocument("_id" -> "foo", "bar" -> 2)

      col2.runCommand(Update(UpdateElement(
        q = doc, u = BSONDocument("$set" -> BSONDocument("bar" -> 3))
      ))).
        aka("result") must beLike[UpdateWriteResult]({
          case result => result.nModified must_== 1 and (
            col2.find(BSONDocument("_id" -> "foo")).one[BSONDocument].
            aka("updated") must beSome(BSONDocument(
              "_id" -> "foo", "bar" -> 3
            )).await(1, timeout)
          )
        }).await(1, timeout)
    }
  }
}
