# 股票持仓 & 做T管理技术设计方案

## 1. 股票持仓表（stock_position）

```sql
CREATE TABLE stock_position (
    id              BIGINT        NOT NULL COMMENT '主键ID',
    stock_code      VARCHAR(10)   NOT NULL COMMENT '股票代码，如 600519',
    stock_name      VARCHAR(50)   NOT NULL COMMENT '股票名称',
    shares          INT           NOT NULL DEFAULT 0 COMMENT '持仓股数',
    cost_price      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成本价（元）',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_code (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票持仓表';
```

> **不存现价**：现价属于实时行情数据，通过行情接口实时获取，市值和盈亏在服务层计算。
> **不存市值/盈亏**：均为计算派生值，不存冗余字段。

## 2. 持仓概览指标计算方案

现价通过行情接口实时获取，不落库。以下 5 个指标均在服务层实时计算：

| 指标 | 公式 | 说明 |
|------|------|------|
| **总成本** | Σ(成本价 × 持仓股数) | 从 `stock_position` 表汇总 |
| **总市值** | Σ(现价 × 持仓股数) | 现价来自行情接口 |
| **总盈亏** | 总市值 − 总成本 | 正数盈利，负数亏损 |
| **收益率** | (总盈亏 ÷ 总成本) × 100% | 总成本为 0 时返回 0 |
| **总仓位** | (总市值 ÷ 总成本) × 100% | 反映持仓市值相对于成本的比例 |

### 计算流程

```
1. 查询 stock_position 全表（deleted = 0），得到 List<StockPosition>
2. 提取所有 stock_code，批量调用行情接口获取 Map<stockCode, currentPrice>
3. 遍历持仓列表，逐条匹配现价，累加计算：
   - 每只股票：市值 = shares × currentPrice，盈亏 = (currentPrice − costPrice) × shares
   - 汇总即可得到总市值、总成本、总盈亏、收益率、总仓位
```

### 后端接口

#### 4.1 汇总查询

```
GET /manage/stock/position/summary
```

返回 VO：

```json
{
  "totalCost": 16800.00,
  "totalMarketValue": 17523.00,
  "totalProfit": 723.00,
  "totalProfitRate": 4.30,
  "positionRate": 104.30
}
```

> 调用行情接口获取所有持仓股票的现价后，实时计算汇总指标并返回。

#### 4.2 股票新增

```
POST /manage/stock/position/add
```

请求 DTO：

```json
{
  "stockCode": "600519",
  "stockName": "贵州茅台",
  "shares": 10,
  "costPrice": 1680.00
}
```

> `stock_code` 唯一约束，重复新增时返回"该股票已存在，请使用修改功能"。

#### 4.3 股票查询

```
GET /manage/stock/position/list?stockCode=600519&stockName=茅台&page=1&pageSize=10
```

| 参数 | 必填 | 说明 |
|------|------|------|
| stockCode | 否 | 股票代码，模糊匹配 |
| stockName | 否 | 股票名称，模糊匹配 |
| page | 否 | 页码，默认 1 |
| pageSize | 否 | 每页条数，默认 10 |

返回 VO：

```json
{
  "total": 1,
  "records": [
    {
      "id": 1610000000000000001,
      "stockCode": "600519",
      "stockName": "贵州茅台",
      "shares": 10,
      "costPrice": 1680.00,
      "currentPrice": 1752.30,
      "marketValue": 17523.00,
      "profit": 723.00,
      "profitRate": 4.30
    }
  ]
}
```

> `currentPrice`、`marketValue`、`profit`、`profitRate` 实时计算不落库。

#### 4.4 修改 / 删除（公用接口）

```
POST /manage/stock/position/save
```

请求 DTO：

```json
{
  "id": 1610000000000000001,
  "stockName": "贵州茅台",
  "shares": 20,
  "costPrice": 1700.00,
  "deleted": 0
}
```

| 字段 | 说明 |
|------|------|
| id | 必填，目标持仓 ID |
| stockName | 修改时传，删除时不传 |
| shares | 修改时传 |
| costPrice | 修改时传 |
| deleted | 0=修改，1=逻辑删除 |

