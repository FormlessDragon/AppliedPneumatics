---
navigation:
  parent: index.md
  title: 新增P2P
  icon: appliedpneumatics:air_p2p_tunel
  position: 30
item_ids:
  - air_p2p_tunel
  - heat_p2p_tunel
---

# 新增P2P

<Row>
    <ItemImage id="air_p2p_tunel" scale="4" />
    <ItemImage id="heat_p2p_tunel" scale="4" />
</Row>

## 空气P2P
使用任意能存储空气的物品右键完成调频。空气P2P的输入端会对外暴露所有输出端的压强均值，当具有更高压强的气压
系统与其连接时，会向其中输入空气，输入端会将输入的空气按其均值与每个输出端之间的压强差加权分配到各个输出端
中。

## 温度P2P
使用压缩铁块、热管或散热片右键完成调频。温度P2P的输入端会对外暴露按热容加权计算后的所有输出端温度均值，当
其连接到另一个热量系统时，输入端会与其进行热量交换，并以输出端热容x输入端与输出端的温差为权重分配到各个输
出端中。


