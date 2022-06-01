package com.timzaak.ucloud

import com.tizaak.ucloud.codec.HmacSHA1
import org.apache.commons.codec.digest.DigestUtils
import scalaj.http.{ Http, HttpConstants }

case class UFileRequest(
    key: String,
    http_method: String = "PUT",
    content_md5: String = "",
    content_type: String = "",
    date: String = "",
    put_policy: String = "",
    expires: String = ""
)

case class ObjectProfile(
    contentType: String,
    contentLength: String,
    eTag: String,
    acceptRanges: String,
    lastModified: String,
    vary: String
)

trait UFileSDK {
  val bucket: String
  protected val publicKey: String
  protected val privateKey: String
  protected val hostPrefix: String = "cn-sh2.ufileos.com"
  val connTimeoutMS: Int = 2000
  val readTimeoutMs: Int = 20000
  val defaultExpireTime: Int = 60 * 30 // integer类型, 下载链接有效时间，单位为秒
  lazy val requestUrl = s"https://$bucket.$hostPrefix"

  private def encodeUrl(value: String) = HttpConstants.urlEncode(value, HttpConstants.utf8)
  def authorization(key: String, httpMethod: String = "POST", contentMD5: String = "", contentType: String= "", date: String = "") = {

    val canonicalizedResource = s"""/$bucket/$key"""
    val strTosig = s"${httpMethod.toUpperCase}\n$contentMD5\n$contentType\n$date\n$canonicalizedResource"
    val signature = new HmacSHA1().sign(privateKey, strTosig)
    s"UCloud $publicKey:$signature"
  }

  def putFile(param: UFileRequest, data: Array[Byte]) = {
    import param._
    var req = Http(s"$requestUrl/${encodeUrl(key)}")
      .timeout(connTimeoutMS, readTimeoutMs)
    val md5 = if (content_type != "" && content_md5 == "") {
      DigestUtils.md5Hex(data)
    } else {
      content_md5
    }
    req = req.header("Content-MD5", md5)
    req = req.header("Content-Type", content_type)

    req = req.header("Authorization", authorization(key, http_method, md5, content_type, date))
    if (date != "") {
      req = req.header("Date", date)
    }

    req.put(data).asString
  }

  def publicDownloadFileUrl(key: String, url: String = requestUrl): String = {
    s"$url/${encodeUrl(key)}"
  }

  def privateDownloadFileUrl(
      key: String,
      expire: Int = defaultExpireTime,
      url: String = requestUrl
  ): String = {
    val expireStr = (System.currentTimeMillis() / 1000 + expire).toString
    val signature = {
      val strToSig = s"GET\n\n\n$expireStr\n/$bucket/$key"
      new HmacSHA1().sign(privateKey, strToSig)
    }
    s"$url/$key?UCloudPublicKey=$publicKey&Expires=$expireStr&Signature=$signature"
  }

  def privateDownloadFile(
      key: String,
      expire: Int = defaultExpireTime,
      url: String = requestUrl
  ) = {
    Http(privateDownloadFileUrl(key, expire, url)).asBytes
  }

  def getInfo(key: String) = {
    val result = Http(s"$requestUrl/$key")
      .header("Authorization", authorization(key, "HEAD", "","",""))
      .timeout(connTimeoutMS, readTimeoutMs)
      .method("HEAD")
      .asBytes
    if (result.is2xx) {
      Some(
        ObjectProfile(
          contentType = result.header("Content-Type").get,
          contentLength = result.header("Content-Length").getOrElse("0"),
          eTag = result.header("ETAG").getOrElse(""),
          acceptRanges = result.header("Accept-Ranges").getOrElse(""),
          lastModified = result.header("Last-Modified").getOrElse(""),
          vary = result.header("Vary").getOrElse("")
        )
      )
    } else {
      None
    }
  }
}
