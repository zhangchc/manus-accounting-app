# 做T管理（网格交易）技术设计方案

## 1. 做T管理规则表（t_stock_config）

```sql
CREATE TABLE t_stock_config (
    id              BIGINT        NOT NULL COMMENT '主键ID',
    stock_code      VARCHAR(10)   NOT NULL COMMENT '股票代码',
    stock_name      VARCHAR(50)   NOT NULL COMMENT '股票名称',
    base_price      DECIMAL(10,2) NOT NULL COMMENT '基准价（网格中枢）',
    levels          INT           NOT NULL DEFAULT 5 COMMENT '档位数（买入/卖出各N档）',
    up_pct          DECIMAL(5,2)  NOT NULL DEFAULT 5.00 COMMENT '卖出每档涨幅 %',
    down_pct        DECIMAL(5,2)  NOT NULL DEFAULT 5.00 COMMENT '买入每档跌幅 %',
    fixed_shares    INT           NOT NULL COMMENT '每档操作固定股数',
    active          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_code (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='做T管理规则表';
```

> **唯一约束**：`stock_code` 唯一，一只股票只能有一条做T规则。保存时如已存在则自动覆盖旧规则。

## 2. 做T操作表（t_stock_operation）

```sql
CREATE TABLE t_stock_operation (
    id              BIGINT        NOT NULL COMMENT '主键ID',
    config_id       BIGINT        NOT NULL COMMENT '关联规则表 t_stock_config.id',
    stock_code      VARCHAR(10)   NOT NULL COMMENT '股票代码（冗余，方便查询）',
    level_no        INT           NOT NULL COMMENT '档位编号 1,2,3...',
    direction       TINYINT       NOT NULL COMMENT '买卖方向 1-买入 2-卖出',
    level_price     DECIMAL(10,2) NOT NULL COMMENT '档位价格（规则生成时计算）',
    triggered       TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已触发 0-未触发 1-已触发',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_config_id (config_id),
    KEY idx_stock_code (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='做T操作表（网格档位快照）';
```

> **档位生成规则**（规则创建/修改时重建所有档位行）：
> - 卖出档位（direction=2）：`level_price = base_price × (1 + up_pct%) ^ level_no`
> - 买入档位（direction=1）：`level_price = base_price × (1 − down_pct%) ^ level_no`
> - 示例：基准价 ¥100、5档、涨幅 5%、跌幅 5%
>   - 卖出 1~5 档：¥105.00 → ¥110.25 → ¥115.76 → ¥121.55 → ¥127.63
>   - 买入 1~5 档：¥95.00  → ¥90.25  → ¥85.74  → ¥81.45  → ¥77.38
>
> **触发标记**：当用户在某个档位执行了实际买入/卖出后，将 `triggered` 置为 1。UI 据此判断该档位是否已触发，显示"已买/已卖"或"卖出/买入"按钮。
>
> **重建策略**：规则表的 `base_price`、`levels`、`up_pct`、`down_pct` 任一变更时，需要 `DELETE` 该规则下所有旧档位行，重新生成并 `INSERT`。已触发的历史档位不保留，因为价格已变化，旧档位不再有意义。

## 3. 做T流水表（t_stock_trade）

```sql
CREATE TABLE t_stock_trade (
    id              BIGINT        NOT NULL COMMENT '主键ID',
    config_id       BIGINT        NOT NULL COMMENT '关联规则表 t_stock_config.id',
    operation_id    BIGINT        DEFAULT NULL COMMENT '关联档位表 t_stock_operation.id（满档强制卖出时为空）',
    stock_code      VARCHAR(10)   NOT NULL COMMENT '股票代码（冗余，方便查询）',
    direction       TINYINT       NOT NULL COMMENT '买卖方向 1-买入 2-卖出',
    shares          INT           NOT NULL COMMENT '操作股数',
    price           DECIMAL(10,2) NOT NULL COMMENT '成交价（元）',
    reason          VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '买卖理由',
    trade_time      DATETIME      NOT NULL COMMENT '交易时间',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_config_id (config_id),
    KEY idx_stock_code (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='做T流水表';
```

> **不存配对盈亏**：配对盈亏 = `(卖价 − 买价) × 股数`，依赖 FIFO 顺序。如果存库，中间插入/删除一条记录会导致后续所有配对盈亏失效，带来数据一致性问题。查询时实时按 FIFO 计算即可。
>
> **direction 冗余**：虽然通过 `operation_id` 可以 join 到操作表获取方向，但流水表自身冗余 `direction` 可避免不必要的关联查询，统计买卖笔数时更高效。

## 4. 表关系

```
t_stock_config (规则表，每只股票一条)
    ├── 1:N → t_stock_operation (操作表，每规则 2×levels 条，买入N档 + 卖出N档)
    └── 1:N → t_stock_trade (流水表，每次交易一条，关联到具体档位)

t_stock_trade.operation_id → t_stock_operation.id
```

## 5. 配对盈亏计算（FIFO）

不存库，查询流水表时按以下逻辑实时计算：

