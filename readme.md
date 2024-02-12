

TODO：

- XUI
- 指南针
- 权限检查
- 刷新到当前位置
- 调整坐标整理到单独文件



GPS：

[使用Android原生的Api进行GPS定位获取位置信息_android 原生方式获取gps惯导数据-CSDN博客](https://blog.csdn.net/qq_19560943/article/details/71250420)

地图：

[Android地图SDK | 腾讯位置服务 (qq.com)](https://lbs.qq.com/mobile/androidMapSDK/developerGuide/showMap)

[Android地图SDK | 腾讯位置服务 (qq.com)](https://lbs.qq.com/mobile/androidMapSDK/developerGuide/showLocation)





腾讯地图自己的GPS定位工具不单单用到离线的GPS功能 会夹杂很多其他的 所以使用原生的自带的传感器

这会导致直接使用GPS经纬度到腾讯地图中会有大约1km内的固定偏移

[WebService API | 腾讯位置服务 (qq.com)](https://lbs.qq.com/service/webService/webServiceGuide/webServiceTranslate)提供位置坐标转换

但是离线条件不允许通过http请求 并且每次定位刷新都要转换很麻烦

查询到腾讯地图是gcj02  gps是wgs84 网上可以搜到数学转换代码

[coordTransform_py/coordTransform_utils.py at master · wandergis/coordTransform_py (github.com)](https://github.com/wandergis/coordTransform_py/blob/master/coordTransform_utils.py)
