# 食品溯源系统 - 后端 API 接口文档

**版本**: v1.0.0
**更新日期**: 2026-04-25
**基础路径**: `/api`

---

## 目录

1. [管理员认证模块](#1-管理员认证模块)
2. [产品管理模块](#2-产品管理模块)
3. [原材料管理模块](#3-原材料管理模块)
4. [生产批次管理模块](#4-生产批次管理模块)
5. [防伪码管理模块](#5-防伪码管理模块)
6. [溯源查询模块](#6-溯源查询模块)
7. [投诉管理模块](#7-投诉管理模块)
8. [数据导入模块](#8-数据导入模块)

---

## 通用说明

### 请求格式
- Content-Type: `application/json`
- 字符编码: `UTF-8`

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 通用状态码
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/登录失败 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 全局异常响应格式
```json
{
  "timestamp": "2026-04-25T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "详细错误信息",
  "errors": { "fieldName": "错误详情" }
}
```

---

## 1. 管理员认证模块

### 1.1 存储验证码

存储图形验证码，用于登录验证。

**请求**
```http
POST /api/captcha
Content-Type: application/json

{
  "username": "admin",
  "captcha": "ABCDE"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 管理员用户名 |
| captcha | string | 是 | 5位图形验证码 |

**响应**
```
HTTP/1.1 200 OK
```

---

### 1.2 管理员登录

**请求**
```http
POST /api/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123",
  "captcha": "ABCDE"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| username | string | 是 | 用户名 | 3-50字符 |
| password | string | 是 | 密码 | 6-20字符 |
| captcha | string | 是 | 验证码 | 5位 |

**成功响应**
```json
{
  "username": "admin",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**失败响应**
```json
"账号或密码错误"
```

**错误码**
| 错误信息 | 说明 |
|----------|------|
| 验证码不能为空 | 未填写验证码 |
| 验证码错误 | 验证码不正确 |
| 账号已被锁定，请 X 分钟后再试 | 连续登录失败5次后锁定 |
| 账号或密码错误 | 用户名或密码错误 |

---

### 1.3 管理员注册

注册新管理员（需要当前管理员验证）。

**请求**
```http
POST /api/admin/register
Content-Type: application/json

{
  "username": "newadmin",
  "password": "NewAdmin@123",
  "currentPassword": "Admin@123",
  "currentAdminUsername": "admin"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| username | string | 是 | 新管理员用户名 | 4-20位字母或数字组合 |
| password | string | 是 | 新管理员密码 | 至少8位，包含字母、数字和特殊字符 |
| currentPassword | string | 是 | 当前管理员密码 | 用于身份验证 |
| currentAdminUsername | string | 是 | 当前管理员用户名 | - |

**密码规则**
- 至少8位
- 必须包含字母、数字和特殊字符
- 特殊字符范围: `!@#$%^&*()_+-=[]{}|;':",./<>?`

**成功响应**
```json
{
  "message": "管理员创建成功",
  "username": "newadmin"
}
```

**失败响应**
```json
"管理员已存在"
```

---

## 2. 产品管理模块

### 2.1 创建产品

**请求**
```http
POST /api/products
Content-Type: application/json

{
  "name": "有机大米",
  "specification": "5kg/袋",
  "shelfLife": "12个月",
  "imageUrl": "https://example.com/rice.jpg",
  "contactPhone": "400-888-9999",
  "contactEmail": "contact@example.com"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 是 | 产品名称 |
| specification | string | 否 | 规格 |
| shelfLife | string | 否 | 保质期 |
| imageUrl | string | 否 | 产品图片URL |
| contactPhone | string | 否 | 联系电话 |
| contactEmail | string | 否 | 联系邮箱 |

**成功响应**
```json
{
  "id": 1,
  "name": "有机大米",
  "specification": "5kg/袋",
  "shelfLife": "12个月",
  "imageUrl": "https://example.com/rice.jpg",
  "contactPhone": "400-888-9999",
  "contactEmail": "contact@example.com",
  "qrCodeUrl": null,
  "antiFakeCode": null,
  "isDeleted": false
}
```

**失败响应**
```json
{
  "error": "产品名称不能为空"
}
```

---

### 2.2 更新产品

**请求**
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "有机大米（升级版）",
  "specification": "6kg/袋",
  "shelfLife": "18个月"
}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 产品ID |

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 否 | 产品名称 |
| specification | string | 否 | 规格 |
| shelfLife | string | 否 | 保质期 |
| imageUrl | string | 否 | 产品图片URL |
| contactPhone | string | 否 | 联系电话 |
| contactEmail | string | 否 | 联系邮箱 |
| qrCodeUrl | string | 否 | 二维码URL |

**成功响应**
```json
{
  "id": 1,
  "name": "有机大米（升级版）",
  ...
}
```

---

### 2.3 删除产品

级联删除产品及其所有关联数据（批次、防伪码、检验、仓储、运输等）。

**请求**
```http
DELETE /api/products/{id}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 产品ID |

**成功响应**
```json
{
  "success": true,
  "message": "删除成功"
}
```

**失败响应**
```json
{
  "error": "产品不存在"
}
```

---

### 2.4 查询产品列表

**请求**
```http
GET /api/products
GET /api/products?keyword=大米
```

**查询参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | string | 否 | 搜索关键词（按产品名称模糊匹配） |

**成功响应**
```json
[
  {
    "id": 1,
    "name": "有机大米",
    "specification": "5kg/袋",
    "shelfLife": "12个月",
    ...
  }
]
```

---

### 2.5 查询单个产品

**请求**
```http
GET /api/products/{id}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 产品ID |

**成功响应**
```json
{
  "id": 1,
  "name": "有机大米",
  ...
}
```

---

### 2.6 选择产品（下拉列表）

**请求**
```http
GET /api/products/select
GET /api/products/select?keyword=大米
GET /api/products/select?role=consumer
```

**查询参数**
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| keyword | string | 否 | 搜索关键词 | - |
| role | string | 否 | 角色 | consumer |

---

## 3. 原材料管理模块

### 3.1 创建原材料采购记录

**请求**
```http
POST /api/materials
Content-Type: application/json

{
  "productId": 1,
  "materialName": "有机稻谷",
  "batchNumber": "M20260425001",
  "supplierName": "东北农场",
  "producerName": "黑龙江农业公司",
  "producerAddress": "黑龙江省哈尔滨市",
  "quantity": 1000.0,
  "unit": "kg"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | long | 是 | 关联的产品ID |
| materialName | string | 是 | 原材料名称 |
| batchNumber | string | 是 | 原材料批次号 |
| supplierName | string | 否 | 供应商名称 |
| producerName | string | 否 | 生产商名称 |
| producerAddress | string | 否 | 生产商地址 |
| quantity | double | 否 | 采购数量 |
| unit | string | 否 | 数量单位 |

**成功响应**
```json
{
  "id": 1,
  "productId": 1,
  "materialName": "有机稻谷",
  "batchNumber": "M20260425001",
  "supplierName": "东北农场",
  "producerName": "黑龙江农业公司",
  "producerAddress": "黑龙江省哈尔滨市",
  "quantity": 1000.0,
  "unit": "kg",
  "isDeleted": false
}
```

---

### 3.2 更新原材料采购记录

**请求**
```http
PUT /api/materials/{id}
Content-Type: application/json

{
  "materialName": "有机稻谷（精选）",
  "quantity": 1500.0,
  "unit": "kg"
}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 原材料记录ID |

**成功响应**
```json
{
  "id": 1,
  "materialName": "有机稻谷（精选）",
  "quantity": 1500.0,
  ...
}
```

---

### 3.3 删除原材料采购记录

软删除，将 isDeleted 标记为 true。

**请求**
```http
DELETE /api/materials/{id}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 原材料记录ID |

**成功响应**
```json
{
  "success": true,
  "message": "删除成功"
}
```

---

### 3.4 查询原材料列表

**请求**
```http
GET /api/materials
GET /api/materials?productId=1
```

**查询参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | long | 否 | 按产品ID筛选 |

---

## 4. 生产批次管理模块

### 4.1 创建生产批次

**请求**
```http
POST /api/batches
Content-Type: application/json

{
  "productId": 1,
  "productionDate": "2026-04-25",
  "shelfLife": "12个月",
  "quantity": 5000.0,
  "unit": "kg",
  "materialIds": [1, 2, 3],
  "storage": {
    "storageTime": "2026-04-25T10:00:00",
    "warehouseLocation": "A仓库-01区",
    "quantity": 5000.0,
    "unit": "kg"
  },
  "transportSale": {
    "time": "2026-04-26T08:00:00",
    "transportCompany": "顺丰物流",
    "vehicleNumber": "黑A12345",
    "receiverName": "张三",
    "receiverContact": "13800138000",
    "salesRegion": "北京市朝阳区",
    "environmentTemperature": 20.0,
    "productTemperature": 18.0
  }
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | long | 是 | 关联的产品ID |
| productionDate | date | 是 | 生产日期 |
| shelfLife | string | 否 | 保质期 |
| quantity | double | 否 | 数量 |
| unit | string | 否 | 单位 |
| materialIds | array | 是 | 关联的原材料ID列表（至少选择一个） |
| storage | object | 否 | 仓储信息 |
| transportSale | object | 否 | 运输销售信息 |

**仓储信息 (storage)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| storageTime | datetime | 否 | 入库时间 |
| warehouseLocation | string | 否 | 仓库位置 |
| quantity | double | 否 | 库存数量 |
| unit | string | 否 | 单位 |

**运输销售信息 (transportSale)**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| time | datetime | 否 | 运输时间 |
| transportCompany | string | 否 | 运输公司 |
| vehicleNumber | string | 否 | 车牌号 |
| receiverName | string | 否 | 收货人姓名 |
| receiverContact | string | 否 | 收货人联系方式 |
| salesRegion | string | 否 | 销售区域 |
| environmentTemperature | double | 否 | 环境温度(℃) |
| productTemperature | double | 否 | 产品温度(℃) |

**成功响应**
```json
{
  "id": 1,
  "batchNumber": "B202604250001",
  "productId": 1,
  "productName": "有机大米",
  "productionDate": "2026-04-25",
  "shelfLife": "12个月",
  "quantity": 5000.0,
  "unit": "kg",
  "createdAt": "2026-04-25T10:30:00",
  "storageId": 1,
  "transportSaleId": 1
}
```

**失败响应**
```json
{
  "error": "至少选择一个原料批次"
}
```

---

### 4.2 查询生产批次列表

**请求**
```http
GET /api/batches
GET /api/batches?productId=1
```

**查询参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | long | 否 | 按产品ID筛选 |

---

### 4.3 查询单个生产批次

**请求**
```http
GET /api/batches/{id}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 批次ID |

---

### 4.4 按批次号查询

**请求**
```http
GET /api/batches/by-number/{batchNumber}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| batchNumber | string | 批次号，如 B202604250001 |

---

### 4.5 添加检验信息

**请求**
```http
POST /api/batches/{id}/inspection
Content-Type: application/json

{
  "sampleName": "大米样品A",
  "sampleQuantity": 500,
  "sampleSpecification": "符合GB/T标准",
  "imageUrl": "https://example.com/inspection.jpg"
}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 批次ID |

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sampleName | string | 是 | 样品名称 |
| sampleQuantity | int | 否 | 样品数量 |
| sampleSpecification | string | 否 | 样品规格 |
| imageUrl | string | 否 | 检验报告图片URL |

---

### 4.6 添加仓储信息

**请求**
```http
POST /api/batches/{id}/storage
Content-Type: application/json

{
  "storageTime": "2026-04-25T10:00:00",
  "outboundTime": "2026-05-01T09:00:00",
  "warehouseLocation": "A仓库-01区",
  "quantity": 5000.0,
  "unit": "kg"
}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 批次ID |

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| storageTime | datetime | 否 | 入库时间 |
| outboundTime | datetime | 否 | 出库时间 |
| warehouseLocation | string | 否 | 仓库位置 |
| quantity | double | 否 | 库存数量 |
| unit | string | 否 | 单位 |

---

### 4.7 添加运输销售信息

**请求**
```http
POST /api/batches/{id}/transport-sale
Content-Type: application/json

{
  "time": "2026-04-26T08:00:00",
  "transportCompany": "顺丰物流",
  "vehicleNumber": "黑A12345",
  "receiverName": "张三",
  "receiverContact": "13800138000",
  "salesRegion": "北京市朝阳区",
  "environmentTemperature": 20.0,
  "productTemperature": 18.0
}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 批次ID |

---

## 5. 防伪码管理模块

### 5.1 生成防伪码

为指定批次生成防伪码，同时自动将第一个防伪码分配给产品。

**请求**
```http
POST /api/batches/{id}/security-codes
Content-Type: application/json

{
  "quantity": 100
}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 批次ID |

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quantity | int | 是 | 生成数量 |

**成功响应**
```json
{
  "codes": [
    "SC1744...xxx1",
    "SC1744...xxx2",
    ...
  ],
  "count": 100
}
```

---

### 5.2 查询批次的防伪码列表

**请求**
```http
GET /api/batches/{id}/security-codes
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 批次ID |

**成功响应**
```json
[
  {
    "id": 1,
    "code": "SC1744...xxx1",
    "batchId": 1,
    "batchNumber": "B202604250001",
    "status": "未激活",
    "firstScanTime": null,
    "scanCount": 0,
    "createdAt": "2026-04-25T10:30:00"
  }
]
```

---

### 5.3 导出防伪码

**请求**
```http
GET /api/security-codes/export/{batchId}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| batchId | long | 批次ID |

---

## 6. 溯源查询模块

### 6.1 防伪码验证查询

消费者扫描产品二维码后调用此接口验证产品真伪。

**请求**
```http
GET /api/verify?code=SC17445555551234567890ABCD
```

**查询参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| code | string | 是 | 防伪码 |

**首次验证成功响应**
```json
{
  "valid": true,
  "data": {
    "product": {
      "id": 1,
      "name": "有机大米",
      "specification": "5kg/袋",
      "shelfLife": "12个月",
      "imageUrl": "https://example.com/rice.jpg",
      "contactPhone": "400-888-9999",
      "contactEmail": "contact@example.com",
      "antiFakeCode": "SC17445555551234567890ABCD"
    },
    "batch": {
      "id": 1,
      "batchNumber": "B202604250001",
      "productionDate": "2026-04-25",
      "shelfLife": "12个月",
      "createdAt": "2026-04-25T10:30:00"
    },
    "materials": [
      {
        "materialName": "有机稻谷",
        "batchNumber": "M20260425001",
        "supplierName": "东北农场",
        "producerName": "黑龙江农业公司"
      }
    ],
    "inspection": {
      "sampleName": "大米样品A",
      "sampleQuantity": 500,
      "sampleSpecification": "符合GB/T标准",
      "imageUrl": "https://example.com/inspection.jpg"
    },
    "storage": {
      "storageTime": "2026-04-25T10:00:00",
      "outboundTime": "2026-05-01T09:00:00",
      "warehouseLocation": "A仓库-01区"
    },
    "transportSale": {
      "transportTime": "2026-04-26T08:00:00",
      "salesRegion": "北京市朝阳区"
    },
    "status": "已激活",
    "firstScanTime": "2026-04-25T14:30:00",
    "scanCount": 1,
    "isQueried": false,
    "queryTip": null
  }
}
```

**重复查询响应（可能是伪品）**
```json
{
  "valid": false,
  "message": "该产品已被查询过 3 次，首次查询时间：2026-04-25T14:30:00，该产品可能是伪品，请谨慎购买！"
}
```

**防伪码不存在响应**
```json
{
  "valid": false,
  "message": "未找到该防伪码对应的产品信息，该产品可能是伪品，请谨慎购买！"
}
```

---

### 6.2 按批次号追溯

**请求**
```http
GET /api/trace/batch/{batchNumber}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| batchNumber | string | 批次号，如 B202604250001 |

**成功响应**
```json
{
  "valid": true,
  "data": {
    "product": { ... },
    "batch": { ... },
    "materials": [ ... ],
    "inspection": { ... },
    "storage": { ... },
    "transportSale": { ... }
  }
}
```

**失败响应**
```json
{
  "valid": false,
  "message": "未找到该批次对应的产品信息"
}
```

---

### 6.3 获取产品详情（消费者端）

**请求**
```http
GET /api/product-detail?antiFakeCode=SC17445555551234567890ABCD
```

**查询参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| antiFakeCode | string | 是 | 防伪码 |

---

### 6.4 获取产品详情（管理员端）

管理员查询返回更完整的信息（如运输公司详细信息）。

**请求**
```http
GET /api/admin/product-detail?antiFakeCode=SC17445555551234567890ABCD
```

---

## 7. 投诉管理模块

### 7.1 提交投诉

**请求**
```http
POST /api/complaint
Content-Type: application/json

{
  "productName": "有机大米",
  "complaintReason": "产品中发现异物"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| productName | string | 是 | 产品名称 | 不能为空 |
| complaintReason | string | 是 | 投诉原因 | 不能为空 |

**成功响应**
```json
{
  "id": 1,
  "productName": "有机大米",
  "complaintReason": "产品中发现异物",
  "complaintTime": "2026-04-25T15:00:00"
}
```

**失败响应**
```json
{
  "timestamp": "2026-04-25T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "参数验证失败",
  "errors": {
    "productName": "产品名称不能为空"
  }
}
```

---

### 7.2 查询所有投诉信息

**请求**
```http
GET /api/getAllComplaintInfo
```

**成功响应**
```json
[
  {
    "id": 1,
    "productName": "有机大米",
    "complaintReason": "产品中发现异物",
    "complaintTime": "2026-04-25T15:00:00",
    "batchNumber": null
  }
]
```

---

### 7.3 删除投诉

**请求**
```http
DELETE /api/deleteComplaintInfo/{id}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 投诉ID |

**成功响应**
```json
{
  "success": true,
  "message": "删除成功",
  "id": 1
}
```

**失败响应（投诉不存在）**
```json
"投诉信息不存在，ID: 1"
```

---

### 7.4 批量删除投诉

**请求**
```http
DELETE /api/deleteComplaintInfo/batch
Content-Type: application/json

[1, 2, 3]
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| body | array | 是 | 投诉ID数组 |

**成功响应**
```json
{
  "success": true,
  "successCount": 3,
  "failCount": 0,
  "message": "批量删除完成，成功: 3，失败: 0"
}
```

---

## 8. 数据导入模块

### 8.1 获取产品列表（数据导入）

**请求**
```http
POST /api/insert/products/list
```

**成功响应**
```json
[
  {
    "id": 1,
    "name": "有机大米",
    ...
  }
]
```

---

### 8.2 为产品生成二维码

自动创建批次并生成100个防伪码。

**请求**
```http
POST /api/insert/products/{productId}/generate-qrcode
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| productId | long | 产品ID |

**成功响应**
```json
{
  "codes": ["SC17445555551234567890ABCD", ...],
  "count": 100
}
```

---

### 8.3 批量生成二维码

**请求**
```http
POST /api/insert/products/batch-generate-qrcode
Content-Type: application/json

[1, 2, 3, 4, 5]
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| body | array | 是 | 产品ID数组 |

**成功响应**
```json
{
  "success": true,
  "successCount": 4,
  "failCount": 1,
  "message": "批量生成完成，成功: 4，失败: 1"
}
```

---

### 8.4 批量删除产品（清除二维码）

**请求**
```http
POST /api/insert/products/batch-delete
Content-Type: application/json

{
  "productIds": [1, 2, 3]
}
```

**成功响应**
```json
{
  "success": true,
  "successCount": 3,
  "failCount": 0,
  "message": "清除完成，成功: 3，失败: 0"
}
```

---

### 8.5 插入原材料采购记录（数据导入）

**请求**
```http
POST /api/insert/material-purchase
Content-Type: application/json

{
  "antiFakeCode": "SC17445555551234567890ABCD",
  "materialName": "有机稻谷",
  "batchNumber": "M20260425001",
  "producerName": "黑龙江农业公司",
  "producerAddress": "黑龙江省哈尔滨市",
  "supplierName": "东北农场"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| antiFakeCode | string | 是 | 产品防伪码（用于关联产品） |
| materialName | string | 是 | 原材料名称 |
| batchNumber | string | 是 | 原材料批次号 |
| producerName | string | 否 | 生产商名称 |
| producerAddress | string | 否 | 生产商地址 |
| supplierName | string | 否 | 供应商名称 |

---

### 8.6 查询原材料列表（数据导入）

**请求**
```http
GET /api/insert/material-purchase
GET /api/insert/material-purchase?productId=1
```

**别名接口**
```http
GET /api/insert/materials
GET /api/insert/materials?productId=1
```

---

### 8.7 删除原材料（数据导入）

**请求**
```http
DELETE /api/insert/material-purchase/{id}
```

**路径参数**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | long | 原材料记录ID |

---

### 8.8 查询生产批次列表（数据导入）

**请求**
```http
GET /api/insert/batches
GET /api/insert/batches?productId=1
```

---

### 8.9 查询所有检验记录

**请求**
```http
GET /api/insert/inspections
```

**成功响应**
```json
[
  {
    "id": 1,
    "batchId": 1,
    "sampleName": "大米样品A",
    "sampleQuantity": 500,
    "sampleSpecification": "符合GB/T标准",
    "imageUrl": "https://example.com/inspection.jpg"
  }
]
```

---

### 8.10 查询所有仓储记录

**请求**
```http
GET /api/insert/storages
```

**成功响应**
```json
[
  {
    "id": 1,
    "batchId": 1,
    "storageTime": "2026-04-25T10:00:00",
    "outboundTime": "2026-05-01T09:00:00",
    "quantity": 5000.0,
    "unit": "kg",
    "warehouseLocation": "A仓库-01区"
  }
]
```

---

### 8.11 查询所有运输销售记录

**请求**
```http
GET /api/insert/transport-sales
```

**成功响应**
```json
[
  {
    "id": 1,
    "batchId": 1,
    "transportCompany": "顺丰物流",
    "vehicleNumber": "黑A12345",
    "salesRegion": "北京市朝阳区",
    "receiverName": "张三",
    "receiverContact": "13800138000",
    "environmentTemperature": 20.0,
    "productTemperature": 18.0,
    "time": "2026-04-26T08:00:00"
  }
]
```

---

## 附录

### A. 溯源信息结构

完整溯源信息包含以下层级：

```
TraceInfo
├── product          # 产品信息
├── batch            # 批次信息
├── materials[]      # 原材料列表
├── inspection       # 检验信息
├── storage          # 仓储信息
└── transportSale    # 运输销售信息
```

### B. 防伪码状态说明

| 状态 | 说明 |
|------|------|
| 未激活 | 防伪码已生成，但未被扫描 |
| 已激活 | 首次扫描后自动激活 |

### C. 重复查询检测

当 `scanCount > 1` 时，表示该产品已被多次查询，可能是伪品。

### D. 错误码汇总

| 错误信息 | HTTP状态码 | 说明 |
|----------|------------|------|
| 产品不存在 | 400 | 产品ID不存在 |
| 产品名称不能为空 | 400 | 产品名称为空 |
| 至少选择一个原料批次 | 400 | 创建批次时未选择原材料 |
| 原材料不存在 | 400 | 原材料ID不存在 |
| 防伪码不存在 | 400 | 防伪码无效 |
| 生产批次不存在 | 400 | 批次ID或批次号不存在 |
| 验证码不能为空 | 400 | 未填写验证码 |
| 验证码错误 | 400 | 验证码不正确 |
| 账号已被锁定 | 400 | 登录失败次数过多 |
| 账号或密码错误 | 400 | 登录凭证错误 |
| 管理员已存在 | 409 | 用户名已被注册 |
| 投诉信息不存在 | 404 | 投诉ID不存在 |

---

**文档结束**
