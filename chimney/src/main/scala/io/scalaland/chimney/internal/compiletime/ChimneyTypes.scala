package io.scalaland.chimney.internal.compiletime

import io.scalaland.chimney.*
import io.scalaland.chimney.dsl.{ImplicitTransformerPreference, TransformerDefinitionCommons}

private[compiletime] trait ChimneyTypes { this: Types =>

  val ChimneyType: ChimneyTypeModule
  trait ChimneyTypeModule {

    def Transformer[From: Type, To: Type]: Type[Transformer[From, To]]
    def PartialTransformer[From: Type, To: Type]: Type[PartialTransformer[From, To]]
    def Patcher[A: Type, Patch: Type]: Type[Patcher[A, Patch]]

    val PartialResult: PartialResultModule
    trait PartialResultModule { this: PartialResult.type =>
      def apply[A: Type]: Type[partial.Result[A]]
      def Value[A: Type]: Type[partial.Result.Value[A]]
      val Errors: Type[partial.Result.Errors]
    }

    val PreferTotalTransformer: Type[io.scalaland.chimney.dsl.PreferTotalTransformer.type]
    val PreferPartialTransformer: Type[io.scalaland.chimney.dsl.PreferPartialTransformer.type]

    val RuntimeDataStore: Type[TransformerDefinitionCommons.RuntimeDataStore]

    val TransformerCfg: TransformerCfgModule
    trait TransformerCfgModule {
      def Empty: Type[internal.TransformerCfg.Empty]
    }

    val TransformerFlags: TransformerFlagsModule
    trait TransformerFlagsModule { this: TransformerFlags.type =>
      import internal.TransformerFlags.Flag

      val Default: Type[internal.TransformerFlags.Default]
      def Enable[F <: Flag: Type, Flags <: internal.TransformerFlags: Type]
          : Type[internal.TransformerFlags.Enable[F, Flags]]
      def Disable[F <: Flag: Type, Flags <: internal.TransformerFlags: Type]
          : Type[internal.TransformerFlags.Disable[F, Flags]]

      val Flags: FlagsModule
      trait FlagsModule { this: Flags.type =>
        val DefaultValues: Type[internal.TransformerFlags.DefaultValues]
        val BeanGetters: Type[internal.TransformerFlags.BeanGetters]
        val BeanSetters: Type[internal.TransformerFlags.BeanSetters]
        val MethodAccessors: Type[internal.TransformerFlags.MethodAccessors]
        val OptionDefaultsToNone: Type[internal.TransformerFlags.OptionDefaultsToNone]
        def ImplicitConflictResolution[R <: ImplicitTransformerPreference: Type]
            : Type[internal.TransformerFlags.ImplicitConflictResolution[R]]
        val MacrosLogging: Type[internal.TransformerFlags.MacrosLogging]
      }
    }
  }

  // you can import TypeImplicits.* in your shared code to avoid providing types manually, while avoiding conflicts with
  // implicit types seen in platform-specific scopes
  protected object ChimneyTypeImplicits {

    implicit def TransformerType[From: Type, To: Type]: Type[Transformer[From, To]] = ChimneyType.Transformer[From, To]
    implicit def PartialTransformerType[From: Type, To: Type]: Type[PartialTransformer[From, To]] =
      ChimneyType.PartialTransformer[From, To]
    implicit def PatcherType[A: Type, Patch: Type]: Type[Patcher[A, Patch]] = ChimneyType.Patcher[A, Patch]

    implicit def PartialResultType[A: Type]: Type[partial.Result[A]] = ChimneyType.PartialResult[A]
    implicit def PartialResultValueType[A: Type]: Type[partial.Result.Value[A]] = ChimneyType.PartialResult.Value[A]
    implicit val PartialResultErrorsType: Type[partial.Result.Errors] = ChimneyType.PartialResult.Errors
  }
}
