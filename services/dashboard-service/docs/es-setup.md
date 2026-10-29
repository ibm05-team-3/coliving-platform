# dashboard-service Elasticsearch 설정

대시보드 서비스는 Elasticsearch(ES)에 데이터를 저장합니다.
ES 데이터는 깃허브에 올라가지 않으므로, 처음 실행할 때 아래 순서로 인덱스를 만들어야 합니다.
**인덱스가 없으면 서비스가 에러를 냅니다.**

## 0. 준비

1. 프로젝트 루트에서 ES와 Kibana를 띄웁니다.
   ```
   docker compose up -d elasticsearch kibana
   ```
2. `http://localhost:9200`에 JSON이 나오면 ES 정상, `http://localhost:5601`에서 Kibana가 열리면 됩니다 (처음엔 1~2분 걸립니다).
3. Kibana 왼쪽 메뉴 → Management → Dev Tools를 엽니다. 아래 내용을 붙여 넣고 ▶로 실행합니다.

## 1. 인덱스 생성 (필수)

세 개를 각각 실행합니다. 응답에 `"acknowledged": true`가 나오면 성공입니다.

```
PUT dashboard-snapshot
{
  "settings": { "number_of_shards": 1, "number_of_replicas": 0 },
  "mappings": {
    "properties": {
      "building_id":             { "type": "long" },
      "building_name":           { "type": "keyword" },
      "snapshot_date":           { "type": "date", "format": "yyyy-MM-dd" },
      "total_rooms":             { "type": "integer" },
      "occupied_rooms":          { "type": "integer" },
      "occupancy_rate":          { "type": "scaled_float", "scaling_factor": 100 },
      "resident_count":          { "type": "integer" },
      "moving_out_count":        { "type": "integer" },
      "pending_approval_count":  { "type": "integer" },
      "overdue_count":           { "type": "integer" },
      "month_collected_amount":  { "type": "long" },
      "month_unpaid_amount":     { "type": "long" },
      "total_unpaid_amount":     { "type": "long" },
      "deposit_held_amount":     { "type": "long" },
      "refund_scheduled_amount": { "type": "long" },
      "refund_scheduled_count":  { "type": "integer" },
      "created_at":              { "type": "date" },
      "updated_at":              { "type": "date" }
    }
  }
}
```

```
PUT activity-log
{
  "settings": { "number_of_shards": 1, "number_of_replicas": 0 },
  "mappings": {
    "properties": {
      "event_id":        { "type": "keyword" },
      "building_id":     { "type": "long" },
      "building_name":   { "type": "keyword" },
      "activity_type":   { "type": "keyword" },
      "content":         { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 512 } } },
      "status":          { "type": "keyword" },
      "source_service":  { "type": "keyword" },
      "source_ref_id":   { "type": "keyword" },
      "occurred_at":     { "type": "date" },
      "updated_at":      { "type": "date" }
    }
  }
}
```

```
PUT room-status
{
  "settings": { "number_of_shards": 1, "number_of_replicas": 0 },
  "mappings": {
    "properties": {
      "room_id":         { "type": "long" },
      "building_id":     { "type": "long" },
      "building_name":   { "type": "keyword" },
      "floor":           { "type": "integer" },
      "room_name":       { "type": "keyword" },
      "status":          { "type": "keyword" },
      "updated_at":      { "type": "date" }
    }
  }
}
```

## 2. 더미 데이터 (선택, 화면 확인용)

연동 전에 화면을 확인하기 위한 데이터입니다. 실제 서비스가 연결되면 지워도 됩니다.

- 건물 ID: **1 망원 스테이, 2 연남 하우스, 3 성수 로프트**
- 입주자는 1인 1실이라 입주자 수 = 입주 호실 수이고, 입주율은 퇴거 예정 호실도 포함해서 계산했습니다.
- 수납액 + 미수액 = 입주자 수 × 월세, 보증금 보관액 = 입주자 수 × 보증금이 되도록 맞췄습니다.

