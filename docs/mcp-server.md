# TimeCampus Admin MCP Server

TimeCampus 后端通过 Spring AI MCP WebMVC Starter 暴露 Streamable HTTP MCP Server，用于给 agent 维护 POI、官方影像资料、审核状态和展示文案。

## 启用方式

默认关闭 MCP Server，避免管理写能力在生产环境被意外暴露。启用时设置：

```bash
TIMECAMPUS_MCP_ENABLED=true
TIMECAMPUS_MCP_TOKEN=replace-with-a-long-random-token
TIMECAMPUS_MCP_ADMIN_ID=1
TIMECAMPUS_MCP_ADMIN_ROLE=admin
```

默认端点：

```text
POST/GET http://<host>:<port>/mcp
Header: X-TimeCampus-MCP-Token: <TIMECAMPUS_MCP_TOKEN>
```

也支持 `Authorization: Bearer <token>`。如需改端点，可设置 `TIMECAMPUS_MCP_ENDPOINT=/your-mcp-path`。

## Tools

读工具：

- `timecampus_search_pois`：按关键字/状态搜索 POI。
- `timecampus_get_poi`：获取 POI 详情。
- `timecampus_list_media`：按 POI、类型、审核状态、年份范围查询影像。
- `timecampus_get_media`：获取单条影像详情。
- `timecampus_rag_search`：检索 POI、影像、评论和维护规范，给 agent 提供 grounded context。
- `timecampus_rag_context_pack`：围绕一个维护任务生成 RAG 上下文包和后续调用建议。
- `timecampus_rag_corpus_summary`：查看 RAG 语料覆盖范围和文档数量。
- `timecampus_rag_rebuild_vector_index`：从 MySQL 抽取业务数据，切块后写入 Qdrant。

POI 写工具：

- `timecampus_create_poi`：新增 POI。
- `timecampus_update_poi`：局部更新 POI 基础信息。
- `timecampus_update_poi_copy`：只更新 POI 简介和冷知识。
- `timecampus_delete_poi`：删除 POI，需要 `confirmDelete=true`。

影像写工具：

- `timecampus_import_official_media`：批量导入官方影像记录。
- `timecampus_update_media_metadata`：更新影像 POI、图片路径、年份、说明、审核状态。
- `timecampus_update_media_copy`：只更新影像说明。
- `timecampus_delete_media`：删除影像，需要 `confirmDelete=true`。
- `timecampus_approve_media`：审核通过 pending 影像。
- `timecampus_reject_media`：驳回 pending 影像。

## Resources

- `timecampus://pois`：完整 POI 目录。
- `timecampus://poi/{poiId}`：单个 POI 和关联影像。
- `timecampus://media/{mediaId}`：单条影像详情。
- `timecampus://media/by-poi/{poiId}`：某个 POI 下的影像列表。
- `timecampus://admin/dashboard`：管理端统计概览。
- `timecampus://content-guidelines`：POI、影像和文案维护规则。
- `timecampus://rag/corpus`：RAG 语料摘要。

## Prompts

- `timecampus_poi_editor`：新增或编辑 POI 的维护流程。
- `timecampus_media_importer`：新增或编辑影像资料的维护流程。
- `timecampus_copy_editor`：POI 简介、冷知识、影像说明的文案润色流程。
- `timecampus_content_auditor`：内容巡检流程，发现缺失文案、影像断层和审核积压。
- `timecampus_rag_grounded_maintenance`：基于 RAG 的维护流程，要求 agent 先检索再编辑。

## RAG 封装

当前 RAG 实现支持两层检索：

- 优先：Spring AI `VectorStore` + Qdrant 向量检索。
- 兜底：本地词法检索，保证 Qdrant 或 EmbeddingModel 未配置时 MCP 仍可用。

语料从 MySQL 业务数据构建：

