// ============================================================
// 年龄限制游戏时长，多结局版（基岩版转制）
// 原模组: Minor Safety Mod v1.0.0 (Forge 1.20.1, by SereneCloud)
// 二次开发: ET | 许可证: MIT（保留原作者署名）
// 兼容: Minecraft 基岩版 1.20.60 ~ 1.26+（依赖 @minecraft/server 1.8.0）
// 功能: 登录身份验证(姓名+年龄) / 未成年人5分钟限时锁定 /
//       成人按年龄段趣味提示(多结局) / 验证令牌 / 聊天指令兜底
// 显示方案（基岩版无 HUD 左上角文本 API，采用三重保障）:
//   - 顶部大字标题 setTitle（每 5 秒刷新，最接近"顶部倒计时"）
//   - 右侧常驻记分板 scoreboard sidebar（实时倒数，最持久）
//   - 底部动作条 setActionBar（每秒刷新）
// ============================================================
import { world, system, ItemStack } from "@minecraft/server";
import { ModalFormData, MessageFormData } from "@minecraft/server-ui";

// ---------- 常量 ----------
const ADULT_AGE = 18;
const MINOR_SECONDS = 300; // 未成年人限玩 5 分钟
const TICK_PER_SECOND = 20;
const PROP_NAME = "agelimit:name";
const PROP_AGE = "agelimit:age";
const PROP_REMAIN = "agelimit:remaining"; // 剩余秒数
const PROP_STATE = "agelimit:state";       // 0=未验证 1=成人 2=未成年游玩中 3=时间到(锁定)
const VERIFY_ITEM = "agelimit:verify_token";
const SCOREBOARD_ID = "agelimit_timer";
const UNVERIFIED = 0, ADULT = 1, MINOR_PLAYING = 2, TIME_UP = 3;

// 正在展示的表单（防止重复弹窗）
const openForms = new Set();

