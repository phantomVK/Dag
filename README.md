Dag
=========

[![Download](https://api.bintray.com/packages/phantomtvk/Dag/dag/images/download.svg)](https://bintray.com/phantomtvk/Dag/dag/_latestVersion) [![license](https://img.shields.io/badge/License-Apache2.0-brightgreen)](https://github.com/phantomVK/SlideBack/blob/master/LICENSE)

利用有向无环图的节点关系，对 __android.app.Application__ 的初始化任务进行编排，并在多核处理器上并行处理，对系统资源利用率最大化，缩短应用冷启动时间。

开源库不会主动识别任务是 __计算密集型__ 还是 __IO密集型__。这要求开源库使用者，利用开发工具自行测量，然后设置任务间的图关系，并指定任务性质(__计算密集型__，__IO密集型__)。



下载
-------

可通过 __Gradle__ 从 __JCenter__ 下载依赖：

```groovy
repositories {
    google()
    jcenter()
}

dependencies {
    implementation 'com.phantomvk.dag:dag:latest.release'
}
```



兼容性
-------------

* **最低 Android SDK**: Dag 最低支持 API16；
* **编译 Android SDK**: Dag 要求使用 API30 或更新版本进行编译；



许可证
--------

```
Copyright 2019 WenKang Tan(phantomVK)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

