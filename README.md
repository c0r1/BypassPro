# BypassPro

一个自动化 bypass 403/auth 的 Burpsuite 插件，重构自：[AutoBypass403-BurpSuite](https://github.com/p0desta/AutoBypass403-BurpSuite)

申明：该工具只用于安全自测，禁止用于非法用途



## Note

- ==**插件采用 Montoya API 进行开发，使用插件需要升级你的 BurpSuite 版本（>=2024.7）**==



## How to Run

1. 解压缩后在 Burp Extensions 中加载 BypassPro.jar

![img](./README.assets/1.png)

2. 选择目标请求，右键点击 "Send to BypassPro”

![img](./README.assets/2.png)





支持多目标请求扫描：

![img](./README.assets/3.png)



3. 选择 BypassPro 标签页，查看结果

​	支持响应内容相似度匹配(from @pmiaowu HostCollision); 如果返回结果相似度很高，则不展示

![img](./README.assets/4.png)



## Fuzz Rules

<img src="./README.assets/5.png" width="360" height="515">



## Custom rules

修改 BypassPro.jar 所在目录下 resources/config.yml 内容，可添加自定义扫描规则

==**PS：修改配置文件后，需要重载 BypassPro 插件，才能使新配置生效**==

<img src="./README.assets/6.png" width="500" height="600">



## Thanks

- https://github.com/p0desta/AutoBypass403-BurpSuite
- https://github.com/pmiaowu/BurpShiroPassiveScan
