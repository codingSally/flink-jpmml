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

package io.radicalbit.examples.util

import org.apache.flink.api.java.utils.ParameterTool

trait EnsureParameters {
  def ensureParams(params: ParameterTool) = {
    (params.has("model"), params.has("output")) match {
      case (true, true) => (params.get("model"), params.get("output"))
      case _ => throw new IllegalArgumentException("model are required")
    }
  }
}