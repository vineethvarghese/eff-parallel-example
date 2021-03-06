import cats.data.State

import org.atnos.eff.{|=, Eff, Member, Translate}
import org.atnos.eff.Interpret.translate
import org.atnos.eff.future._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._

object FutureInterpreter {
  type ToyState[A] = State[SimState, A]
  type _toyState[R] = ToyState |= R


  def runToy[R, U, A](effects: Eff[R, A])(implicit m: Member.Aux[Toy, R, U], state: _toyState[U], future: _future[U]): Eff[U, A] =
    translate(effects)(new Translate[Toy, U]{
      def apply[T](cmd: Toy[T]): Eff[U, T] = cmd match {
        case Getting(k)    => gets[U, SimState, Option[String]](m => m.context.get(k))
        case Setting(k, v) => modify[U, SimState](m => m.copy(context = m.context + ((k, v))))
        case Doing(v) => {
          futureDelay(Thread.sleep(5000)) >>
          pure[U, Unit](println(v)) >>
          modify[U, SimState](m => m.copy(actions = m.actions :+ v)) >>
          pure("OK")
        }
      }
    })
}
