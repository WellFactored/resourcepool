/*
 * Copyright 2018 Well-Factored Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wellfactored.resourcepool

import cats.effect.IO
import org.scalatest.EitherValues

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

class ResourcePoolClosingSpec extends AnyFreeSpecLike with Matchers with EitherValues with IOSpec {
  "calling close" - {
    "should result in the cleanup function being called on each resource currently available in the pool" in {
      case class Resource(var cleanupCalled: Boolean = false)
      val cleanup: Resource => IO[Unit] = { r => r.cleanupCalled = true; IO.unit }
      val resources: List[Resource] = List.fill(5)(Resource())


      val test = for {
        pool <- ResourcePool.of[IO, Resource](resources, (t, _) => IO.pure(t))
        _ <- pool.close(cleanup)
      } yield resources

      val list = test.unsafeRunSync()
      list.foreach(_.cleanupCalled shouldBe true)
    }
  }
}
