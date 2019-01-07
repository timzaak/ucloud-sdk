package com.timzaak.ucloud

import java.nio.file.{ Files, Paths }

import cn.ucloud.ufile.UfileClient
import cn.ucloud.ufile.api.`object`.ObjectConfig
import cn.ucloud.ufile.auth.UfileObjectLocalAuthorization
import com.typesafe.config.ConfigFactory
import org.scalatest._

class UFileSDKTest extends FreeSpec with Matchers {
  // filemgr --action put --bucket timc --key test.jpg --file /Users/timzaak/Downloads/video.jpg
  val config = ConfigFactory.load().getConfig("ucloud")
  val uFile = new UFileSDK {
    override val bucket: String = config.getString("bucket")
    override val privateKey: String = config.getString("privateKey")
    override val publicKey: String = config.getString("publicKey")
  }

  val auther = new UfileObjectLocalAuthorization(config.getString("publicKey"), config.getString("privateKey"))
  val buck = new ObjectConfig("cn-sh2", "ufileos.com")

  "test upfile" in {
    val data = Files.readAllBytes(Paths.get(config.getString("path")))
    val req = UFileRequest("test.jpg")
    val res = uFile.putFile(req, data)
    println(res.body)
    res.is2xx shouldBe true
  }
  "private download url" in {
    val res = uFile.privateDownloadFileUrl("test.jpg")
    println(res)
  }
  "UFile upfile" in {
    val bean = UfileClient
      .`object`(auther, buck)
      .putObject(Files.newInputStream(Paths.get(config.getString("path"))), "image/jpeg")
      .nameAs("test.jpg")
      .withVerifyMd5(false)
      .toBucket(
        config.getString("bucket")
      )
      .execute()
    bean.toString
  }

  "UFile downloadPrivate" in {
    println(
      UfileClient
        .`object`(auther, buck)
        .getDownloadUrlFromPrivateBucket("test.jpg", config.getString("bucket"), 60 * 30)
        .createUrl()
    )
  }
}
