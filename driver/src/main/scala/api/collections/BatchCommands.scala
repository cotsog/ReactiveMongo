/*
 * Copyright 2012-2013 Stephane Godbillon (@sgodbillon) and Zenexity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactivemongo.api.collections

import reactivemongo.api.SerializationPack
import reactivemongo.api.commands.{
  AggregationFramework => AC,
  CountCommand => CC,
  DistinctCommand => DistC,
  InsertCommand => IC,
  UpdateCommand => UC,
  DeleteCommand => DC,
  DefaultWriteResult,
  LastError,
  ResolvedCollectionCommand,
  FindAndModifyCommand => FMC,
  ShardCollCommand => SCC,
  UnitBox
}

trait BatchCommands[P <: SerializationPack] {
  val pack: P

  val CountCommand: CC[pack.type]
  implicit def CountWriter: pack.Writer[ResolvedCollectionCommand[CountCommand.Count]]
  implicit def CountResultReader: pack.Reader[CountCommand.CountResult]

  val DistinctCommand: DistC[pack.type]
  implicit def DistinctWriter: pack.Writer[ResolvedCollectionCommand[DistinctCommand.Distinct]]
  implicit def DistinctResultReader: pack.Reader[DistinctCommand.DistinctResult]

  val InsertCommand: IC[pack.type]
  implicit def InsertWriter: pack.Writer[ResolvedCollectionCommand[InsertCommand.Insert]]

  val UpdateCommand: UC[pack.type]
  implicit def UpdateWriter: pack.Writer[ResolvedCollectionCommand[UpdateCommand.Update]]
  implicit def UpdateReader: pack.Reader[UpdateCommand.UpdateResult]

  val DeleteCommand: DC[pack.type]
  implicit def DeleteWriter: pack.Writer[ResolvedCollectionCommand[DeleteCommand.Delete]]

  val FindAndModifyCommand: FMC[pack.type]
  implicit def FindAndModifyWriter: pack.Writer[ResolvedCollectionCommand[FindAndModifyCommand.FindAndModify]]
  implicit def FindAndModifyReader: pack.Reader[FindAndModifyCommand.FindAndModifyResult]

  val AggregationFramework: AC[pack.type]
  implicit def AggregateWriter: pack.Writer[ResolvedCollectionCommand[AggregationFramework.Aggregate]]
  implicit def AggregateReader: pack.Reader[AggregationFramework.AggregationResult]

  val ShardCollCommand: SCC[pack.type]
  implicit def ShardCollWriter: pack.Writer[ResolvedCollectionCommand[ShardCollCommand.ShardCollection]]
  implicit def ShardCollReader: pack.Reader[UnitBox.type]

  implicit def DefaultWriteResultReader: pack.Reader[DefaultWriteResult]

  implicit def LastErrorReader: pack.Reader[LastError]
}
