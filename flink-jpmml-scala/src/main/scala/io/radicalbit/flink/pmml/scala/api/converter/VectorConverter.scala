/*
 * flink-jpmml
 * Copyright (C) 2017 Radicalbit

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.flink.pmml.scala.api.converter

import io.radicalbit.flink.pmml.scala.api.{Evaluator, PmmlInput}
import org.apache.flink.ml.math.{DenseVector, SparseVector, Vector}

import scala.collection.JavaConversions._

/**
  * Create A TypeClass Pattern to convert input data from flink, in Map to pass a JPMML
  */
sealed trait VectorConverter[T] extends Serializable {
  def serializeVector(v: T, eval: Evaluator): Map[String, Any]
}

private[api] object VectorConverter {

  private[api] implicit object VectorToMapbleJpmml extends VectorConverter[Vector] {
    def serializeVector(v: Vector, eval: Evaluator): PmmlInput = {
      v match {
        case denseVector: DenseVector => DenseVectorMapbleJpmml.serializeVector(denseVector, eval)
        case sparseVector: SparseVector => SparseVectorMapbleJpmml.serializeVector(sparseVector, eval)
      }
    }
  }

  private[api] implicit object DenseVectorMapbleJpmml extends VectorConverter[DenseVector] {
    def serializeVector(v: DenseVector, eval: Evaluator): PmmlInput = {
      val getNameInput = getKeyFromModel(eval)

      getNameInput.zip(v.data).toMap
    }
  }

  private[api] implicit object SparseVectorMapbleJpmml extends VectorConverter[SparseVector] {
    def serializeVector(v: SparseVector, eval: Evaluator): PmmlInput = {
      val getNameInput = getKeyFromModel(eval)

      getNameInput.zip(toDenseData(v)).collect { case (key, Some(value)) => (key, value) }.toMap
    }

    private def toDenseData(sparseVector: SparseVector): Seq[Option[Any]] = {
      (0 to sparseVector.size).map { index =>
        if (sparseVector.indices.contains(index)) Some(sparseVector(index)) else None
      }
    }

  }

  private[api] implicit def portingToFlinkJpmml[T: VectorConverter, E <: Evaluator](dataVector: T, eval: E) =
    implicitly[VectorConverter[T]].serializeVector(dataVector, eval)

  /**
    * Used to extract all key from input
    *
    * @param evaluator
    * @return Seq[String]
    *
    */
  private def getKeyFromModel(evaluator: Evaluator) =
    evaluator.getActiveFields.map(_.getName.getValue)

}