| 건물 | 호실 | 월세(평균) | 보증금(평균) |
|---|---|---|---|
| 1 망원 스테이 | 20호실 (4층 × 5) | 78만원 | 200만원 |
| 2 연남 하우스 | 15호실 (3층 × 5) | 69만원 | 150만원 |
| 3 성수 로프트 | 24호실 (4층 × 6) | 92만원 | 300만원 |

**날짜 값은 반드시 `2026-10-26T15:00:00.000Z`처럼 밀리초(`.000`)까지 써야 합니다.** 서비스가 이 형식만 읽습니다.
각 `_bulk`는 줄 하나가 데이터 하나이므로 **줄바꿈을 바꾸지 말고** 그대로 붙여 넣어야 합니다.
응답에 `"errors": false`가 나오면 성공입니다.

### 2-1. 기존 데이터 지우기 (이미 다른 더미가 있을 때만)

```
POST room-status/_delete_by_query
{ "query": { "match_all": {} } }

POST activity-log/_delete_by_query
{ "query": { "match_all": {} } }

POST dashboard-snapshot/_delete_by_query
{ "query": { "match_all": {} } }
```

### 2-2. 일별 스냅샷 (3개 건물 × 7건 = 21건)

```
POST _bulk
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-05-31"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-05-31","total_rooms":20,"occupied_rooms":17,"occupancy_rate":85.0,"resident_count":17,"moving_out_count":1,"pending_approval_count":2,"overdue_count":1,"month_collected_amount":12480000,"month_unpaid_amount":780000,"total_unpaid_amount":780000,"deposit_held_amount":34000000,"refund_scheduled_amount":2000000,"refund_scheduled_count":1,"created_at":"2026-05-31T15:00:00.000Z","updated_at":"2026-05-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-06-30"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-06-30","total_rooms":20,"occupied_rooms":18,"occupancy_rate":90.0,"resident_count":18,"moving_out_count":1,"pending_approval_count":1,"overdue_count":0,"month_collected_amount":14040000,"month_unpaid_amount":0,"total_unpaid_amount":0,"deposit_held_amount":36000000,"refund_scheduled_amount":2000000,"refund_scheduled_count":1,"created_at":"2026-06-30T15:00:00.000Z","updated_at":"2026-06-30T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-07-31"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-07-31","total_rooms":20,"occupied_rooms":16,"occupancy_rate":80.0,"resident_count":16,"moving_out_count":3,"pending_approval_count":2,"overdue_count":1,"month_collected_amount":11700000,"month_unpaid_amount":780000,"total_unpaid_amount":780000,"deposit_held_amount":32000000,"refund_scheduled_amount":6000000,"refund_scheduled_count":3,"created_at":"2026-07-31T15:00:00.000Z","updated_at":"2026-07-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-08-31"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-08-31","total_rooms":20,"occupied_rooms":15,"occupancy_rate":75.0,"resident_count":15,"moving_out_count":1,"pending_approval_count":3,"overdue_count":2,"month_collected_amount":10140000,"month_unpaid_amount":1560000,"total_unpaid_amount":2340000,"deposit_held_amount":30000000,"refund_scheduled_amount":2000000,"refund_scheduled_count":1,"created_at":"2026-08-31T15:00:00.000Z","updated_at":"2026-08-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-09-30"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-09-30","total_rooms":20,"occupied_rooms":16,"occupancy_rate":80.0,"resident_count":16,"moving_out_count":2,"pending_approval_count":2,"overdue_count":1,"month_collected_amount":11700000,"month_unpaid_amount":780000,"total_unpaid_amount":1560000,"deposit_held_amount":32000000,"refund_scheduled_amount":4000000,"refund_scheduled_count":2,"created_at":"2026-09-30T15:00:00.000Z","updated_at":"2026-09-30T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-10-19"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-10-19","total_rooms":20,"occupied_rooms":17,"occupancy_rate":85.0,"resident_count":17,"moving_out_count":2,"pending_approval_count":2,"overdue_count":2,"month_collected_amount":9360000,"month_unpaid_amount":3900000,"total_unpaid_amount":1560000,"deposit_held_amount":34000000,"refund_scheduled_amount":4000000,"refund_scheduled_count":2,"created_at":"2026-10-19T15:00:00.000Z","updated_at":"2026-10-19T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"1_2026-10-26"}}
{"building_id":1,"building_name":"망원 스테이","snapshot_date":"2026-10-26","total_rooms":20,"occupied_rooms":17,"occupancy_rate":85.0,"resident_count":17,"moving_out_count":2,"pending_approval_count":1,"overdue_count":1,"month_collected_amount":12480000,"month_unpaid_amount":780000,"total_unpaid_amount":780000,"deposit_held_amount":34000000,"refund_scheduled_amount":4000000,"refund_scheduled_count":2,"created_at":"2026-10-26T15:00:00.000Z","updated_at":"2026-10-26T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-05-31"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-05-31","total_rooms":15,"occupied_rooms":12,"occupancy_rate":80.0,"resident_count":12,"moving_out_count":1,"pending_approval_count":1,"overdue_count":0,"month_collected_amount":8280000,"month_unpaid_amount":0,"total_unpaid_amount":0,"deposit_held_amount":18000000,"refund_scheduled_amount":1500000,"refund_scheduled_count":1,"created_at":"2026-05-31T15:00:00.000Z","updated_at":"2026-05-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-06-30"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-06-30","total_rooms":15,"occupied_rooms":11,"occupancy_rate":73.3,"resident_count":11,"moving_out_count":2,"pending_approval_count":2,"overdue_count":1,"month_collected_amount":6900000,"month_unpaid_amount":690000,"total_unpaid_amount":690000,"deposit_held_amount":16500000,"refund_scheduled_amount":3000000,"refund_scheduled_count":2,"created_at":"2026-06-30T15:00:00.000Z","updated_at":"2026-06-30T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-07-31"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-07-31","total_rooms":15,"occupied_rooms":13,"occupancy_rate":86.7,"resident_count":13,"moving_out_count":0,"pending_approval_count":1,"overdue_count":0,"month_collected_amount":8970000,"month_unpaid_amount":0,"total_unpaid_amount":0,"deposit_held_amount":19500000,"refund_scheduled_amount":0,"refund_scheduled_count":0,"created_at":"2026-07-31T15:00:00.000Z","updated_at":"2026-07-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-08-31"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-08-31","total_rooms":15,"occupied_rooms":14,"occupancy_rate":93.3,"resident_count":14,"moving_out_count":1,"pending_approval_count":0,"overdue_count":1,"month_collected_amount":8970000,"month_unpaid_amount":690000,"total_unpaid_amount":690000,"deposit_held_amount":21000000,"refund_scheduled_amount":1500000,"refund_scheduled_count":1,"created_at":"2026-08-31T15:00:00.000Z","updated_at":"2026-08-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-09-30"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-09-30","total_rooms":15,"occupied_rooms":13,"occupancy_rate":86.7,"resident_count":13,"moving_out_count":2,"pending_approval_count":2,"overdue_count":2,"month_collected_amount":7590000,"month_unpaid_amount":1380000,"total_unpaid_amount":2070000,"deposit_held_amount":19500000,"refund_scheduled_amount":3000000,"refund_scheduled_count":2,"created_at":"2026-09-30T15:00:00.000Z","updated_at":"2026-09-30T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-10-19"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-10-19","total_rooms":15,"occupied_rooms":14,"occupancy_rate":93.3,"resident_count":14,"moving_out_count":1,"pending_approval_count":1,"overdue_count":0,"month_collected_amount":6210000,"month_unpaid_amount":3450000,"total_unpaid_amount":0,"deposit_held_amount":21000000,"refund_scheduled_amount":1500000,"refund_scheduled_count":1,"created_at":"2026-10-19T15:00:00.000Z","updated_at":"2026-10-19T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"2_2026-10-26"}}
{"building_id":2,"building_name":"연남 하우스","snapshot_date":"2026-10-26","total_rooms":15,"occupied_rooms":14,"occupancy_rate":93.3,"resident_count":14,"moving_out_count":1,"pending_approval_count":1,"overdue_count":1,"month_collected_amount":8970000,"month_unpaid_amount":690000,"total_unpaid_amount":690000,"deposit_held_amount":21000000,"refund_scheduled_amount":1500000,"refund_scheduled_count":1,"created_at":"2026-10-26T15:00:00.000Z","updated_at":"2026-10-26T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-05-31"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-05-31","total_rooms":24,"occupied_rooms":17,"occupancy_rate":70.8,"resident_count":17,"moving_out_count":1,"pending_approval_count":3,"overdue_count":1,"month_collected_amount":14720000,"month_unpaid_amount":920000,"total_unpaid_amount":920000,"deposit_held_amount":51000000,"refund_scheduled_amount":3000000,"refund_scheduled_count":1,"created_at":"2026-05-31T15:00:00.000Z","updated_at":"2026-05-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-06-30"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-06-30","total_rooms":24,"occupied_rooms":19,"occupancy_rate":79.2,"resident_count":19,"moving_out_count":2,"pending_approval_count":2,"overdue_count":2,"month_collected_amount":15640000,"month_unpaid_amount":1840000,"total_unpaid_amount":2760000,"deposit_held_amount":57000000,"refund_scheduled_amount":6000000,"refund_scheduled_count":2,"created_at":"2026-06-30T15:00:00.000Z","updated_at":"2026-06-30T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-07-31"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-07-31","total_rooms":24,"occupied_rooms":21,"occupancy_rate":87.5,"resident_count":21,"moving_out_count":1,"pending_approval_count":3,"overdue_count":1,"month_collected_amount":18400000,"month_unpaid_amount":920000,"total_unpaid_amount":1840000,"deposit_held_amount":63000000,"refund_scheduled_amount":3000000,"refund_scheduled_count":1,"created_at":"2026-07-31T15:00:00.000Z","updated_at":"2026-07-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-08-31"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-08-31","total_rooms":24,"occupied_rooms":20,"occupancy_rate":83.3,"resident_count":20,"moving_out_count":3,"pending_approval_count":2,"overdue_count":2,"month_collected_amount":16560000,"month_unpaid_amount":1840000,"total_unpaid_amount":2760000,"deposit_held_amount":60000000,"refund_scheduled_amount":9000000,"refund_scheduled_count":3,"created_at":"2026-08-31T15:00:00.000Z","updated_at":"2026-08-31T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-09-30"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-09-30","total_rooms":24,"occupied_rooms":19,"occupancy_rate":79.2,"resident_count":19,"moving_out_count":2,"pending_approval_count":1,"overdue_count":1,"month_collected_amount":16560000,"month_unpaid_amount":920000,"total_unpaid_amount":1840000,"deposit_held_amount":57000000,"refund_scheduled_amount":6000000,"refund_scheduled_count":2,"created_at":"2026-09-30T15:00:00.000Z","updated_at":"2026-09-30T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-10-19"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-10-19","total_rooms":24,"occupied_rooms":20,"occupancy_rate":83.3,"resident_count":20,"moving_out_count":2,"pending_approval_count":3,"overdue_count":2,"month_collected_amount":14720000,"month_unpaid_amount":3680000,"total_unpaid_amount":2760000,"deposit_held_amount":60000000,"refund_scheduled_amount":6000000,"refund_scheduled_count":2,"created_at":"2026-10-19T15:00:00.000Z","updated_at":"2026-10-19T15:00:00.000Z"}
{"index":{"_index":"dashboard-snapshot","_id":"3_2026-10-26"}}
{"building_id":3,"building_name":"성수 로프트","snapshot_date":"2026-10-26","total_rooms":24,"occupied_rooms":21,"occupancy_rate":87.5,"resident_count":21,"moving_out_count":2,"pending_approval_count":2,"overdue_count":2,"month_collected_amount":17480000,"month_unpaid_amount":1840000,"total_unpaid_amount":2760000,"deposit_held_amount":63000000,"refund_scheduled_amount":6000000,"refund_scheduled_count":2,"created_at":"2026-10-26T15:00:00.000Z","updated_at":"2026-10-26T15:00:00.000Z"}
```

