---
navigation:
  parent: index.md
  title: 亚马龙处理站
item_ids:
  - appliedpneumatics:me_amadron_process_station
  - appliedpneumatics:me_amadron_extended_process_station
---

# 亚马龙处理站

处理来自亚马龙无线终端和ME网络自动合成的订单。当订单进入队列后，处理站每隔200游戏刻向亚马龙购物中心发送无人机请求。来自亚马龙公司的无人机会前来收取
资源并派送货物。这个过程中，你需要始终保证处理站的上方无遮挡物，否则亚马龙无人机可能会拒绝出动，这将导致你的处理站出现资源堵塞，需要手动清理。

如果处理站在订单队列非空时被破坏，未能及时完成的订单资源都会返还到ME网络中，如果ME网络不可用或者空间不足，则会将剩余的资源作为掉落物弹出。
