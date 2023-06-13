package io.scalaland.chimney.internal.compiletime

import scala.collection.compat.Factory
import scala.collection.immutable.ListSet

private[compiletime] trait Types { this: Existentials =>

  /** Platform-specific type representation (c.universe.Type in 2, scala.quoted.Type[A] in 3) */
  protected type Type[A]
  protected val Type: TypeModule
  protected trait TypeModule { this: Type.type =>
    final def apply[A](implicit A: Type[A]): Type[A] = A

    val Nothing: Type[Nothing]
    val Any: Type[Any]
    val AnyVal: Type[AnyVal]
    val Boolean: Type[Boolean]
    val Byte: Type[Byte]
    val Char: Type[Char]
    val Short: Type[Short]
    val Int: Type[Int]
    val Long: Type[Long]
    val Float: Type[Float]
    val Double: Type[Double]
    val Unit: Type[Unit]

    lazy val primitives: Set[ExistentialType] = ListSet(
      Boolean.asExistential,
      Byte.asExistential,
      Char.asExistential,
      Short.asExistential,
      Int.asExistential,
      Long.asExistential,
      Float.asExistential,
      Double.asExistential,
      Unit.asExistential
    )

    trait Constructor1[F[_]] {
      def apply[A: Type]: Type[F[A]]
      def unapply[A](tpe: Type[A]): Option[ExistentialType]
    }

    trait Constructor2[F[_, _]] {
      def apply[A: Type, B: Type]: Type[F[A, B]]
      def unapply[A](tpe: Type[A]): Option[(ExistentialType, ExistentialType)]
    }

    def Tuple2[A: Type, B: Type]: Type[(A, B)]

    def Function1[A: Type, B: Type]: Type[A => B]
    def Function2[A: Type, B: Type, C: Type]: Type[(A, B) => C]

    val Array: ArrayModule
    trait ArrayModule extends Constructor1[Array] { this: Array.type =>
      val Any: Type[Array[Any]] = Array(Type.Any)
    }

    val Option: OptionModule
    trait OptionModule extends Constructor1[Option] { this: Option.type =>
      val None: Type[scala.None.type]
    }

    val Either: EitherModule
    trait EitherModule extends Constructor2[Either] { this: Either.type =>
      val Left: LeftModule
      trait LeftModule extends Constructor2[Left] { this: Left.type => }

      val Right: RightModule
      trait RightModule extends Constructor2[Right] { this: Right.type => }
    }

    val Iterable: IterableModule
    trait IterableModule extends Constructor1[Iterable] { this: Iterable.type => }

    val Map: MapModule
    trait MapModule extends Constructor2[Map] { this: Map.type => }

    val Iterator: IteratorModule
    trait IteratorModule extends Constructor1[Iterator] { this: Iterator.type => }

    def Factory[A: Type, C: Type]: Type[Factory[A, C]]

    def isTuple[A](A: Type[A]): Boolean

    def isSubtypeOf[A, B](S: Type[A], T: Type[B]): Boolean
    def isSameAs[A, B](S: Type[A], T: Type[B]): Boolean

    def prettyPrint[A: Type]: String
  }
  implicit protected class TypeOps[A](private val tpe: Type[A]) {

    final def <:<[B](another: Type[B]): Boolean = Type.isSubtypeOf(tpe, another)
    final def =:=[B](another: Type[B]): Boolean = Type.isSameAs(tpe, another)

    final def isPrimitive: Boolean = Type.primitives.exists(tpe <:< _.Underlying)

    final def isTuple: Boolean = Type.isTuple(tpe)
    final def isAnyVal: Boolean = tpe <:< Type.AnyVal
    final def isOption: Boolean = tpe <:< Type.Option(Type.Any)
    final def isEither: Boolean = tpe <:< Type.Either(Type.Any, Type.Any)
    final def isLeft: Boolean = tpe <:< Type.Either.Left(Type.Any, Type.Any)
    final def isRight: Boolean = tpe <:< Type.Either.Right(Type.Any, Type.Any)
    final def isIterable: Boolean = tpe <:< Type.Iterable(Type.Any)
    final def isMap: Boolean = tpe <:< Type.Map(Type.Any, Type.Any)

    final def asExistential: ExistentialType = ExistentialType(tpe)
  }

  // you can import TypeImplicits.* in your shared code to avoid providing types manually, while avoiding conflicts with
  // implicit types seen in platform-specific scopes
  protected object TypeImplicits {

    implicit val NothingType: Type[Nothing] = Type.Nothing
    implicit val AnyType: Type[Any] = Type.Any
    implicit val AnyValType: Type[AnyVal] = Type.AnyVal
    implicit val BooleanType: Type[Boolean] = Type.Boolean
    implicit val ByteType: Type[Byte] = Type.Byte
    implicit val CharType: Type[Char] = Type.Char
    implicit val ShortType: Type[Short] = Type.Short
    implicit val IntType: Type[Int] = Type.Int
    implicit val LongType: Type[Long] = Type.Long
    implicit val FloatType: Type[Float] = Type.Float
    implicit val DoubleType: Type[Double] = Type.Double
    implicit val UnitType: Type[Unit] = Type.Unit

    implicit def Tuple2Type[A: Type, B: Type]: Type[(A, B)] = Type.Tuple2[A, B]

    implicit def Function1Type[A: Type, B: Type]: Type[A => B] = Type.Function1[A, B]
    implicit def Function2Type[A: Type, B: Type, C: Type]: Type[(A, B) => C] = Type.Function2[A, B, C]

    implicit def ArrayType[A: Type]: Type[Array[A]] = Type.Array[A]
    implicit def OptionType[A: Type]: Type[Option[A]] = Type.Option[A]
    implicit val NoneType: Type[None.type] = Type.Option.None
    implicit def EitherType[L: Type, R: Type]: Type[Either[L, R]] = Type.Either[L, R]
    implicit def LeftType[L: Type, R: Type]: Type[Left[L, R]] = Type.Either.Left[L, R]
    implicit def RightType[L: Type, R: Type]: Type[Right[L, R]] = Type.Either.Right[L, R]
    implicit def IterableType[A: Type]: Type[Iterable[A]] = Type.Iterable[A]
    implicit def MapType[K: Type, V: Type]: Type[Map[K, V]] = Type.Map[K, V]
    implicit def IteratorType[A: Type]: Type[Iterator[A]] = Type.Iterator[A]
    implicit def FactoryType[A: Type, C: Type]: Type[Factory[A, C]] = Type.Factory[A, C]
  }
}