// ---------- 多结局：成人趣味提示（移植自 AgeMessageProvider） ----------
const AGE_MESSAGES = [
  [18, 40, "玩去吧，哥们，青春就是用来造的，别浪费！"],
  [41, 60, "玩去吧，大哥，事业再忙也得喘口气啊！"],
  [61, 100, "玩去吧，爷爷，您这身板子，广场舞还能领舞！"],
  [101, 200, "玩去吧，太爷爷，您遛弯都算文物巡展，快出去显摆！"],
  [201, 500, "玩去吧，老祖宗，您再不动动，祠堂画像都嫌您宅！"],
  [501, 1000, "玩去吧，古代人，您这资历，出门旅游叫微服私访！"],
  [1001, 1340, "玩去吧，现代都市人，手机屏幕比脸大，快出去看看真实世界！"],
  [1341, 1795, "玩去吧，工业时代人，蒸汽机都淘汰了，您还宅着当锅炉？"],
  [1796, 2405, "玩去吧，中世纪骑士，盔甲都生锈了，去广场上练练剑吧！"],
  [2406, 3222, "玩去吧，古典文明人，亚里士多德都劝您多运动，别光辩论！"],
  [3223, 4317, "玩去吧，史前部落人，火把都烧完了，出去打点野味加餐！"],
  [4318, 5784, "玩去吧，晚期智人，洞穴壁画都画完了，该去画点新风景！"],
  [5785, 7750, "玩去吧，早期智人，石器都磨秃了，出去找块新石头耍耍！"],
  [7751, 10385, "玩去吧，尼安德特人，您的肌肉都变化石了，活动活动吧！"],
  [10386, 13916, "玩去吧，海德堡人，您的长矛都朽了，去森林里练练准头！"],
  [13917, 18647, "玩去吧，直立人，您的骨架都站累了，躺会儿也行，还是出去走走吧！"],
  [18648, 24987, "玩去吧，能人，您的工具都过时了，去捡点新树枝DIY！"],
  [24988, 33483, "玩去吧，南方古猿阿法，露西都叫您出去散步，别老窝着！"],
  [33484, 44868, "玩去吧，南方古猿非洲，非洲大草原都等着您奔跑呢！"],
  [44869, 60123, "玩去吧，湖畔南方古猿，水边待久了，上岸溜达溜达晒晒毛！"],
  [60124, 80565, "玩去吧，始祖地猿，树杈都坐穿了，下来走走接地气！"],
  [80566, 107957, "玩去吧，原康修尔猿，果园都熟透了，去摘点果子补充维C！"],
  [107958, 144663, "玩去吧，森林古猿，丛林法则您都懂，但别忘了玩闹！"],
  [144664, 193849, "玩去吧，西瓦古猿，您的牙齿都磨平了，该去啃点嫩叶换换口味！"],
  [193850, 259758, "玩去吧，腊玛古猿，您的名字都成历史了，去创造点新历史！"],
  [259759, 348076, "玩去吧，原猿，您的尾巴都退化没了，去练练平衡感！"],
  [348077, 466421, "玩去吧，普尔加托里猴，您是最早的灵长类，出去给猴子们做个榜样！"],
  [466422, 625004, "玩去吧，更猴，您的名字就够“更”的，出去更上一层楼！"],
  [625005, 837505, "玩去吧，古鼩鼱，您是哺乳动物先祖，别老缩着，出去探索新大陆！"],
  [837506, 1122257, "玩去吧，摩根齿兽，您算早期哺乳动物，去晒晒太阳补补钙！"],
  [1122258, 1503825, "玩去吧，二齿兽，您像恐龙亲戚，但别宅着，出去挖挖洞运动！"],
  [1503826, 2015125, "玩去吧，埃雷拉龙，您是早期恐龙，跑起来让大地震一震！"],
  [2015126, 2700268, "玩去吧，板龙，您脖子长，伸长脖子看看远处的风景！"],
  [2700269, 3618359, "玩去吧，梁龙，您身子长，出去量量地球周长再回来！"],
  [3618360, 4848600, "玩去吧，霸王龙，您虽是大王，但也要出去巡巡山！"],
  [4848601, 6497124, "玩去吧，翼龙，翅膀都进化了，飞出去兜兜风吧！"],
  [6497125, 8706146, "玩去吧，鱼龙，您在水里游得快，去海里冲个浪！"],
  [8706147, 11666235, "玩去吧，蛇颈龙，您脖子弯弯，去水里捞个月亮玩玩！"],
  [11666236, 15632755, "玩去吧，沧龙，您是海上霸主，去征服一片海域！"],
  [15632756, 20947892, "玩去吧，菊石，您螺旋壳都成型了，去海里转转显摆显摆！"],
  [20947893, 28070175, "玩去吧，三叶虫，您满身铠甲，去沙滩上打滚磨磨壳！"],
  [28070176, 37614035, "玩去吧，奇虾，您曾是顶尖掠食者，去秀秀您的大钳子！"],
  [37614036, 50402807, "玩去吧，皮卡虫，您是脊索动物始祖，去游个泳舒展脊梁！"],
  [50402808, 67539762, "玩去吧，云南虫，您算是早期鱼类，去江河里溯流而上！"],
  [67539763, 90503281, "玩去吧，海口鱼，您是最早脊椎动物，去感受心跳的节奏！"],
  [90503282, 121274397, "玩去吧，海绵，您过滤海水几亿年，该去换换新水了！"],
  [121274398, 162507692, "玩去吧，叠层石，您堆叠成礁石，去海边吹吹海风！"],
  [162507693, 217760307, "玩去吧，颤藻，您是蓝细菌，去水里释放点氧气造福众生！"],
  [217760308, 291798811, "玩去吧，嗜盐菌，您喜欢盐，去死海漂浮当个咸鱼！"],
  [291798812, 391000000, "玩去吧，埃迪卡拉动物群，您是多细胞先驱，去探索新大陆！"],
  [391000001, 524000000, "玩去吧，层侵纪微生物，您太古老，去地底钻个洞探险！"],
  [524000001, 702000000, "玩去吧，真核生物祖先，您有细胞核，去核聚变一下能量！"],
  [702000001, 940000000, "玩去吧，原核生物霸主，您没细胞核但霸气不能丢，出去玩！"],
  [940000001, 1259000000, "玩去吧，化学演化产物，您是分子组合，去合成点快乐激素！"],
  [1259000001, 1687000000, "玩去吧，原始细胞，您是生命起点，去分裂出一个派对！"],
  [1687000001, 2147483647, "玩去吧，LUCA，您是所有生命的最后共同祖先，您不动全地球都不动，快动起来！"]
];

