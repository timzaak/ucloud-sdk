## UCloud SDK

PS: 注意，此项目的价值只会是自己用了，具体实现可以参考官方 [ufile-java-sdk](https://github.com/ucloud/ufile-sdk-java) 。
目前该项目的一些 BUG 修复也是参考它的实现。此项目存在的意义，主要是在自己项目中不想引入各种 http client。
后面关于一些加密算法之类的，会替换成 UCloud 的实现，感觉它的实现是 ok 的。

这个主要是自己用的，包含 UCloud API SDK 和 UFile 上传文件（PUT方法，私有文件 url 生成）。

### UCloud API
UCLoud API 很简单，基本上对照下 UAPI的 examples，就懂了。

### UFile

UFile 的官方的文档看得一头雾水，很多变量到底需不需要传，格式怎么样没有明确的说明。在参考 nodejs, js 和 java 版本后，混合写成的。弄不清到底是实现了 V1 还是 V2 版本。 

UFile 最简单的使用方法可以参考下测试用例。基本上，除了基础设定，填个文件Key，就能上传。但建议还是添加上md5和时间戳（就是不知道UFile对时间戳有没有过期处理）。

时间戳的格式， nodejs 的实现是 `new Date().toString()` 也就是， `Mon Aug 06 2018 22:14:05 GMT+0800 (CST)`。 不清楚其他时间格式是否OK。

UFile privateKey和publicKey 可以是UAPI的秘钥，也可以是UFile里的令牌，都ok！

#### UFile 神奇的地方
如果传递 `Content-Type`，就必须传递 `Content-MD5`，否则报签名错误。

### PS
整个代码也就是签名的地方参考下，自己实现就可以了。 UFile 的 HmacSHA1 加密类直接复制 官方JAVASDK.