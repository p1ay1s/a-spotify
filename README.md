# a-spotify

> 简介: 基于 Spotify Remote Sdk 实现的安卓 Spotify 音乐控制端, 需要配合 spotify 官方 app 使用

## 技术亮点

- bitmap, collection 的简单缓存
- 对 viewbinding 进行封装的 activity, fragment 等的基类 (ViewBindingXXXXX)
- MVVM 架构
- 针对 recyclerview item 的数据预加载
- 自定义 view
  如:
    - 跑马灯 textview
    - 带有'加载中状态'的seekbar
    - 基本复刻的 Apple music 播放器
    - 类似 nav controller 的 fragment 管理 view
- 对 spotify remote 使用方法的集成