function getAdultMessage(age) {
  for (const [min, max, msg] of AGE_MESSAGES) {
    if (age >= min && age <= max) return msg;
  }
  return "玩去吧！";
}

// ---------- 状态读写 ----------
function getState(p) { return p.getDynamicProperty(PROP_STATE) ?? UNVERIFIED; }
function setState(p, s) { p.setDynamicProperty(PROP_STATE, s); }

// ---------- 常驻记分板（右侧实时倒数，最可靠的 HUD 替代） ----------
function ensureObjective() {
  try {
    let obj = world.scoreboard.getObjective(SCOREBOARD_ID);
    if (!obj) {
      obj = world.scoreboard.addObjective(SCOREBOARD_ID, "剩余游玩时间");
    }
    return obj;
  } catch (e) { return null; }
}

function setScoreboard(player, secs) {
  try {
    const obj = ensureObjective();
    if (!obj) return;
    obj.setScore(player, Math.max(0, secs));
    world.scoreboard.setObjectiveAtDisplaySlot("sidebar", obj);
  } catch (e) { }
}

function clearScoreboard(player) {
  try {
    const obj = ensureObjective();
    if (obj && player) obj.removeParticipant(player);
  } catch (e) { }
}

// ---------- 锁定 / 解锁 ----------
function lock(player) {
  try {
    if (player.inputPermissions) {
      player.inputPermissions.movementEnabled = false;
      // 保留 cameraEnabled（不锁视角，避免干扰表单交互）
    }
  } catch (e) { /* 旧版本不支持时忽略 */ }
  player.addEffect("slowness", 999999, { amplifier: 255, showParticles: false });
  player.addEffect("jump_boost", 999999, { amplifier: 255, showParticles: false });
  player.addEffect("mining_fatigue", 999999, { amplifier: 255, showParticles: false });
  player.addEffect("weakness", 999999, { amplifier: 255, showParticles: false });
}

function unlock(player) {
  try {
    if (player.inputPermissions) {
      player.inputPermissions.movementEnabled = true;
      player.inputPermissions.cameraEnabled = true;
    }
  } catch (e) { }
  player.removeEffect("slowness");
  player.removeEffect("jump_boost");
  player.removeEffect("mining_fatigue");
  player.removeEffect("weakness");
  player.onScreenDisplay.setActionBar("");
}

// ---------- 顶部大字标题（每 N 秒刷新）+ 副标题常驻 ----------
function showTopTimer(player, remain) {
  try {
    const text = "§e剩余游玩时间: §f" + formatTime(remain);
    // 主标题：屏幕中上方大字（最醒目）
    player.onScreenDisplay.setTitle(text, { fadeInDuration: 0.1, stayDuration: 1.5, fadeOutDuration: 0.2 });
    // 副标题：标题正下方常驻（stayDuration 长，贴近"顶部倒计时"观感）
    player.onScreenDisplay.updateSubtitle("§6请合理安排游戏时间，时间到后将自动锁定");
  } catch (e) { }
}

// ---------- 时间到：常驻大字循环（直到玩家重新验证才停止） ----------
function timeUpBanner(player) {
  try {
    player.onScreenDisplay.setTitle("§c⚠ 游玩时间已结束 ⚠", { fadeInDuration: 0.1, stayDuration: 2.6, fadeOutDuration: 0.3 });
    player.onScreenDisplay.updateSubtitle("§f物品栏等已被锁定\n§e点击「我已长大」重新验证");
  } catch (e) { }
}

