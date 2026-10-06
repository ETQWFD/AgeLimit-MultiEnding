# -*- coding: utf-8 -*-
"""
年龄限制游戏时长，多结局版 - 网易我的世界中国版（基岩）Python ModSDK 入口
二次开发自 Minor Safety Mod v1.0.0（原作者 SereneCloud，MIT 许可）
版权：ET / ET协会
"""
import mod.server.extraServerApi as serverApi
from ageLimitServer import AgeLimitServer

# 注册服务端系统（中国版 ModSDK 标准入口）
serverSystemCls = serverApi.RegisterSystem('ageLimit', 'server', AgeLimitServer)
