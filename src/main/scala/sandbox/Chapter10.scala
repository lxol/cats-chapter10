//package sandbox

//import cats.data.Validated
//import cats.kernel.Semigroup
//import cats.syntax.all._
//import sandbox.Chapter10.Predicate.Pure
//
//object Chapter10 {
//  trait Predicate[E, A, B] { self =>
//
//    import Predicate._
//
//    def apply(value: A)(implicit E: Semigroup[E]): Validated[E, B] =
//      this match {
//        case (Pure(f)) => f(value)
//
//        case (And(c1, c2)) => (c1(value), c2(value)).mapN((c, _) => c)
//
//        case (Or(c1, c2)) => c1(value).findValid(c2(value))
//      }
//    def and(that: Predicate[E,A, B]): Predicate[E,A,B] = And(this, that )
//    def or(that: Predicate[E,A,B]): Predicate[E,A,B] = Or(this, that )
//
////    def map[C](f: B => C): Predicate[E, A, C] = new Predicate[E,A, C] {
////        override def apply(value: A)(implicit E: Semigroup[E]): Validated[E, C] = self(value).map(f(_))
//
//    //}
//  }
//
//  object Predicate {
//
//      final case class Pure[E, A, B](a: A => Validated[E, B]) extends Predicate[E, A, B]
//
//      final case class And[E, A, B](c1: Predicate[E, A, B], c2: Predicate[E, A, B])
//          extends Predicate[E, A, B]
//
//      final case class Or[E, A, B](c1: Predicate[E, A, B], c2: Predicate[E, A, B])
//          extends Predicate[E, A, B]
//
//      def pure[E, A, B](f: A => Validated[E, B]) = Pure(f)
//  }
//
//}
//
//object Fixtures {
//  import sandbox.Chapter10.Predicate._
//  case class MyForm(name: String, password: String)
//  val validForm = MyForm("Alex", "12345")
//  val invalidNameForm = MyForm("", "12345")
//  val invalidPasswordForm = MyForm("Alex", "")
//  val invalidNameAndPasswordForm = MyForm("", "")
//
//  val check1 = pure(
//    (form: MyForm) =>
//      if (form.name.size > 0) form.valid[List[String]]
//      else List("empty name").invalid[MyForm]
//  )
//
//  val check2 = pure(
//    (form: MyForm) =>
//      if (form.password.size > 0) form.valid[List[String]]
//      else List("empty name").invalid[MyForm]
//  )
//
//  val both1And2 = And(check1, check2)
//}

//object `check as function` {
//
//    import cats.syntax.all._
//    case class CheckF[E, A](f:A => Either[E,A]) {
//        def apply(value: A): Either[E,A]  = f(value)
//        def and(that: CheckF[E,A])(implicit E:Semigroup[E]): CheckF[E,A]  = CheckF[E,A](
//            (a:A) => (this(a), that(a)) match {
//                case (Right(v1), Right(_)) => v1.asRight[E]
//                case (Left(e), Right(_)) => e.asLeft[A]
//                case (Right(_), Left(e)) => e.asLeft[A]
//                case (Left(e1), Left(e2)) => (e2 |+| e1).asLeft[A]
//            }
//        )
//    }
//}
//
//
//object `check as ADT` {
//
//    import cats.syntax.all._
//    case class CheckF[E, A](f:A => Either[E,A]) {
//        def apply(value: A): Either[E,A]  = f(value)
//        def and(that: CheckF[E,A])(implicit E:Semigroup[E]): CheckF[E,A]  = CheckF[E,A](
//            (a:A) => (this(a), that(a)) match {
//                case (Right(v1), Right(_)) => v1.asRight[E]
//                case (Left(e), Right(_)) => e.asLeft[A]
//                case (Right(_), Left(e)) => e.asLeft[A]
//                case (Left(e1), Left(e2)) => (e2 |+| e1).asLeft[A]
//            }
//        )
//    }
//}
//
//
//
//
//object MyFixtures {
//    case class MyForm(name: String, password: String)
//
//String}