// ---------- 身份验证（ModalForm） ----------
function openVerification(player) {
  const key = player.id;
  if (openForms.has(key)) return;
  openForms.add(key);

  const defaultName = player.getDynamicProperty(PROP_NAME) ?? "";
  const form = new ModalFormData()
    .title("未成年人保护 - 身份验证")
    .textField("请输入姓名：", defaultName || "姓名")
    .textField("请输入年龄：", "年龄（数字）");

  form.show(player).then((res) => {
    openForms.delete(key);
    if (res.canceled) {
      // 未验证状态不允许取消，稍后重新弹出
      system.runTimeout(() => {
        if (getState(player) === UNVERIFIED) openVerification(player);
      }, TICK_PER_SECOND * 3);
      return;
    }
    const name = String(res.formValues[0] ?? "").trim();
    const ageStr = String(res.formValues[1] ?? "").trim();
    const age = Number(ageStr);
    if (!name) {
      player.sendMessage("§c未成年人保护：姓名不能为空");
      system.runTimeout(() => openVerification(player), TICK_PER_SECOND);
      return;
    }
    if (!Number.isInteger(age) || age < 1 || age > 2147482617) {
      player.sendMessage("§c未成年人保护：请输入有效年龄（1~2147482617 的数字）");
      system.runTimeout(() => openVerification(player), TICK_PER_SECOND);
      return;
    }
    player.setDynamicProperty(PROP_NAME, name);
    player.setDynamicProperty(PROP_AGE, age);

    if (age >= ADULT_AGE) {
      // ---- 结局A：成人放行，多结局趣味提示 ----
      setState(player, ADULT);
      unlock(player);
      clearScoreboard(player);
      const msg = getAdultMessage(age);
      new MessageFormData()
        .title("§a成人验证通过！")
        .body(msg + "\n\n欢迎回来，" + name + "！")
        .button1("好嘞，玩去了！")
        .show(player).then((r) => { openForms.delete(key); }).catch(() => {});
    } else {
      // ---- 结局B：未成年人限时游玩 ----
      setState(player, MINOR_PLAYING);
      player.setDynamicProperty(PROP_REMAIN, MINOR_SECONDS);
      unlock(player);
      setScoreboard(player, MINOR_SECONDS);
      showTopTimer(player, MINOR_SECONDS);
      player.sendMessage("§e未成年人保护：剩余游玩时间 " + MINOR_SECONDS + " 秒（5分钟），请合理安排游戏时间。");
    }
  }).catch(() => { openForms.delete(key); });
}

// ---------- 时间到（MessageForm + 常驻大字兜底） ----------
function showTimeUp(player) {
  const key = player.id + ":timeup";
  // 常驻大字兜底：即使表单被系统吞掉，玩家也能持续看到限制提示
  timeUpBanner(player);
  if (openForms.has(key)) return;
  openForms.add(key);
  const f = new MessageFormData()
    .title("§c未成年人保护 - 游玩时间已结束")
    .body("今日游玩时间已达到限制，物品栏等已被锁定。\n\n点击「我已长大」重新验证。")
    .button1("我已长大")
    .button2("离开游戏");
  f.show(player).then((res) => {
    openForms.delete(key);
    if (!res.canceled && res.selection === 0) {
      clearScoreboard(player);
      openVerification(player);
    } else {
      // 取消或选择离开：保持锁定，稍后再提醒
      system.runTimeout(() => {
        if (getState(player) === TIME_UP) showTimeUp(player);
      }, TICK_PER_SECOND * 6);
    }
  }).catch(() => {
    openForms.delete(key);
    // 弹窗失败：3 秒后重试，保证最终弹出
    system.runTimeout(() => {
      if (getState(player) === TIME_UP) showTimeUp(player);
    }, TICK_PER_SECOND * 3);
  });
}

// ---------- 发放验证令牌 ----------
function ensureToken(player) {
  try {
    const inv = player.getComponent("inventory")?.container;
    if (!inv) return;
    for (let i = 0; i < inv.size; i++) {
      const it = inv.getItem(i);
      if (it && it.typeId === VERIFY_ITEM) return;
    }
    inv.addItem(new ItemStack(VERIFY_ITEM, 1));
  } catch (e) { }
}

