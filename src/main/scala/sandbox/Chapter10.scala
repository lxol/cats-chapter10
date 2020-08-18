package sandbox

import cats.kernel.Semigroup
import cats.syntax.all._

object Chapter10 {
  trait Check[E, A] {
    import Check._
    def apply(value: A)(implicit E: Semigroup[E]): Either[E, A] = this match {
      case (Pure(f)) => f(value)
      case (And(c1, c2)) =>
        (c1(value), c2(value)) match {
          case (Right(form), Right(_)) => form.asRight[E]
          case (Left(e), Right(_))     => e.asLeft[A]
          case (Right(_), Left(e))     => e.asLeft[A]
          case (Left(e1), Left(e2))    => (e1 |+| e2).asLeft[A]
        }
    }
  }

  object Check {
    final case class Pure[E, A](a: A => Either[E, A]) extends Check[E, A]
    final case class And[E, A](c1: Check[E, A], c2: Check[E, A])
        extends Check[E, A]
    def pure[E, A](f: A => Either[E, A]) = Pure(f)
  }

}

object Fixtures {
  import Chapter10.Check._
  case class MyForm(name: String, password: String)
  val validForm = MyForm("Alex", "12345")
  val invalidNameForm = MyForm("", "12345")
  val invalidPasswordForm = MyForm("Alex", "")
  val invalidNameAndPasswordForm = MyForm("", "")

  val check1 = pure(
    (form: MyForm) =>
      if (form.name.size > 0) form.asRight[List[String]]
      else List("empty name").asLeft[MyForm]
  )

  val check2 = pure(
    (form: MyForm) =>
      if (form.name.size > 0) form.asRight[List[String]]
      else List("empty name").asLeft[MyForm]
  )

  val both1And2 =  And(check1, check2)
}