### 2-3. 호실 현황 (20 + 15 + 24 = 59호실)

호실 ID는 `건물번호 × 1000 + 호실번호`입니다. (예: 망원 스테이 101호 = 1101)

```
POST _bulk
{"index":{"_index":"room-status","_id":"1101"}}
{"room_id":1101,"building_id":1,"building_name":"망원 스테이","floor":1,"room_name":"101호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1102"}}
{"room_id":1102,"building_id":1,"building_name":"망원 스테이","floor":1,"room_name":"102호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1103"}}
{"room_id":1103,"building_id":1,"building_name":"망원 스테이","floor":1,"room_name":"103호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1104"}}
{"room_id":1104,"building_id":1,"building_name":"망원 스테이","floor":1,"room_name":"104호","status":"MOVING_OUT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1105"}}
{"room_id":1105,"building_id":1,"building_name":"망원 스테이","floor":1,"room_name":"105호","status":"VACANT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1201"}}
{"room_id":1201,"building_id":1,"building_name":"망원 스테이","floor":2,"room_name":"201호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1202"}}
{"room_id":1202,"building_id":1,"building_name":"망원 스테이","floor":2,"room_name":"202호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1203"}}
{"room_id":1203,"building_id":1,"building_name":"망원 스테이","floor":2,"room_name":"203호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1204"}}
{"room_id":1204,"building_id":1,"building_name":"망원 스테이","floor":2,"room_name":"204호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1205"}}
{"room_id":1205,"building_id":1,"building_name":"망원 스테이","floor":2,"room_name":"205호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1301"}}
{"room_id":1301,"building_id":1,"building_name":"망원 스테이","floor":3,"room_name":"301호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1302"}}
{"room_id":1302,"building_id":1,"building_name":"망원 스테이","floor":3,"room_name":"302호","status":"APPLYING","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1303"}}
{"room_id":1303,"building_id":1,"building_name":"망원 스테이","floor":3,"room_name":"303호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1304"}}
{"room_id":1304,"building_id":1,"building_name":"망원 스테이","floor":3,"room_name":"304호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1305"}}
{"room_id":1305,"building_id":1,"building_name":"망원 스테이","floor":3,"room_name":"305호","status":"VACANT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1401"}}
{"room_id":1401,"building_id":1,"building_name":"망원 스테이","floor":4,"room_name":"401호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1402"}}
{"room_id":1402,"building_id":1,"building_name":"망원 스테이","floor":4,"room_name":"402호","status":"MOVING_OUT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1403"}}
{"room_id":1403,"building_id":1,"building_name":"망원 스테이","floor":4,"room_name":"403호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1404"}}
{"room_id":1404,"building_id":1,"building_name":"망원 스테이","floor":4,"room_name":"404호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"1405"}}
{"room_id":1405,"building_id":1,"building_name":"망원 스테이","floor":4,"room_name":"405호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2101"}}
{"room_id":2101,"building_id":2,"building_name":"연남 하우스","floor":1,"room_name":"101호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2102"}}
{"room_id":2102,"building_id":2,"building_name":"연남 하우스","floor":1,"room_name":"102호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2103"}}
{"room_id":2103,"building_id":2,"building_name":"연남 하우스","floor":1,"room_name":"103호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2104"}}
{"room_id":2104,"building_id":2,"building_name":"연남 하우스","floor":1,"room_name":"104호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2105"}}
{"room_id":2105,"building_id":2,"building_name":"연남 하우스","floor":1,"room_name":"105호","status":"MOVING_OUT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2201"}}
{"room_id":2201,"building_id":2,"building_name":"연남 하우스","floor":2,"room_name":"201호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2202"}}
{"room_id":2202,"building_id":2,"building_name":"연남 하우스","floor":2,"room_name":"202호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2203"}}
{"room_id":2203,"building_id":2,"building_name":"연남 하우스","floor":2,"room_name":"203호","status":"APPLYING","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2204"}}
{"room_id":2204,"building_id":2,"building_name":"연남 하우스","floor":2,"room_name":"204호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2205"}}
{"room_id":2205,"building_id":2,"building_name":"연남 하우스","floor":2,"room_name":"205호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2301"}}
{"room_id":2301,"building_id":2,"building_name":"연남 하우스","floor":3,"room_name":"301호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2302"}}
{"room_id":2302,"building_id":2,"building_name":"연남 하우스","floor":3,"room_name":"302호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2303"}}
{"room_id":2303,"building_id":2,"building_name":"연남 하우스","floor":3,"room_name":"303호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2304"}}
{"room_id":2304,"building_id":2,"building_name":"연남 하우스","floor":3,"room_name":"304호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"2305"}}
{"room_id":2305,"building_id":2,"building_name":"연남 하우스","floor":3,"room_name":"305호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3101"}}
{"room_id":3101,"building_id":3,"building_name":"성수 로프트","floor":1,"room_name":"101호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3102"}}
{"room_id":3102,"building_id":3,"building_name":"성수 로프트","floor":1,"room_name":"102호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3103"}}
{"room_id":3103,"building_id":3,"building_name":"성수 로프트","floor":1,"room_name":"103호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3104"}}
{"room_id":3104,"building_id":3,"building_name":"성수 로프트","floor":1,"room_name":"104호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3105"}}
{"room_id":3105,"building_id":3,"building_name":"성수 로프트","floor":1,"room_name":"105호","status":"MOVING_OUT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3106"}}
{"room_id":3106,"building_id":3,"building_name":"성수 로프트","floor":1,"room_name":"106호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3201"}}
{"room_id":3201,"building_id":3,"building_name":"성수 로프트","floor":2,"room_name":"201호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3202"}}
{"room_id":3202,"building_id":3,"building_name":"성수 로프트","floor":2,"room_name":"202호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3203"}}
{"room_id":3203,"building_id":3,"building_name":"성수 로프트","floor":2,"room_name":"203호","status":"APPLYING","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3204"}}
{"room_id":3204,"building_id":3,"building_name":"성수 로프트","floor":2,"room_name":"204호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3205"}}
{"room_id":3205,"building_id":3,"building_name":"성수 로프트","floor":2,"room_name":"205호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3206"}}
{"room_id":3206,"building_id":3,"building_name":"성수 로프트","floor":2,"room_name":"206호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3301"}}
{"room_id":3301,"building_id":3,"building_name":"성수 로프트","floor":3,"room_name":"301호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3302"}}
{"room_id":3302,"building_id":3,"building_name":"성수 로프트","floor":3,"room_name":"302호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3303"}}
{"room_id":3303,"building_id":3,"building_name":"성수 로프트","floor":3,"room_name":"303호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3304"}}
{"room_id":3304,"building_id":3,"building_name":"성수 로프트","floor":3,"room_name":"304호","status":"MOVING_OUT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3305"}}
{"room_id":3305,"building_id":3,"building_name":"성수 로프트","floor":3,"room_name":"305호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3306"}}
{"room_id":3306,"building_id":3,"building_name":"성수 로프트","floor":3,"room_name":"306호","status":"VACANT","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3401"}}
{"room_id":3401,"building_id":3,"building_name":"성수 로프트","floor":4,"room_name":"401호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3402"}}
{"room_id":3402,"building_id":3,"building_name":"성수 로프트","floor":4,"room_name":"402호","status":"APPLYING","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3403"}}
{"room_id":3403,"building_id":3,"building_name":"성수 로프트","floor":4,"room_name":"403호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3404"}}
{"room_id":3404,"building_id":3,"building_name":"성수 로프트","floor":4,"room_name":"404호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3405"}}
{"room_id":3405,"building_id":3,"building_name":"성수 로프트","floor":4,"room_name":"405호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
{"index":{"_index":"room-status","_id":"3406"}}
{"room_id":3406,"building_id":3,"building_name":"성수 로프트","floor":4,"room_name":"406호","status":"OCCUPIED","updated_at":"2026-10-20T00:00:00.000Z"}
```

