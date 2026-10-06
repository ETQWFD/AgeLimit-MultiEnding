# -*- coding: utf-8 -*-
"""
年龄限制游戏时长，多结局版 - 服务端系统（网易中国版基岩 Python ModSDK）
功能：
  1. 玩家加入世界 -> 强制身份验证（聊天指令：!验证 姓名 年龄）
  2. 未成年人：5 分钟倒计时（每秒刷新提示），时间到锁定（移动/攻击受限）
  3. 成年人：按 56 档年龄区间弹出趣味多结局文案
  4. 玩家数据持久化（服务端数据组件）
注意事项：
  - 本代码按中国版 ModSDK（ApiVersion 1.x）通用 API 编写。
  - 个别组件/API 名称随引擎版本可能调整，请在 MCStudio 中核对报错并反馈修正。
"""
import mod.server.extraServerApi as serverApi
from mod_log import engine_logger as logger

ADULT_AGE = 18
MINOR_SECONDS = 300  # 未成年人限玩 5 分钟

# 状态：0=未验证 1=成人 2=未成年游玩中 3=时间到(锁定)
STATE_UNVERIFIED = 0
STATE_ADULT = 1
STATE_MINOR = 2
STATE_TIMEUP = 3


class AgeLimitServer(serverApi.ModServerSystem):

    def __init__(self, namespace, systemName):
        serverApi.ModServerSystem.__init__(self, namespace, systemName)
        self.timerTask = None
        # 内存态：playerId -> {name, age, remain, state}
        self.players = {}
        # 监听事件
        self.ListenForEvent(
            serverApi.GetEngineNamespace(),
            serverApi.GetEngineSystemName(),
            'PlayerJoinServerEvent', self, self.OnPlayerJoin)
        self.ListenForEvent(
            serverApi.GetEngineNamespace(),
            serverApi.GetEngineSystemName(),
            'PlayerChatEvent', self, self.OnPlayerChat)

    # ---------- 生命周期 ----------
    def InitServer(self):
        # 每秒执行一次倒计时
        self.timerTask = self.AddRepeatingTask(self.TickOnce, 1.0)

    def DestroyServer(self):
        if self.timerTask is not None:
            self.RemoveRepeatingTask(self.timerTask)
            self.timerTask = None

    # ---------- 数据组件（服务端持久化） ----------
    def _data(self, pid):
        return serverApi.GetEngineCompFactory().CreateData(pid)

    def _load(self, pid):
        d = self._data(pid)
        return {
            'name': d.GetData('agelimit_name') or '',
            'age': d.GetData('agelimit_age') or 0,
            'remain': d.GetData('agelimit_remain') or MINOR_SECONDS,
            'state': d.GetData('agelimit_state') or STATE_UNVERIFIED,
        }

    def _save(self, pid, info):
        d = self._data(pid)
        d.AddData('agelimit_name', info['name'])
        d.AddData('agelimit_age', info['age'])
        d.AddData('agelimit_remain', info['remain'])
        d.AddData('agelimit_state', info['state'])

    # ---------- 消息 ----------
    def _tip(self, pid, text):
        """向玩家发送醒目提示（优先 tip，失败则聊天栏）"""
        try:
            game = serverApi.GetEngineCompFactory().CreateGame(self)
            game.SetTipMessage(pid, text)
        except Exception:
            try:
                self.NotifyOneMessage(pid, text)
            except Exception as e:
                logger.warn('tip failed: %s' % e)

    def _chat(self, pid, text):
        try:
            self.NotifyOneMessage(pid, text)
        except Exception as e:
            logger.warn('chat failed: %s' % e)

    # ---------- 锁定 / 解锁（效果模拟，API 随版本核对） ----------
    def _applyEffects(self, pid, on):
        try:
            attr = serverApi.GetEngineCompFactory().CreateAttr(pid)
            for eff in ('slowness', 'mining_fatigue', 'weakness', 'jump_boost'):
                try:
                    if on:
                        attr.AddEffectToEntity(eff, 1, 999999, 1, False)
                    else:
                        attr.RemoveEffectFromEntity(eff)
                except Exception:
                    pass
        except Exception as e:
            logger.warn('effect api may differ: %s' % e)

    def Lock(self, pid, on):
        self._applyEffects(pid, on)

    # ---------- 事件：加入 ----------
    def OnPlayerJoin(self, data):
        pid = data.get('id') or data.get('playerId')
        if not pid:
            return
        info = self._load(pid)
        self.players[pid] = info
        if info['state'] == STATE_UNVERIFIED:
            self.Lock(pid, True)
            self._tip(pid, '未成年人保护：请完成身份验证')
            self._chat(pid, '§e未成年人保护：请输入「!验证 姓名 年龄」完成身份验证')
        elif info['state'] == STATE_TIMEUP:
            self.Lock(pid, True)
            self._tip(pid, '游玩时间已结束，物品栏等已被锁定！\n重新验证请输入「!验证 姓名 年龄」')
        elif info['state'] == STATE_MINOR:
            self._tip(pid, '剩余游玩时间：%s' % self._fmt(info['remain']))

    # ---------- 事件：聊天（验证指令） ----------
    def OnPlayerChat(self, data):
        pid = data.get('playerId')
        msg = (data.get('message') or '').strip()
        if not pid:
            return
        low = msg.lower()
        if low.startswith('!验证') or low.startswith('/验证') or low.startswith('!verify'):
            parts = msg.replace('/', ' ').replace('!', ' ').split()
            # 形如：!验证 姓名 年龄
            if len(parts) >= 3:
                name = parts[1]
                try:
                    age = int(parts[2])
                except ValueError:
                    self._chat(pid, '§c年龄必须是数字')
                    return
                if not name or age < 1:
                    self._chat(pid, '§c请输入有效姓名与年龄')
                    return
                info = self._load(pid)
                info['name'] = name
                info['age'] = age
                if age >= ADULT_AGE:
                    info['state'] = STATE_ADULT
                    self._save(pid, info)
                    self.players[pid] = info
                    self.Lock(pid, False)
                    self._chat(pid, '§a成人验证通过！%s' % self._adultMsg(age))
                    self._tip(pid, '成人验证通过，欢迎回来 %s！' % name)
                else:
                    info['state'] = STATE_MINOR
                    info['remain'] = MINOR_SECONDS
                    self._save(pid, info)
                    self.players[pid] = info
                    self.Lock(pid, False)
                    self._chat(pid, '§e未成年人保护：剩余游玩时间 %s 秒（5分钟）' % MINOR_SECONDS)
                    self._tip(pid, '剩余游玩时间：5:00')
            else:
                self._chat(pid, '§e格式：!验证 姓名 年龄（示例：!验证 小明 12）')

    # ---------- 每秒倒计时 ----------
    def TickOnce(self):
        for pid, info in list(self.players.items()):
            if info['state'] == STATE_MINOR:
                info['remain'] -= 1
                if info['remain'] <= 0:
                    info['remain'] = 0
                    info['state'] = STATE_TIMEUP
                    self._save(pid, info)
                    self.Lock(pid, True)
                    self._tip(pid, '游玩时间已结束，物品栏等已被锁定！')
                    self._chat(pid, '§c未成年人保护：今日游玩时间已到，物品栏等已被锁定。重新验证请输入「!验证 姓名 年龄」')
                else:
                    self._save(pid, info)
                    self._tip(pid, '剩余游玩时间：%s' % self._fmt(info['remain']))
            elif info['state'] == STATE_UNVERIFIED:
                self._tip(pid, '请完成身份验证：!验证 姓名 年龄')

    # ---------- 工具 ----------
    @staticmethod
    def _fmt(total):
        total = max(0, int(total))
        m = total // 60
        s = total % 60
        return '%d:%02d' % (m, s)

    @staticmethod
    def _adultMsg(age):
        # 56 档年龄区间趣味文案（与 Java 版一致）
        table = [
            (18, 40, '玩去吧，哥们，青春就是用来造的，别浪费！'),
            (41, 60, '玩去吧，大哥，事业再忙也得喘口气啊！'),
            (61, 100, '玩去吧，爷爷，您这身板子，广场舞还能领舞！'),
            (101, 200, '玩去吧，太爷爷，您遛弯都算文物巡展，快出去显摆！'),
            (201, 500, '玩去吧，老祖宗，您再不动动，祠堂画像都嫌您宅！'),
            (501, 1000, '玩去吧，古代人，您这资历，出门旅游叫微服私访！'),
            (1001, 1340, '玩去吧，现代都市人，手机屏幕比脸大，快出去看看真实世界！'),
            (1341, 1795, '玩去吧，工业时代人，蒸汽机都淘汰了，您还宅着当锅炉？'),
            (1796, 2405, '玩去吧，中世纪骑士，盔甲都生锈了，去广场上练练剑吧！'),
            (2406, 3222, '玩去吧，古典文明人，亚里士多德都劝您多运动，别光辩论！'),
            (3223, 4317, '玩去吧，史前部落人，火把都烧完了，出去打点野味加餐！'),
            (4318, 5784, '玩去吧，晚期智人，洞穴壁画都画完了，该去画点新风景！'),
            (5785, 7750, '玩去吧，早期智人，石器都磨秃了，出去找块新石头耍耍！'),
            (7751, 10385, '玩去吧，尼安德特人，您的肌肉都变化石了，活动活动吧！'),
            (10386, 13916, '玩去吧，海德堡人，您的长矛都朽了，去森林里练练准头！'),
            (13917, 18647, '玩去吧，直立人，您的骨架都站累了，躺会儿也行，还是出去走走吧！'),
            (18648, 24987, '玩去吧，能人，您的工具都过时了，去捡点新树枝DIY！'),
            (24988, 33483, '玩去吧，南方古猿阿法，露西都叫您出去散步，别老窝着！'),
            (33484, 44868, '玩去吧，南方古猿非洲，非洲大草原都等着您奔跑呢！'),
            (44869, 60123, '玩去吧，湖畔南方古猿，水边待久了，上岸溜达溜达晒晒毛！'),
            (60124, 80565, '玩去吧，始祖地猿，树杈都坐穿了，下来走走接地气！'),
            (80566, 107957, '玩去吧，原康修尔猿，果园都熟透了，去摘点果子补充维C！'),
            (107958, 144663, '玩去吧，森林古猿，丛林法则您都懂，但别忘了玩闹！'),
            (144664, 193849, '玩去吧，西瓦古猿，您的牙齿都磨平了，该去啃点嫩叶换换口味！'),
            (193850, 259758, '玩去吧，腊玛古猿，您的名字都成历史了，去创造点新历史！'),
            (259759, 348076, '玩去吧，原猿，您的尾巴都退化没了，去练练平衡感！'),
            (348077, 466421, '玩去吧，普尔加托里猴，您是最早的灵长类，出去给猴子们做个榜样！'),
            (466422, 625004, '玩去吧，更猴，您的名字就够“更”的，出去更上一层楼！'),
            (625005, 837505, '玩去吧，古鼩鼱，您是哺乳动物先祖，别老缩着，出去探索新大陆！'),
            (837506, 1122257, '玩去吧，摩根齿兽，您算早期哺乳动物，去晒晒太阳补补钙！'),
            (1122258, 1503825, '玩去吧，二齿兽，您像恐龙亲戚，但别宅着，出去挖挖洞运动！'),
            (1503826, 2015125, '玩去吧，埃雷拉龙，您是早期恐龙，跑起来让大地震一震！'),
            (2015126, 2700268, '玩去吧，板龙，您脖子长，伸长脖子看看远处的风景！'),
            (2700269, 3618359, '玩去吧，梁龙，您身子长，出去量量地球周长再回来！'),
            (3618360, 4848600, '玩去吧，霸王龙，您虽是大王，但也要出去巡巡山！'),
            (4848601, 6497124, '玩去吧，翼龙，翅膀都进化了，飞出去兜兜风吧！'),
            (6497125, 8706146, '玩去吧，鱼龙，您在水里游得快，去海里冲个浪！'),
            (8706147, 11666235, '玩去吧，蛇颈龙，您脖子弯弯，去水里捞个月亮玩玩！'),
            (11666236, 15632755, '玩去吧，沧龙，您是海上霸主，去征服一片海域！'),
            (15632756, 20947892, '玩去吧，菊石，您螺旋壳都成型了，去海里转转显摆显摆！'),
            (20947893, 28070175, '玩去吧，三叶虫，您满身铠甲，去沙滩上打滚磨磨壳！'),
            (28070176, 37614035, '玩去吧，奇虾，您曾是顶尖掠食者，去秀秀您的大钳子！'),
            (37614036, 50402807, '玩去吧，皮卡虫，您是脊索动物始祖，去游个泳舒展脊梁！'),
            (50402808, 67539762, '玩去吧，云南虫，您算是早期鱼类，去江河里溯流而上！'),
            (67539763, 90503281, '玩去吧，海口鱼，您是最早脊椎动物，去感受心跳的节奏！'),
            (90503282, 121274397, '玩去吧，海绵，您过滤海水几亿年，该去换换新水了！'),
            (121274398, 162507692, '玩去吧，叠层石，您堆叠成礁石，去海边吹吹海风！'),
            (162507693, 217760307, '玩去吧，颤藻，您是蓝细菌，去水里释放点氧气造福众生！'),
            (217760308, 291798811, '玩去吧，嗜盐菌，您喜欢盐，去死海漂浮当个咸鱼！'),
            (291798812, 391000000, '玩去吧，埃迪卡拉动物群，您是多细胞先驱，去探索新大陆！'),
            (391000001, 524000000, '玩去吧，层侵纪微生物，您太古老，去地底钻个洞探险！'),
            (524000001, 702000000, '玩去吧，真核生物祖先，您有细胞核，去核聚变一下能量！'),
            (702000001, 940000000, '玩去吧，原核生物霸主，您没细胞核但霸气不能丢，出去玩！'),
            (940000001, 1259000000, '玩去吧，化学演化产物，您是分子组合，去合成点快乐激素！'),
            (1259000001, 1687000000, '玩去吧，原始细胞，您是生命起点，去分裂出一个派对！'),
            (1687000001, 2147483647, '玩去吧，LUCA，您是所有生命的最后共同祖先，您不动全地球都不动，快动起来！'),
        ]
        for lo, hi, msg in table:
            if lo <= age <= hi:
                return msg
        return '玩去吧！'
