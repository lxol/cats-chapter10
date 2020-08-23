package sandbox

import cats.kernel.Semigroup
import cats.syntax.all._
import sandbox.`check as ADT`.Check.{And, Pure}
object `check as function` {

  case class CheckF[A, E](f: A => Either[E, A]) {
    def apply(value: A): Either[E, A] = f(value)

    def and(that: CheckF[A, E])(implicit E: Semigroup[E]): CheckF[A, E] =
      CheckF(
        (a: A) =>
          (this(a), that(a)) match {
            case (Right(_), Right(_)) => Right(a)
            case (Left(e), Right(_))  => e.asLeft[A]
            case (Right(_), Left(e))  => e.asLeft[A]
            case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft[A]
        }
      )
  }

}

object `check as ADT` {
  trait Check[A, E] {
    def apply(value: A)(implicit E: Semigroup[E]): Either[E, A] =
      this match {
        case Pure(f) => f(value)
        case And(c1, c2) =>
          (c1(value), c2(value)) match {
            case (Right(_), Right(_)) => Right(value)
            case (Left(e), Right(_))  => e.asLeft[A]
            case (Right(_), Left(e))  => e.asLeft[A]
            case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft[A]
          }
      }

  }

  object Check {
    final case class Pure[A, E](f: A => Either[E, A]) extends Check[A, E]
    final case class And[A, E](c1: Check[A, E], c2: Check[A, E])
        extends Check[A, E]
    final case class Or[A, E](c1: Check[A, E], c2: Check[A, E])
        extends Check[A, E]
  }

}
