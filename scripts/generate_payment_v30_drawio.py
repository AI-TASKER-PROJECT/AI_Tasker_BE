from html import escape
from pathlib import Path
from xml.sax.saxutils import quoteattr


OUT = Path("outputs/payment-drawio/payment_v30_tables.drawio")


TABLES = [
    {
        "name": "MEMBERSHIP_PACKAGES",
        "x": 40,
        "y": 40,
        "columns": [
            ("BIGSERIAL", "package_id", "PK"),
            ("VARCHAR(20)", "role_type", ""),
            ("VARCHAR(80)", "package_code", ""),
            ("VARCHAR(120)", "package_name", ""),
            ("NUMERIC(19,2)", "price", ""),
            ("INT", "badge_duration_days", ""),
            ("INT", "job_post_quota", ""),
            ("INT", "proposal_quota", ""),
            ("BOOLEAN", "recommend_visibility", ""),
            ("BOOLEAN", "is_active", ""),
            ("TIMESTAMP", "created_at", ""),
            ("TIMESTAMP", "updated_at", ""),
        ],
    },
    {
        "name": "MEMBERSHIP_PURCHASES",
        "x": 410,
        "y": 40,
        "columns": [
            ("BIGSERIAL", "purchase_id", "PK"),
            ("INT", "account_id", "FK"),
            ("BIGINT", "package_id", "FK"),
            ("NUMERIC(19,2)", "amount", ""),
            ("VARCHAR(20)", "status", ""),
            ("TIMESTAMP", "badge_start_at", ""),
            ("TIMESTAMP", "badge_end_at", ""),
            ("BIGINT", "wallet_transaction_id", "FK"),
            ("TIMESTAMP", "created_at", ""),
            ("TIMESTAMP", "updated_at", ""),
        ],
    },
    {
        "name": "USER_QUOTAS",
        "x": 780,
        "y": 40,
        "columns": [
            ("BIGSERIAL", "quota_id", "PK"),
            ("INT", "account_id", "FK"),
            ("INT", "job_post_quota_balance", ""),
            ("INT", "proposal_quota_balance", ""),
            ("TIMESTAMP", "badge_expired_at", ""),
            ("TIMESTAMP", "premium_expired_at", ""),
            ("TIMESTAMP", "created_at", ""),
            ("TIMESTAMP", "updated_at", ""),
        ],
    },
    {
        "name": "QUOTA_USAGE_LOGS",
        "x": 40,
        "y": 420,
        "columns": [
            ("BIGSERIAL", "quota_usage_id", "PK"),
            ("INT", "account_id", "FK"),
            ("VARCHAR(20)", "quota_type", ""),
            ("VARCHAR(20)", "action_type", ""),
            ("INT", "amount", ""),
            ("INT", "balance_before", ""),
            ("INT", "balance_after", ""),
            ("VARCHAR(50)", "reference_type", ""),
            ("BIGINT", "reference_id", ""),
            ("TIMESTAMP", "created_at", ""),
        ],
    },
    {
        "name": "CONTRACT_DEPOSITS",
        "x": 410,
        "y": 420,
        "columns": [
            ("BIGSERIAL", "deposit_id", "PK"),
            ("INT", "contract_id", "FK"),
            ("INT", "business_id", "FK"),
            ("NUMERIC(19,2)", "deposit_amount", ""),
            ("NUMERIC(19,2)", "held_amount", ""),
            ("NUMERIC(19,2)", "refunded_amount", ""),
            ("NUMERIC(19,2)", "resolved_amount", ""),
            ("VARCHAR(30)", "status", ""),
            ("BIGINT", "hold_transaction_id", "FK"),
            ("BIGINT", "refund_transaction_id", "FK"),
            ("INT", "admin_id", "FK"),
            ("TEXT", "admin_note", ""),
            ("TIMESTAMP", "paid_at", ""),
            ("TIMESTAMP", "refunded_at", ""),
            ("TIMESTAMP", "created_at", ""),
            ("TIMESTAMP", "updated_at", ""),
        ],
    },
    {
        "name": "WITHDRAWAL_REQUESTS",
        "x": 780,
        "y": 420,
        "columns": [
            ("BIGSERIAL", "withdrawal_id", "PK"),
            ("INT", "account_id", "FK"),
            ("BIGINT", "wallet_id", "FK"),
            ("NUMERIC(19,2)", "amount", ""),
            ("VARCHAR(120)", "bank_name", ""),
            ("VARCHAR(80)", "bank_account_number", ""),
            ("VARCHAR(160)", "bank_account_holder", ""),
            ("VARCHAR(20)", "status", ""),
            ("BIGINT", "hold_transaction_id", "FK"),
            ("BIGINT", "review_transaction_id", "FK"),
            ("INT", "admin_id", "FK"),
            ("TEXT", "admin_note", ""),
            ("TIMESTAMP", "requested_at", ""),
            ("TIMESTAMP", "reviewed_at", ""),
            ("TIMESTAMP", "created_at", ""),
            ("TIMESTAMP", "updated_at", ""),
        ],
    },
]


