package com.timzaak.ucloud

import com.tizaak.ucloud.codec.HmacSHA1
import scalaj.http.{Http, HttpConstants}

case class UFileRequest(
                         key: String,
                         http_method: String = "PUT",
                         content_md5: String = "",
                         content_type: String = "",
                         date: String = "",
                         put_policy: String = ""
                       )

trait UFileSDK {
  val bucket:String
  val privateKey:String
  val publicKey:String
  val hostPrefix:String = "cn-sh2.ufileos.com"
  val connTimeoutMS = 2000
  val readTimeoutMs = 20000
  lazy val requestUrl = s"https://$bucket.$hostPrefix"

  def authorization(req:UFileRequest) = {
    import req._
    val canonicalizedResource = s"""/$bucket/$key"""
    val strTosig = s"${http_method.toUpperCase}\n$content_md5\n$content_type\n$date\n$canonicalizedResource"
    val signature = new HmacSHA1().sign(privateKey, strTosig)
    s"UCloud $publicKey:$signature"
  }

  def putFile(param:UFileRequest,data:Array[Byte]) = {
    import param._
    val encodedKey = HttpConstants.urlEncode(key, "UTF-8")
    var req = Http(s"$requestUrl/$encodedKey")
      .header("Authorization", authorization(param))
      .timeout(connTimeoutMS,readTimeoutMs)
    if(content_md5 != ""){
      req = req.header("Content-Type", content_type)
    }
    if(content_type != ""){
      req = req.header("Content-MD5", content_md5)
    }
    if (date != "") {
      req = req.header("Date", date)
    }
    req.put(data).asString
  }
}
