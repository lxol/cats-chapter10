import $ivy.`org.scalacheck::scalacheck:1.14.0`
import $ivy.`org.typelevel::cats-core:2.0.0`
import $ivy.`org.typelevel::simulacrum:1.0.0`
import Predicate.{And, Or, Pure}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.kernel.Semigroup
//import cats.syntax.all._
//import cats.syntax.all
import cats.implicits._
//import cats.instances.either._

trait Predicate[E, A] {
  def apply(a: A)(implicit E: Semigroup[E]): Validated[E, A] = this match {
    case Pure(f)     => f(a)
    case And(c1, c2) => (c1(a), c2(a)).mapN((_, _) => a)
    case Or(c1, c2) =>
      c1(a) match {
        case Valid(_) => a.valid[E]
        case Invalid(e1) =>
          c2(a) match {
            case Valid(_)    => a.valid[E]
            case Invalid(e2) => (e1 |+| e2).invalid[A]
          }
      }
  }
  def and(that: Predicate[E, A]): Predicate[E, A] = And(this, that)
  def or(that: Predicate[E, A]): Predicate[E, A] = Or(this, that)
  def run()(implicit  E: Semigroup[E]): A => Validated[E,A] = (a:A)  => apply(a)

}

object Predicate {
  case class Pure[E, A](f: A => Validated[E, A]) extends Predicate[E, A]
  case class And[E, A](c1: Predicate[E, A], c2: Predicate[E, A])
      extends Predicate[E, A]
  case class Or[E, A](c1: Predicate[E, A], c2: Predicate[E, A])
      extends Predicate[E, A]
  def pure[E, A](f: A => Validated[E, A]) = Pure(f)
}

trait Check[E, A, B] {
  def apply(value: A)(implicit E: Semigroup[E]): Validated[E, B]

  def map[C](f: B => C): Check[E, A, C] = Check.Map(this, f)

  def flatMap[C](f: B => Check[E, A, C]): Check[E, A, C] =
    Check.FlatMap(this, f)

  def andThen[C](that: Check[E, B, C]): Check[E, A, C] =
    Check.AndThen(this, that)
}

object Check {
  case class Pure[E, A](p: Predicate[E, A]) extends Check[E, A, A] {
    override def apply(value: A)(implicit E: Semigroup[E]): Validated[E, A] =
      p(value)

  }
  case class Map[E, A, B, C](check: Check[E, A, B], f: B => C)
      extends Check[E, A, C] {
    override def apply(value: A)(implicit E: Semigroup[E]): Validated[E, C] =
      check(value).map(f)
  }

  case class FlatMap[E, A, B, C](check: Check[E, A, B], f: B => Check[E, A, C])
      extends Check[E, A, C] {
    override def apply(value: A)(implicit E: Semigroup[E]): Validated[E, C] =
      check(value).withEither(_.flatMap(b => f(b)(value).toEither))
  }

  case class AndThen[E, A, B, C](check1: Check[E, A, B], check2: Check[E, B, C])
      extends Check[E, A, C] {
    override def apply(value: A)(implicit E: Semigroup[E]): Validated[E, C] =
      check1(value).withEither(_.flatMap(b => check2(b).toEither))
  }

}

object sandbox {
  import cats.syntax.all._

  val predicateA = Predicate.pure(
    (s: String) =>
      if (s.contains('a')) s.valid[List[String]]
      else List("does not contains a").invalid[String]
  )
  val predicateLength = Predicate.pure(
    (s: String) =>
      if (s.length > 2) s.valid[List[String]]
      else List("too short").invalid[String]
  )
  val predicateSymbol = Predicate.pure(
    (s: String) =>
      "\\W".r
        .findFirstIn(s)
        .map(_.valid[List[String]])
        .getOrElse(List("does not contain special symbols").invalid[String])
  )
}
import sandbox._
