---
navigation:
  parent: index.md
  title: ME气压接口
  icon: appliedpneumatics:me_pressure_interface_block
  position: 40
item_ids:
  - appliedpneumatics:me_pressure_interface_block
---

# ME气压接口

<Row>
  <BlockImage id="me_pressure_interface_block" scale="4" />
</Row>


ME气压接口有着极易用空气交互功能，你只需设置期望气压。ME气压接口会通过从ME网络中取出/插入空气，将其气压尽
可能维持在你的期望值。对外，它可以通过气动工艺的压力管道传输空气，并遵循高气压向低气压流动的逻辑。同时，它
提供了一个物品槽，你可以放入气罐等物品，ME气压接口会为它充气或者抽取它的空气，这取决于双方的气压高低。

## 可用升级卡
 - [容积卡](upgrades.md)：用于提高气压接口的基础容积，同气压下，容积越大存储的空气总量越多；
 - [安全卡](upgrades.md)：为气压接口提供一层额外的保险，当一瞬间输入的气压过大或ME网络不可 用导致接口即将超出危险气压，
安全卡会阻止爆炸并排除多余空气；
 - [真空卡](upgrades.md)：允许气压接口的期望气压为负值，不仅能代替真空泵的功能，还能连接不 闭合的管道从世界中源源不断
地抽取空气，虽然速率有限，但其几乎不消耗任何资源。