```
1. 取该股票所有流水，按 id（即创建顺序）升序排列
2. 将买入和卖出分别提取为两个队列
3. 按顺序配对：第1次买入 配 第1次卖出，第2次买入 配 第2次卖出...
4. 配对盈亏 = (卖出价 − 买入价) × min(买入股数, 卖出股数)
5. 未配对的交易（买入多于卖出或反之）显示"待匹配"
```

## 6. 后端接口设计

### 6.1 查询T配置列表

```
GET /manage/stock/trade/config/list
```

返回每条规则及其档位状态汇总（总盈亏、买卖次数由流水表实时计算）。

### 6.2 根据股票代码查询T配置

```
GET /manage/stock/trade/config/query?stockCode=600519
```

> 用于新增页面的股票下拉联动——选中股票后，前端调用此接口查询该股票是否已有规则配置。
> - 存在：返回规则详情，前端自动回填表单（基准价、档位数、涨跌幅、股数等），用户可直接修改
> - 不存在：返回 null，表单保持空白

返回示例（存在）：

```json
{
  "id": 1,
  "stockCode": "600519",
  "stockName": "贵州茅台",
  "basePrice": 1720.00,
  "levels": 5,
  "upPct": 5.00,
  "downPct": 5.00,
  "fixedShares": 100,
  "active": 1
}
```

### 6.3 保存T配置（新增 / 修改统一入口）

```
POST /manage/stock/trade/config/save
```

请求体：

```json
{
  "stockCode": "600519",
  "stockName": "贵州茅台",
  "basePrice": 1720.00,
  "levels": 5,
  "upPct": 5.00,
  "downPct": 5.00,
  "fixedShares": 100,
  "active": 1
}
```

> **保存逻辑**：后端根据 `stock_code` 判断：
> - **不存在**：直接新增规则 + 生成档位行写入 `t_stock_operation`
> - **已存在**：删除旧规则（物理删）及其关联的所有 `t_stock_operation` 行，再插入新规则 + 重新生成档位行
>
> 此操作为事务操作，确保规则表和操作表数据一致。流水表（`t_stock_trade`）保留不受影响，因为 `config_id` 关联的是新规则 id。

### 6.4 新增页面完整交互流程

```
1. 用户点击"新增" → 打开弹窗，表单为空
2. 用户在"选择股票"下拉框中选择一只股票（如 贵州茅台 600519）
3. 前端调用 GET /manage/stock/trade/config/query?stockCode=600519
4. 后端返回：
   - 存在规则 → 前端自动回填表单（基准价、档位数、涨跌幅、股数、状态）
   - 不存在   → 表单保持空白，用户自行填写
5. 用户修改/确认表单后点击"确定"
6. 前端调用 POST /manage/stock/trade/config/save
7. 后端判断 stock_code 是否已有规则：
   - 有 → 删旧规则 + 删旧档位 → 插新规则 + 生成新档位
   - 无 → 插新规则 + 生成新档位
8. 保存成功，关闭弹窗，刷新左侧股票列表
```

### 6.4 查询档位列表（含触发状态）

```
GET /manage/stock/trade/operation/list?configId={configId}
```

返回该规则下所有买入/卖出档位，UI 用于渲染网格视图。

### 6.5 新增交易记录

```
POST /manage/stock/trade/record/save
```

请求体：

```json
{
  "configId": 1,
  "operationId": 10,
  "direction": 2,
  "shares": 100,
  "price": 1806.00,
  "reason": "触及一档止盈位，获利了结",
  "tradeTime": "2026-07-10 10:23"
}
```

> 事务操作：插入流水 + 将对应 `t_stock_operation.triggered` 置为 1。

### 6.6 查询交易流水（含配对盈亏）

```
GET /manage/stock/trade/record/list?configId={configId}
```

> 返回流水列表，配对盈亏在服务层实时 FIFO 计算后填充到 VO 中返回。

### 接口汇总

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | /manage/stock/trade/config/list | 查询T配置列表 |
| 2 | GET | /manage/stock/trade/config/query | 根据股票代码查询T配置 |
| 3 | POST | /manage/stock/trade/config/save | 保存T配置（新增 / 修改） |
| 4 | GET | /manage/stock/trade/operation/list | 查询档位列表 |
| 5 | POST | /manage/stock/trade/record/save | 新增交易记录 |
| 6 | GET | /manage/stock/trade/record/list | 查询交易流水 |

## 7. 满档判断逻辑

满档 = 该股票的**卖出已触发次数 >= 设定的档位数**。

查询 `t_stock_operation` 中 `config_id = ? AND direction = 2 AND triggered = 1` 的 count，与 `t_stock_config.levels` 比较。前端卖出按钮点击时，如果已满档则弹窗警告，允许强制继续卖出（此时不关联任何档位，`operation_id` 可传 null 或额外标记）。

## 8. 注意事项

1. **配对盈亏不存库**：FIFO 顺序敏感，必须实时计算。
2. **满档后仍可交易**：满档仅做警告提示，不阻止用户继续卖出（此时新增流水不关联档位，`operation_id` 可置为 null）。
3. **规则表 stock_code 唯一**：一只股票只能有一条做T规则，防止重复配置。
