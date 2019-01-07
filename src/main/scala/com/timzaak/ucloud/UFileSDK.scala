package com.timzaak.ucloud

import com.tizaak.ucloud.codec.HmacSHA1
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

trait UFileSDK {
  val bucket: String
  val privateKey: String
  val publicKey: String
  val hostPrefix: String = "cn-sh2.ufileos.com"
  val connTimeoutMS:Int = 2000
  val readTimeoutMs:Int = 20000
  val defaultExpireTime:Int = 60 * 30 // integer类型, 下载链接有效时间，单位为秒
  lazy val requestUrl = s"https://$bucket.$hostPrefix"

  private def encodeUrl(value: String) = HttpConstants.urlEncode(value, HttpConstants.utf8)
  def authorization(req: UFileRequest) = {
    import req._
    val canonicalizedResource = s"""/$bucket/$key"""
    val strTosig = s"${http_method.toUpperCase}\n$content_md5\n$content_type\n$date\n$canonicalizedResource"
    val signature = new HmacSHA1().sign(privateKey, strTosig)
    s"UCloud $publicKey:$signature"
  }

  def putFile(param: UFileRequest, data: Array[Byte]) = {
    import param._
    var req = Http(s"$requestUrl/${encodeUrl(key)}")
      .header("Authorization", authorization(param))
      .timeout(connTimeoutMS, readTimeoutMs)
    if (content_md5 != "") {
      req = req.header("Content-Type", content_type)
    }
    if (content_type != "") {
      req = req.header("Content-MD5", content_md5)
    }
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
    def signature = {
      val strTosig = s"GET\n\n\n$expireStr\n/$bucket/$key"
      new HmacSHA1().sign(privateKey, strTosig)
    }
    s"$url/${encodeUrl(key)}?UCloudPublicKey=${encodeUrl(publicKey)}&Expires=${encodeUrl(expireStr)}&Signature=${encodeUrl(signature)}"
  }

  def privateDownloadFile(
      key: String,
      expire: Int = defaultExpireTime,
      url: String = requestUrl
  ) = {
    Http(privateDownloadFileUrl(key, expire, url))
      .asBytes
  }
}