// ---------- 玩家进入世界 ----------
world.afterEvents.playerSpawn.subscribe((ev) => {
  const p = ev.player;
  // 清理可能残留的表单锁（防止弹窗被永久吞掉）
  openForms.delete(p.id);
  openForms.delete(p.id + ":timeup");
  if (!ev.initialSpawn) return;
  const st = getState(p);
  if (st === UNVERIFIED) {
    lock(p);
    setScoreboard(p, 0);
    p.onScreenDisplay.setActionBar("§c未成年人保护 - 请先完成身份验证（使用验证令牌或输入 !verify）");
    ensureToken(p);
    system.runTimeout(() => { if (getState(p) === UNVERIFIED) openVerification(p); }, TICK_PER_SECOND * 2);
  } else if (st === TIME_UP) {
    lock(p);
    setScoreboard(p, 0);
    ensureToken(p);
    system.runTimeout(() => { if (getState(p) === TIME_UP) showTimeUp(p); }, TICK_PER_SECOND * 2);
  } else if (st === MINOR_PLAYING) {
    // 未成年玩家重进世界：继续计时
    const remain = p.getDynamicProperty(PROP_REMAIN) ?? MINOR_SECONDS;
    setScoreboard(p, remain);
    p.onScreenDisplay.setActionBar("§e未成年人保护 - 剩余游玩时间: " + formatTime(remain));
  }
});

// ---------- 验证令牌使用 ----------
world.afterEvents.itemUse.subscribe((ev) => {
  if (ev.itemStack && ev.itemStack.typeId === VERIFY_ITEM) {
    const st = getState(ev.source);
    if (st === UNVERIFIED || st === TIME_UP || st === ADULT) {
      openVerification(ev.source);
    }
  }
});

// ---------- 聊天指令兜底（/verify /minorsafety /msverify / !verify） ----------
world.beforeEvents.chatSend.subscribe((ev) => {
  const msg = ev.message.trim().toLowerCase();
  if (msg === "/verify" || msg === "!verify" || msg === "/minorsafety" || msg === "/msverify") {
    ev.cancel = true;
    const st = getState(ev.sender);
    if (st !== MINOR_PLAYING) openVerification(ev.sender);
  }
});

// ---------- 每秒主循环 ----------
system.runInterval(() => {
  for (const p of world.getPlayers()) {
    const st = getState(p);
    if (st === MINOR_PLAYING) {
      let remain = p.getDynamicProperty(PROP_REMAIN) ?? MINOR_SECONDS;
      remain -= 1;
      if (remain <= 0) {
        p.setDynamicProperty(PROP_REMAIN, 0);
        setState(p, TIME_UP);
        lock(p);
        setScoreboard(p, 0);
        p.onScreenDisplay.setActionBar("§c未成年人保护 - 游玩时间已结束，物品栏已锁定");
        showTimeUp(p);
      } else {
        p.setDynamicProperty(PROP_REMAIN, remain);
        // 三重显示：底部动作条（每秒）+ 记分板（实时）+ 顶部大字（每 5 秒/最后 10 秒每秒）
        p.onScreenDisplay.setActionBar("§e未成年人保护 - 剩余游玩时间: " + formatTime(remain));
        setScoreboard(p, remain);
        if (remain <= 10 || remain % 5 === 0) {
          showTopTimer(p, remain);
        }
      }
    } else if (st === UNVERIFIED) {
      p.onScreenDisplay.setActionBar("§c未成年人保护 - 请先完成身份验证（使用验证令牌或输入 !verify）");
      // 每 15 秒强制弹出验证（防绕过）
      if (Math.floor(system.currentTick / (TICK_PER_SECOND * 15)) > 0 &&
          system.currentTick % (TICK_PER_SECOND * 15) === 0) {
        openVerification(p);
      }
    } else if (st === TIME_UP) {
      p.onScreenDisplay.setActionBar("§c未成年人保护 - 游玩时间已结束，物品栏已锁定");
      // 常驻大字循环：每 3 秒重显示一次"时间到"大标题，直到玩家重新验证
      if (system.currentTick % (TICK_PER_SECOND * 3) === 0) {
        timeUpBanner(p);
      }
    }
  }
}, TICK_PER_SECOND);

// ---------- 工具函数 ----------
function formatTime(totalSec) {
  const s = Math.max(0, totalSec);
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return m + ":" + String(sec).padStart(2, "0");
}

// 启动日志
world.sendMessage("§e[未成年人保护] 模组已加载：年龄限制游戏时长，多结局版（二次开发 ET，MIT）");
