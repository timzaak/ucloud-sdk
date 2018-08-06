package com.timzaak.ucloud

import java.nio.file.{Files, Paths}
import com.typesafe.config.ConfigFactory
import org.scalatest._

class UFileSDKTest extends FreeSpec with Matchers {
  "test upfile" in {
    val config = ConfigFactory.load().getConfig("ucloud")
    val uFile = new UFileSDK {
      override val bucket: String = config.getString("bucket")
      override val privateKey: String = config.getString("privateKey")
      override val publicKey: String = config.getString("publicKey")
    }
    val req = UFileRequest("test.jpg")
    val data = Files.readAllBytes(Paths.get(config.getString("path")))
    val res = uFile.putFile(req, data)
    res.is2xx shouldBe true



  }
}