> 通过 `deleted` 字段区分操作：
> - `deleted = 0`：更新 stockName / shares / costPrice
> - `deleted = 1`：逻辑删除该持仓记录

### 接口汇总

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | /manage/stock/position/summary | 汇总查询 |
| 2 | POST | /manage/stock/position/add | 新增 |
| 3 | GET | /manage/stock/position/list | 查询列表 |
| 4 | POST | /manage/stock/position/update | 修改 / 删除 |

## 3. 实时行情接口

### 接口选型

采用腾讯股票行情接口，免费、无需 API Key。

### 请求方式

```
GET https://qt.gtimg.cn/q={prefixedCode}[,{prefixedCode2},...]
```

- 编码：GBK
- 单只查询：`https://qt.gtimg.cn/q=sz300255`
- 批量查询（逗号分隔，每只一行返回）：`https://qt.gtimg.cn/q=sh600519,sz000858,sz300750`
- **`stock_code` 前缀映射**：调用行情接口前，根据库中存储的无前缀代码，按规则自动补前缀：

| 代码段 | 交易所 | 前缀 | 示例 |
|--------|--------|------|------|
| 600 / 601 / 603 / 605 | 上海 | `sh` | 600519 → sh600519 |
| 000 / 001 / 002 / 003 / 300 / 301 | 深圳 | `sz` | 300750 → sz300750 |

```java
/**
 * 根据股票代码自动补充交易所前缀
 * 上海交易所: 600/601/603/605 开头 → sh
 * 深圳交易所: 000/001/002/003/300/301 开头 → sz
 */
private String addExchangePrefix(String stockCode) {
    if (stockCode.matches("^6(00|01|03|05).*")) {
        return "sh" + stockCode;
    }
    if (stockCode.matches("^(000|001|002|003|300|301).*")) {
        return "sz" + stockCode;
    }
    throw new RuntimeException("不支持的股票代码: " + stockCode);
}

### 响应格式

每只股票返回一行，以 `~` 分隔字段：

```
v_sz300255="51~常山药业~300255~8.56~8.45~8.55~90123~45678~..."
```

### 关键字段映射

| 索引 | 字段 | 说明 |
|------|------|------|
| parts[1] | 股票名称 | 用于校验 |
| parts[2] | 股票代码 | 不带前缀 |
| parts[3] | 最新价 | `currentPrice`，BigDecimal |
| parts[4] | 昨收价 | `prevClose`，用于计算当日涨跌 |
| parts[31] | 涨跌额 | 最新价 − 昨收价 |
| parts[32] | 涨跌幅 | 百分比字符串，如 "+1.23" |

### Java 调用示例

```java
private Map<String, StockQuote> fetchStockPrices(List<String> codes) {
    String url = "https://qt.gtimg.cn/q=" + String.join(",", codes);
    String response = HttpUtil.createGet(url).charset("GBK").execute().body();

    if (StrUtil.isBlank(response)) {
        throw new RuntimeException("获取股票行情失败");
    }

    Map<String, StockQuote> result = new HashMap<>();
    for (String line : response.split("\n")) {
        if (StrUtil.isBlank(line)) continue;
        String[] parts = line.split("~");
        if (parts.length < 33) continue;

        StockQuote quote = new StockQuote();
        quote.setStockCode(parts[2]);
        quote.setStockName(parts[1]);
        quote.setCurrentPrice(new BigDecimal(parts[3]));
        quote.setPrevClose(new BigDecimal(parts[4]));
        quote.setChange(new BigDecimal(parts[31]));
        quote.setChangePercent(parts[32]);
        result.put(parts[2], quote);
    }
    return result;
}
```

### 调用时机

- 汇总查询（`/summary`）和列表查询（`/list`）时，后端内部批量调用行情接口，一次性获取所有持仓的现价，实时计算各指标并返回
- 新增和修改接口不调用行情接口，仅做数据持久化