BASE_STYLE = (
    "shape=table;startSize=28;container=1;collapsible=0;childLayout=tableLayout;"
    "fixedRows=1;rowLines=1;fontStyle=1;align=center;resizeLast=1;"
    "fillColor=#ffffff;strokeColor=#6aa6df;gradientColor=none;"
)
HEADER_STYLE = (
    "shape=partialRectangle;connectable=0;fillColor=#1ba1dc;strokeColor=#1ba1dc;"
    "fontColor=#ffffff;fontStyle=1;align=center;verticalAlign=middle;"
)
CELL_STYLE = (
    "shape=partialRectangle;connectable=0;fillColor=none;strokeColor=#8dbff0;"
    "align=left;verticalAlign=middle;spacingLeft=6;fontStyle=1;"
)
KEY_STYLE = (
    "shape=partialRectangle;connectable=0;fillColor=none;strokeColor=#8dbff0;"
    "align=center;verticalAlign=middle;fontStyle=1;"
)


def cell(cell_id, parent, value, style, x, y, width, height):
    return (
        f'<mxCell id="{cell_id}" value={quoteattr(escape(value))} style={quoteattr(style)} '
        f'vertex="1" parent="{parent}"><mxGeometry x="{x}" y="{y}" width="{width}" '
        f'height="{height}" as="geometry"/></mxCell>'
    )


def table_xml(index, table):
    table_id = f"table_{index}"
    row_h = 31
    type_w = 118
    name_w = 178
    key_w = 56
    width = type_w + name_w + key_w
    height = row_h * (len(table["columns"]) + 1)
    parts = [
        f'<mxCell id="{table_id}" value="" style={quoteattr(BASE_STYLE)} vertex="1" parent="1">'
        f'<mxGeometry x="{table["x"]}" y="{table["y"]}" width="{width}" height="{height}" as="geometry"/>'
        "</mxCell>",
        cell(f"{table_id}_header", table_id, table["name"], HEADER_STYLE, 0, 0, width, row_h),
    ]
    for row_index, (data_type, column, key) in enumerate(table["columns"], start=1):
        y = row_index * row_h
        parts.append(cell(f"{table_id}_{row_index}_type", table_id, data_type, CELL_STYLE, 0, y, type_w, row_h))
        parts.append(cell(f"{table_id}_{row_index}_name", table_id, column, CELL_STYLE, type_w, y, name_w, row_h))
        parts.append(cell(f"{table_id}_{row_index}_key", table_id, key, KEY_STYLE, type_w + name_w, y, key_w, row_h))
    return "\n".join(parts)


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    tables = "\n".join(table_xml(index, table) for index, table in enumerate(TABLES, start=1))
    xml = f"""<mxfile host="app.diagrams.net" modified="2026-06-21T00:00:00.000Z" agent="Codex" version="24.7.17">
  <diagram id="payment-v30" name="Payment V30 Tables">
    <mxGraphModel dx="1422" dy="794" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1169" pageHeight="827" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
{tables}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
"""
    OUT.write_text(xml, encoding="utf-8")
    print(OUT)


if __name__ == "__main__":
    main()