### 2-4. 활동 로그 (14건)

```
POST _bulk
{"index":{"_index":"activity-log","_id":"PAYMENT-310"}}
{"building_id":1,"building_name":"망원 스테이","activity_type":"PAYMENT","content":"망원 스테이 302호 결제 완료 (김지우, 2,780,000원)","status":"COMPLETED","source_service":"contract-service","source_ref_id":"310","occurred_at":"2026-10-28T01:12:00.000Z","updated_at":"2026-10-28T01:12:00.000Z"}
{"index":{"_index":"activity-log","_id":"RESERVATION-182"}}
{"building_id":1,"building_name":"망원 스테이","activity_type":"RESERVATION","content":"공용 주방 예약 신청 (회원 5012)","status":"PENDING","source_service":"amenity-service","source_ref_id":"182","occurred_at":"2026-10-28T00:30:00.000Z","updated_at":"2026-10-28T00:30:00.000Z"}
{"index":{"_index":"activity-log","_id":"MOVE_IN-655"}}
{"building_id":1,"building_name":"망원 스테이","activity_type":"MOVE_IN","content":"망원 스테이 204호 입주 완료","status":"COMPLETED","source_service":"contract-service","source_ref_id":"655","occurred_at":"2026-10-27T05:50:00.000Z","updated_at":"2026-10-27T06:05:00.000Z"}
{"index":{"_index":"activity-log","_id":"CONTRACT-662"}}
{"building_id":1,"building_name":"망원 스테이","activity_type":"CONTRACT","content":"망원 스테이 402호 퇴실 요청 (박도현)","status":"PENDING","source_service":"contract-service","source_ref_id":"662","occurred_at":"2026-10-27T03:20:00.000Z","updated_at":"2026-10-27T03:20:00.000Z"}
{"index":{"_index":"activity-log","_id":"RESERVATION-181"}}
{"building_id":1,"building_name":"망원 스테이","activity_type":"RESERVATION","content":"공용 세탁실 예약 승인 (회원 5003)","status":"COMPLETED","source_service":"amenity-service","source_ref_id":"181","occurred_at":"2026-10-26T08:10:00.000Z","updated_at":"2026-10-26T09:45:00.000Z"}
{"index":{"_index":"activity-log","_id":"CONTRACT-720"}}
{"building_id":2,"building_name":"연남 하우스","activity_type":"CONTRACT","content":"연남 하우스 203호 계약 신청 (최예린)","status":"PENDING","source_service":"contract-service","source_ref_id":"720","occurred_at":"2026-10-28T02:05:00.000Z","updated_at":"2026-10-28T02:05:00.000Z"}
{"index":{"_index":"activity-log","_id":"RESERVATION-190"}}
{"building_id":2,"building_name":"연남 하우스","activity_type":"RESERVATION","content":"공용 라운지 예약 승인 (회원 5104)","status":"COMPLETED","source_service":"amenity-service","source_ref_id":"190","occurred_at":"2026-10-28T00:50:00.000Z","updated_at":"2026-10-28T01:30:00.000Z"}
{"index":{"_index":"activity-log","_id":"CONTRACT-705"}}
{"building_id":2,"building_name":"연남 하우스","activity_type":"CONTRACT","content":"연남 하우스 105호 퇴실 요청 (정민호)","status":"PENDING","source_service":"contract-service","source_ref_id":"705","occurred_at":"2026-10-27T07:40:00.000Z","updated_at":"2026-10-27T07:40:00.000Z"}
{"index":{"_index":"activity-log","_id":"PAYMENT-295"}}
{"building_id":2,"building_name":"연남 하우스","activity_type":"PAYMENT","content":"연남 하우스 301호 결제 완료 (오세진, 2,190,000원)","status":"COMPLETED","source_service":"contract-service","source_ref_id":"295","occurred_at":"2026-10-26T04:15:00.000Z","updated_at":"2026-10-26T04:15:00.000Z"}
{"index":{"_index":"activity-log","_id":"CONTRACT-733"}}
{"building_id":3,"building_name":"성수 로프트","activity_type":"CONTRACT","content":"성수 로프트 203호 계약 신청 (한소희)","status":"PENDING","source_service":"contract-service","source_ref_id":"733","occurred_at":"2026-10-28T02:20:00.000Z","updated_at":"2026-10-28T02:20:00.000Z"}
{"index":{"_index":"activity-log","_id":"RESERVATION-205"}}
{"building_id":3,"building_name":"성수 로프트","activity_type":"RESERVATION","content":"스터디룸 예약 승인 (회원 5231)","status":"COMPLETED","source_service":"amenity-service","source_ref_id":"205","occurred_at":"2026-10-28T00:10:00.000Z","updated_at":"2026-10-28T00:40:00.000Z"}
{"index":{"_index":"activity-log","_id":"CONTRACT-731"}}
{"building_id":3,"building_name":"성수 로프트","activity_type":"CONTRACT","content":"성수 로프트 402호 계약 신청 (이도윤)","status":"PENDING","source_service":"contract-service","source_ref_id":"731","occurred_at":"2026-10-27T08:30:00.000Z","updated_at":"2026-10-27T08:30:00.000Z"}
{"index":{"_index":"activity-log","_id":"MOVE_IN-690"}}
{"building_id":3,"building_name":"성수 로프트","activity_type":"MOVE_IN","content":"성수 로프트 106호 입주 완료","status":"COMPLETED","source_service":"contract-service","source_ref_id":"690","occurred_at":"2026-10-27T01:30:00.000Z","updated_at":"2026-10-27T01:30:00.000Z"}
{"index":{"_index":"activity-log","_id":"PAYMENT-318"}}
{"building_id":3,"building_name":"성수 로프트","activity_type":"PAYMENT","content":"성수 로프트 106호 결제 완료 (장현우, 3,920,000원)","status":"COMPLETED","source_service":"contract-service","source_ref_id":"318","occurred_at":"2026-10-26T06:00:00.000Z","updated_at":"2026-10-26T06:00:00.000Z"}
```

### 2-5. 확인

```
GET dashboard-snapshot/_count
GET room-status/_count
GET activity-log/_count
```

각각 **21, 59, 14**가 나오면 정상입니다.

## 3. 실행

| 서비스 | 방법 | 주소 |
|---|---|---|
| dashboard-service | IntelliJ에서 `DashboardServiceApplication` 실행 (Kafka, ES가 떠 있어야 함) | `localhost:8085` |
| admin-web | `frontends/admin-web`에서 `npm run dev` | `localhost:3000` |

- admin-web은 `/api/dashboard/**` 요청을 `localhost:8085`로 전달합니다 (`vite.config.js`의 proxy).
- Kafka 토픽(`contract.status-changed`, `payment.completed`, `reservation.status-changed`)이 아직 없으면 서비스 시작 시 경고 로그가 나올 수 있으나, 메시지를 처음 받을 때 생성되므로 무시해도 됩니다.

## 4. 더미 데이터 지우기

2-1의 삭제 명령을 그대로 실행합니다.
