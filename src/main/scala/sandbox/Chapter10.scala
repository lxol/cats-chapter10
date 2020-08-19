package sandbox

import cats.data.Validated
import cats.kernel.Semigroup
import cats.syntax.all._

object Chapter10 {
  trait Check[E, A] {
    import Check._
    def apply(value: A)(implicit E: Semigroup[E]): Validated[E, A] = this match {
      case (Pure(f)) => f(value)
      case (And(c1, c2)) => (c1(value), c2(value)).mapN((c,_) => c)
//        (c1(value), c2(value)) match {
//          case (Valid(form), Valid(_)) => form.valid[E]
//          case (Invalid(e), Valid(_))     => e.invalid[A]
//          case (Valid(_), Invalid(e))     => e.invalid[A]
//          case (Invalid(e1), Invalid(e2))    => (e1 |+| e2).invalid[A]
//        }
    }
  }

  object Check {
    final case class Pure[E, A](a: A => Validated[E, A]) extends Check[E, A]
    final case class And[E, A](c1: Check[E, A], c2: Check[E, A])
        extends Check[E, A]
    def pure[E, A](f: A => Validated[E, A]) = Pure(f)
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
      if (form.name.size > 0) form.valid[List[String]]
      else List("empty name").invalid[MyForm]
  )

  val check2 = pure(
    (form: MyForm) =>
      if (form.password.size > 0) form.valid[List[String]]
      else List("empty name").invalid[MyForm]
  )

  val both1And2 = And(check1, check2)
}
