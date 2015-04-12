---
layout: default
title: 主页
---


# 简介
词道(Vocabulary Anytime Anywhere)是一个小巧的工具，它可以方便你在阅读时查询不熟悉的单词，并且随后协助你回顾以掌握这些单词。

词道目前包含字典和回顾清单。字典用于在阅读时查询没掌握的单词。查询过的单词会被自动保存到回顾清单中。回顾清单帮助您在碎片时间通过反复识记已掌握生词。

# 安装
<a href="https://chrome.google.com/webstore/detail/词道/cgkjfohooamppcndhnmamboiipnmeaak" target="_blank" class="get-chrome"><img src="{{ site.baseurl }}/images/chromewebstore.png" alt="Download from Chrome Web Store" class="chrome-webstore"></a>  
VAA for Chrome

<a href="https://play.google.com/store/search?q=pub:vaa" target="_blank"><img alt="Get it on Google Play" src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png" /></a>  
VAA for Android (Coming Soon)

# 用法
0. 词道字典  
  + 快捷查词（ Chrome Extension ）  
    浏览网页时，通过键盘快捷键 `Ctrl+Q` （默认）或 `Alt+Q` ，唤出词道快捷查词搜索框，输入要查询的单词查询：
    <img src="{{ site.baseurl }}/images/lookup_via_shortcut.png" alt="快捷查询" class="img-responsive">
    等待片刻后，会弹出含有单词解释的浮动窗口：
    <img src="{{ site.baseurl }}/images/result_shortcut.png" alt="浮动窗口" class="img-responsive">
  + 划词查询（ Chrome Extension）  
    用鼠标选中要查询的单词，等待片刻后，会弹出含有单词解释的浮动窗口。若开启了修饰键（默认为 `Ctrl` ），则鼠标选中单词前，需先按下修饰键并保持，待单词选中后释放。
    <img src="{{ site.baseurl }}/images/lookup_via_mouse.png" alt="划词查询" class="img-responsive">
  + 右键查词（ Chrome Extension）  
    浏览网页时，选中待查询的单词后，通过右键菜单中“使用 VAA 查询”，触发查词。
    <img src="{{ site.baseurl }}/images/lookup_option_action.png" alt="右键查询" class="img-responsive">
  + 分享查词（ Android App）

    0. 在手机上任意可以选择文本的位置，选中要查询的单词:
      <img src="{{ site.baseurl }}/images/dk_lookup_share_selected.png" alt="分享查词 — 选择" class="img-responsive">
    1. 点击“分享”:
      <img src="{{ site.baseurl }}/images/dk_lookup_share_action.png" alt="分享查词 — 分享" class="img-responsive">
    2. 选择“词道字典”
      <img src="{{ site.baseurl }}/images/dk_lookup_share_vaa_dict.png" alt="分享查词 — 米粒字典" class="img-responsive">
    3. 稍等片刻后，会弹出含有单词解释的浮动窗口。
      <img src="{{ site.baseurl }}/images/dk_lookup_share_float_card.png" alt="分享查词 — 结果" class="img-responsive">

1. 词道生词本  
在阅读之后的碎片时间里，可以通过电脑或者手机，随时随地的查看生词本来回顾生词。
  + Chrome 上的生词本
    <img src="{{ site.baseurl }}/images/chrome_recall_list.png" alt="生词本 - Chrome" class="img-responsive">
  + Android上的生词本
    <img src="{{ site.baseurl }}/images/dk_android_recall_list.png" alt="生词本 - Android" class="img-responsive">
  + Chrome 上的回顾提醒
    <img src="{{ site.baseurl }}/images/notification_recall_chrome.png" alt="回顾提醒 — Chrome" class="img-responsive">
  + Android 上的回顾提醒
    <img src="{{ site.baseurl }}/images/dk_notif_recall_android.png" alt="回顾提醒 — Android" class="img-responsive">

# FAQ
+ 问：查询的单词为什么没有马上出现在生词本里？  
答：查询的单词加入回顾队列后，需达到特定的回顾时间点，才会出现在回顾词单里。目前这个回顾时间点符合艾宾浩斯记忆曲线。具体的回顾时间点参考[这里](http://www.douban.com/group/topic/1054905/)。
+ 问：艾宾浩斯记忆曲线有用吗？  
答：艾宾浩斯记忆曲线仅仅是一个科学工作者的实验结果，有参考意义。但对于记单词这件事情来说，它不是关键，重复才是王道。艾宾浩斯记忆曲线只是重复的一种方式，反复回顾吧，一定有用！