- `poi`：地点名称、坐标、简介、冷知识、上下架状态。
- `media`：影像关联 POI、类型、年份、说明、图片路径和审核状态。
- `comment`：已审核评论，或在 `includePending=true` 时包含待审核/驳回评论。
- `guideline`：内容维护规范和 agent 写入约束。

索引流程：

1. 调用 `timecampus_rag_rebuild_vector_index`。
2. 后端从 MySQL 抽取 `poi`、`media`、`comment` 和内置 guideline。
3. 将每条业务记录转换为稳定 `source_id` 的 RAG 文档。
4. 按 `timecampus.rag.chunk-max-chars` 和 `chunk-overlap-chars` 切块。
5. 写入 Qdrant collection，metadata 保留 `source_id`、`rag_type`、`poiId`、`reviewStatus`、`uri` 等字段。

检索结果会返回 `document.uri`，agent 写入前应再读取对应 resource/tool 获取当前值。

### Admin Agent HTTP API

Portal 管理端可通过普通后台 API 使用同一套 RAG 和草案生成能力：

- `POST /api/v1/admin/agent/rag/search`
- `POST /api/v1/admin/agent/rag/context-pack`
- `POST /api/v1/admin/agent/rag/rebuild-index`
- `POST /api/v1/admin/agent/draft`

`/agent/draft` 默认返回规则草案；启用 DeepSeek chat 后返回模型草案，同时保留 RAG context、质量分和执行门槛。

质量门禁字段：

- `quality`：`grounding`、`actionSafety`、`completeness`、`citationDensity`、`overall` 五项 0-100 分。
- `qualityGate.executable`：是否达到管理写入执行线。
- `qualityGate.minOverall`：默认 85。
- `qualityGate.minActionSafety`：默认 80。
- `qualityGate.reasons`：不达标原因或执行前提示。
- `gates`：兼容前端展示的简短标签；低于执行线时只生成草案，不自动写入。

### Visitor Route API

游客导览 agent 使用公开接口 `POST /api/v1/map/walking-route`。前端传入 2-8 个 GCJ02 点位，后端逐段调用腾讯地图步行路线规划并返回总距离、总耗时和每段摘要。

### Qdrant 配置

仓库根目录 `docker-compose.yaml` 已包含 Qdrant 服务：

```bash
docker compose up -d qdrant
```

后端配置示例：

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: ${QDRANT_HOST:localhost}
        port: ${QDRANT_GRPC_PORT:6334}
        collection-name: ${QDRANT_COLLECTION:timecampus_rag}
        initialize-schema: true
```

Qdrant VectorStore 还需要一个 Spring AI `EmbeddingModel` Bean。项目内置了可选的智谱 `embedding-3` HTTP 实现，默认关闭：

```bash
ZHIPU_EMBEDDING_ENABLED=true
ZHIPU_API_KEY=<your-api-key>
ZHIPU_EMBEDDING_MODEL=embedding-3
ZHIPU_EMBEDDING_DIMENSIONS=768
```

也可以接 OpenAI、通义、本地模型等任意 Spring AI embedding starter/bean。没有 `EmbeddingModel` 时不会创建 Qdrant VectorStore，MCP RAG 会自动退回词法检索。

### DeepSeek 草案生成

用于管理端 agent draft，默认关闭：

```bash
DEEPSEEK_CHAT_ENABLED=true
DEEPSEEK_API_KEY=<your-api-key>
DEEPSEEK_CHAT_MODEL=deepseek-v4-flash
```

本地真实 key 可写入被 `.gitignore` 忽略的 `timecampus-server/src/main/resources/application-dev.yaml`。

## 测试

```bash
mvn -q -pl timecampus-server -am test
```

## 维护原则

- 写入前先读相关 Resource 或调用读工具。
- 文案改动优先用 copy-only 工具，降低误改结构化字段的风险。
- 删除工具必须显式确认。
- 对年份、地点、版权、来源不明确的影像，不应直接导入为 `approved`。
