# Auton 自主行为决策与任务执行引擎

[![Java](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **Auton（Autonomous + Decision）是一个面向自主系统的双模式行为决策与任务执行引擎，专为无人车、机器人、智能 Agent 等长期运行实体设计。**
>
> **Auton 同时支持传统行为树的"状态驱动"模式和面向调度系统的"命令驱动"模式，二者共享同一套节点体系——切换模式只需替换引擎实现，行为树和节点逻辑无需修改。**

## 为什么需要 Auton

传统行为树框架的典型范式：环境变化 → tick 一次 → 遍历整棵树 → 返回结果。这对于游戏 AI 或简单机器人是够用的——决策和行动在同一个 tick 内完成。

面向真实世界的自主系统时，这套模型走不通：

> 让一辆车持续自主执行任务：接收调度中心的指令、执行导航、接收紧急中断、等待车辆反馈、恢复任务——整个过程可能持续数十分钟，跨越多次状态变化和外部反馈。

传统行为树的核心缺陷：

| 问题 | 传统行为树 | 你的真实需求 |
|------|-----------|-------------|
| **执行假设** | 一次 tick 完成所有动作 | 动作可能需要数秒到数十分钟，外部设备还在工作中 |
| **状态归属** | 状态散落在节点内部，外部不可感知 | 调度中心、监控系统需要随时读写车辆/机器人状态 |
| **任务调度** | 一棵树 = 一个行为，无调度概念 | 需要命令队列、优先级插队、取消指令、指令生命周期管理 |
| **异步反馈** | 无法处理"执行中等待反馈"的场景 | 下发指令给设备 → 设备工作中 → 反馈完成 → 继续下一步 |

Auton 就是为了解决这类问题设计的——它不假设"一次 tick 完成一切"，而是将决策和执行分离，通过 Actor 状态桥接外部世界的异步反馈，让行为树能够驱动持续数十分钟的长流程任务。

## 核心架构：双模式，可插拔

Auton 的核心架构同时承载两种执行模式。**任务执行是"可插拔"的**——同一组节点和同一套 Actor 状态，可以通过不同的引擎以不同方式驱动：

```
                           ┌─────────────────────┐
                           │   业务节点（复用）     │
                           │  Node / Action      │
                           │  Composite/Decorator│
                           └────────┬────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌───────────────────────────┐       ┌──────────────────────────┐
        │  StateBehaviorTreeEngine  │       │ CommandBehaviorTreeEngine│
        │      （状态驱动模式）        │       │     （命令驱动模式）       │
        │         [待实现]           │       │       [已实现]            │
        └──────────┬────────────────┘       └────────────┬─────────────┘
                   │                                     │
                   ▼                                     ▼
        ┌─────────────────────┐             ┌──────────────────────────┐
        │  Actor State 变化    │             │  Command 入队            │
        │  → 触发 tick        │              │  → 按 type 路由行为树     │
        │  → 遍历行为树        │              │  → 遍历行为树             │
        │  → 返回结果          │             │  → 异步等待反馈 / 出队     │
        └─────────────────────┘             └─────────────────────────┘
```

两种模式共享同一套基础设施：
- **节点体系**：Action、Condition、Composite、Decorator——完全复用
- **Actor 模型**：状态管理、持久化、锁隔离——完全复用
- **Blackboard**：参数传递与解耦——完全复用
- **TreeTracer**：可观测性——完全复用

唯一的区别在于**谁触发 tick**：
- 状态驱动模式：Actor 状态变化事件自动触发
- 命令驱动模式：Command 入队触发，异步反馈后继续+Actor 状态变化事件主动触发

### Status 可扩展设计

Auton 的 `Status` 不是枚举，是接口。内置四种标准状态：

| 状态 | 含义 |
|------|------|
| `SUCCESS` | 节点执行成功 |
| `FAILURE` | 节点执行失败 |
| `RUNNING` | 节点执行中，等待外部反馈 |
| `ABORT` | 执行条件不再满足，短路退出 |

通过 `Status` 接口的 `state()` 和 `sameStateAs()` 方法，业务层可以定义任意自定义状态（如 `WAITING_APPROVAL`、`TIMEOUT`），引擎的 `afterTick` 和节点间状态传播都基于接口比较，不依赖具体枚举值。

## 🔌 模式一：状态驱动引擎（待实现）

状态驱动模式对应传统行为树范式：**环境变化 → 自动触发 tick → 遍历行为树 → 做出决策**。

### 设计思路

```
ActorStateChanged
       │
       ▼
 StateBehaviorTreeEngine
       │
       ├── 1. 获取 Actor
       ├── 2. 获取 Actor 关联的行为树
       ├── 3. 构建 TickContext（无 Command，直接以当前 Actor 状态为上下文）
       ├── 4. rootNode.tick(context)
       └── 5. 返回执行结果
```

### 和传统行为树的区别

即使是"传统模式"，Auton 的实现也不同于普通行为树库：

- **有 Actor 承载状态**：节点仍然不持有状态，每轮 tick 从 Actor 读取。标准行为树库的状态通常散落在节点的成员变量里。
- **支持 RUNNING 中断**：遇到需要异步等待的动作，同样返回 RUNNING 暂停，等待外部 `updateActor` 触发下一轮 tick。标准行为树库通常不支持这种暂停-恢复。
- **ABORT 机制**：条件不满足时整个树短路退出。



## 🔌 模式二：命令驱动引擎（已实现）

命令驱动模式是 Auton 当前已实现的引擎，专为**外部调度系统 + 实体异步执行**的场景设计。

### 设计理念

决策和执行是两件事。Tick 只做决策——判断当前该干什么；真正的执行发生在外部系统（车辆、机器人、Agent），执行结果通过 `updateActor` 反馈。引擎在两者之间充当协调者：

```
外部调度系统                     Auton 引擎                    外部设备/车辆
     │                              │                              │
     ├─ addCommand ────────────────►│                              │
     │                              ├─ lock(actorId)              │
     │                              ├─ tick(actorId)              │
     │                              ├─ createMission ────────────►│
     │                              ├─ 返回 RUNNING               │
     │                              ├─ unlock(actorId)            │
     │                              │                              │
     │                              │  [设备工作中...]              │
     │                              │                              │
     │                    updateActor ◄────────────────────────────┤
     │                              │                              │
     │                              ├─ lock(actorId)              │
     │                              ├─ tick(actorId)              │
     │                              ├─ 检测到 task==RUNNING       │
     │                              ├─ createNavi ───────────────►│
     │                              ├─ 返回 RUNNING               │
     │                              ├─ unlock(actorId)            │
     │                              │                              │
     │                              │   ...持续直到 SUCCESS/FAILURE
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `CommandBehaviorTreeEngine` | 抽象引擎基类，管理命令队列、Actor 生命周期、tick 调度 |
| `Command` | 外部指令，携带 `type`（路由 key）、`actorId`、`missionId`、业务参数 |
| `CommandQueue` | 每个 Actor 独立的命令队列，支持 enqueue / insertFirst / removeByCmdId |
| `Actor` | 执行主体，持有业务状态和命令队列引用 |
| `ActorRepository` | Actor 持久化接口，支持 find / save（内置版本号乐观锁预留） |
| `Blackboard` | 黑板，Command 通过 `fillParams` 写入参数，节点通过 key 读取 |
| `TickContext` | 单次 tick 快照，包含 Command、Actor、Blackboard、TreeTracer |
| `TreeTracer` | 可观测性接口，回调每个节点的进入/离开 |

### Tick 生命周期

```
addCommand(cmd) / addCommandFirst(cmd) / updateActor(actorId, updater)
        │
        ▼
    lock(actorId)              ← 获取分布式锁
        │
        ▼
    tick(actorId)
        ├── peek 队首命令
        ├── buildContext       ← cmd.fillParams → blackboard → context
        ├── 路由到 cmd.type 对应的行为树根节点
        └── node.tick(context)
                ├── beforeTick
                ├── tracer.onNodeEnter
                ├── execute → 递归子节点
                ├── tracer.onNodeExit
                └── afterTick
        │
        ▼
    unlock(actorId)            ← 释放锁
        │
        ▼
    afterTick(status, context)
        ├── true  → removeByCmdId → 异步续 tick（重新获取锁）
        └── false → 停止，等待下次 addCommand / updateActor 唤醒
```

### 关键特性

**Command 与 Node 解耦**：Command 的 `fillParams(Blackboard)` 把业务参数写入黑板，节点只通过黑板 key 读取。同一棵行为树可响应任意 Command 类型——新增指令类型只需新增 Command 实现，行为树零修改。

**异步状态驱动**：节点返回 `RUNNING` 表示"命令未完成，等外部反馈"。引擎释放锁并停止 tick。后续当外部世界变化时，调用 `updateActor` 写入新状态并重新触发 tick。

**SUCCESS/FAILURE 均出队**：无论命令成功或失败，都从队列移除。用户通过 `afterTick` 决定业务逻辑（日志、告警、重试等），不阻塞队列。

**紧急插队**：`addCommandFirst` 将命令插入队首，下一轮 tick 优先执行。命令行完成后，原来的命令从队首恢复执行。

**分布式锁友好**：每次 tick 独立加锁/解锁，异步续 tick 时重新获取锁，锁不跨 tick 持有。

## 🚀 快速开始（命令驱动模式）

以当前测试代码的车辆任务调度为例：调度中心向车辆下发"创建任务并导航"命令，车辆异步执行并反馈状态，期间支持紧急中断和恢复。

### 步骤 1：定义 Actor 业务状态

```java
public class VehicleState {
    private String missionId;
    private String login;      // START / FINISH
    private String emergency;  // START / APPLY / RUNNING / FINISH
    private String task;       // START / APPLY / RUNNING / FINISH
    private String navigation; // START / APPLY / RUNNING / FINISH
}
```

### 步骤 2：编写节点

**条件节点** —— 从 Actor 状态做判断：

```java
public class LoginCondition extends Condition<VehicleState> {
    @Override
    protected boolean doCheck(TickContext<VehicleState> context) {
        return State.FINISH.equalsValue(context.actor().state().getLogin());
    }
}
```

**动作节点** —— 从 Blackboard 读参数，与 Command 类型解耦：

```java
public class CreateMissionAction extends AbstractExecByMissionAction {
    @Override
    protected Actor<VehicleState> execService(TickContext<VehicleState> context) {
        CreateTaskParam param = context.blackboard().get("createTaskParam");
        vehicleService.createMission(param);
        Actor<VehicleState> actor = context.actor();
        actor.state().setTask(State.APPLY.getValue());
        return actor;
    }
}
```

`AbstractExecByMissionAction` 封装了通用状态机模板（幂等判断、状态流转），子类只需覆写 `getState` 和 `execService`。

### 步骤 3：YAML 声明行为树结构

```yaml
id: root
name: 创建并导航树
node: sequence
children:
  - id: c1
    name: 已登录检查
    node: loginCondition
  - id: c2
    name: 未急停检查
    node: noEmergencyCondition
  - id: c3
    name: 任务流程
    node: selector
    children:
      - id: c3.1
        name: 创建任务
        node: createMissionAction
      - id: c3.2
        name: 取消任务
        node: cancelMissionAction
  - id: c4
    name: 创建导航
    node: createNaviAction
```

```java
Node<VehicleState> tree = new TreeBuilder<VehicleState>()
    .loader(loader -> loader.load(yamlString))
    .nodeRegistry(registry -> registry.scanPackages("org.cactus.auton.node"))
    .build();
```

### 步骤 4：实现引擎

```java
public class VehicleBehaviorEngine extends CommandBehaviorTreeEngine<VehicleState> {

    @Override
    protected boolean afterTick(Status status, TickContext<VehicleState> ctx) {
        return Status.BuiltIn.SUCCESS.sameStateAs(status)
            || Status.BuiltIn.FAILURE.sameStateAs(status);
    }

    @Override
    protected Lock getLock(String actorId) {
        return lockMap.computeIfAbsent(actorId, k -> new ReentrantLock());
    }

    @Override
    protected Actor<VehicleState> createDefaultActor(String actorId) {
        // 新 Actor 默认状态初始化
    }
}
```

### 步骤 5：组装并运行

```java
Map<String, Node<VehicleState>> treeMap = new HashMap<>();
treeMap.put("CREATE_TASK_AND_Navi", createTaskTree);
treeMap.put("EMERGENCY_CMD", new EmergencyAction(...));

engine = new VehicleBehaviorEngine(treeMap, new MapActorRepository(), new MapCommandQueue());

// 下发命令 — 引擎自动 tick
engine.addCommand(new CreateTaskAndNaviCmd("123", "A501", "123456789"));

// 车辆异步反馈任务接收 — 引擎自动继续 tick
engine.updateActor("A501", actor -> {
    actor.state().setTask(State.RUNNING.getValue());
    return actor;
});

// 紧急中断插队 — 下一轮 tick 优先执行
engine.addCommandFirst(new EmergencyCmd("111", "A501"));

// 反馈紧急完成 — 引擎继续执行原命令
engine.updateActor("A501", actor -> {
    actor.state().setEmergency(State.FINISH.getValue());
    return actor;
});
```

测试代码完整覆盖了以下流程：车辆登录 → 创建任务 → 异步反馈任务接收 → 创建导航 → 紧急中断 → 紧急完成 → 恢复导航 → 全部完成。

## 🔧 环境要求

- **Java 版本**：JDK 8+
- **构建工具**：Maven 3.6+
- **框架依赖**：仅依赖 SnakeYAML（YAML 解析），无 Spring 等重型框架

## 📊 性能特性

- **无反射执行路径**：节点执行链路为纯方法调用，无代理、无反射
- **Actor 级并行**：不同 Actor 之间完全并行，仅同 Actor 内串行加锁
- **零空闲 CPU**：无后台轮询，引擎在无触发时完全空闲
- **无框架开销**：无容器启动延迟，纯 Java 对象初始化


## 🆘 支持

- **文档**：查看源码注释和 `TreeTest` 测试用例
- **示例场景**：车辆任务调度完整流程（命令下发 → 异步反馈 → 紧急插队 → 状态恢复）
- **问题反馈**：通过 GitHub Issues 提交问题

## 📝 更新记录

### v1.0 — 初始版本

- 完整行为树节点体系（Composite/Decorator/Leaf）及无状态 Tick 执行模型
- `CommandBehaviorTreeEngine` 命令驱动引擎：命令队列、Actor 状态管理、分布式锁、异步续 tick
- `BehaviorTreeEngine` 状态驱动模式架构预留
- 可扩展 `Status` 接口，内置 SUCCESS/FAILURE/RUNNING/ABORT 四种状态
- YAML 行为树声明式构建 + 包扫描自动注册节点
- Blackboard 模式实现 Command 与 Node 类型解耦
- `TreeTracer` 可观测性接口
- 车辆任务调度完整测试用例
