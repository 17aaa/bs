#!/usr/bin/env python3
"""Generate Chen-style ERD drawio files for the thesis.
All 13 tables from the actual codebase, with exact field names.
"""

import xml.etree.ElementTree as ET
from xml.dom import minidom
import math


class DrawioBuilder:
    def __init__(self, page_width=2400, page_height=1400):
        self.root = ET.Element('mxfile', {
            'host': 'draw.io',
            'modified': '2026-04-27',
            'agent': 'Claude',
            'version': '24.0.0',
            'type': 'device',
        })
        self.diagram = ET.SubElement(self.root, 'diagram', {
            'id': 'er-diagram',
            'name': 'E-R图',
        })
        self.mxGraphModel = ET.SubElement(self.diagram, 'mxGraphModel', {
            'dx': '1422', 'dy': '762', 'grid': '1', 'gridSize': '10',
            'guides': '1', 'tooltips': '1', 'connect': '1', 'arrows': '1',
            'fold': '1', 'page': '1', 'pageScale': '1',
            'pageWidth': str(page_width), 'pageHeight': str(page_height),
            'math': '0', 'shadow': '0',
        })
        self.root_cell = ET.SubElement(self.mxGraphModel, 'root')
        ET.SubElement(self.root_cell, 'mxCell', {'id': '0'})
        ET.SubElement(self.root_cell, 'mxCell', {'id': '1', 'parent': '0'})
        self.cell_id = 1

    def next_id(self):
        self.cell_id += 1
        return str(self.cell_id)

    def add_entity(self, x, y, name, width=140, height=50):
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'value': name,
            'style': (
                'shape=rectangle;whiteSpace=wrap;html=1;fillColor=#FFFFFF;'
                'strokeColor=#000000;fontSize=16;fontStyle=1;fontName=SimSun;'
                'strokeWidth=2;arcSize=0;rounded=0;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(width), 'height': str(height), 'as': 'geometry',
        })
        return cell_id

    def add_attribute(self, x, y, name, is_pk=False, is_fk=False, width=110, height=32):
        cell_id = self.next_id()
        style = (
            'shape=ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFFF;'
            'strokeColor=#000000;fontSize=12;fontName=SimSun;strokeWidth=1.5;'
        )
        if is_pk:
            style += 'fontStyle=5;'  # bold + underline for PK
        elif is_fk:
            style += 'fontStyle=2;dashed=1;'  # italic + dashed for FK
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'value': name,
            'style': style,
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(width), 'height': str(height), 'as': 'geometry',
        })
        return cell_id

    def add_relationship(self, x, y, name, width=100, height=60):
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'value': name,
            'style': (
                'shape=rhombus;whiteSpace=wrap;html=1;fillColor=#E8F4FD;'
                'strokeColor=#000000;fontSize=14;fontStyle=1;fontName=SimSun;strokeWidth=2;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(width), 'height': str(height), 'as': 'geometry',
        })
        return cell_id

    def add_line(self, source_id, target_id, label='', cardinality_near_source='', cardinality_near_target=''):
        cell_id = self.next_id()
        style = 'endArrow=none;html=1;strokeColor=#000000;strokeWidth=1.5;fontSize=14;fontName=SimSun;'
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id, 'value': label, 'style': style,
            'edge': '1', 'source': source_id, 'target': target_id, 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
        if cardinality_near_source:
            sid = self.next_id()
            sc = ET.SubElement(self.root_cell, 'mxCell', {
                'id': sid, 'value': cardinality_near_source,
                'style': 'edgeLabel;html=1;align=center;verticalAlign=middle;resizable=0;points=[];fontSize=15;fontStyle=1;fontName=SimSun;labelBackgroundColor=#FFFFFF;',
                'vertex': '1', 'connectable': '0', 'parent': cell_id,
            })
            ET.SubElement(sc, 'mxGeometry', {'x': '-0.8', 'y': '0', 'relative': '1', 'as': 'geometry'})
        if cardinality_near_target:
            tid = self.next_id()
            tc = ET.SubElement(self.root_cell, 'mxCell', {
                'id': tid, 'value': cardinality_near_target,
                'style': 'edgeLabel;html=1;align=center;verticalAlign=middle;resizable=0;points=[];fontSize=15;fontStyle=1;fontName=SimSun;labelBackgroundColor=#FFFFFF;',
                'vertex': '1', 'connectable': '0', 'parent': cell_id,
            })
            ET.SubElement(tc, 'mxGeometry', {'x': '0.8', 'y': '0', 'relative': '1', 'as': 'geometry'})
        return cell_id

    def add_title(self, x, y, text, font_size=20):
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id, 'value': text,
            'style': f'text;html=1;align=center;verticalAlign=middle;resizable=0;points=[];autosize=1;strokeColor=none;fillColor=none;fontSize={font_size};fontStyle=1;fontName=SimSun;',
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': '400', 'height': '40', 'as': 'geometry',
        })
        return cell_id

    def save(self, filepath):
        xml_str = ET.tostring(self.root, encoding='unicode')
        pretty = minidom.parseString(xml_str).toprettyxml(indent='  ')
        lines = [l for l in pretty.split('\n') if l.strip()]
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines))


# ============================================================
# 13 tables with exact fields from entity classes
# ============================================================

ENTITIES = {
    '用户': {
        'table': 'users',
        'pk': '用户ID',
        'attrs': [
            ('用户名', False), ('密码', False), ('邮箱', False), ('手机号', False),
            ('头像', False), ('学校', False), ('专业', False), ('简介', False),
            ('状态', False), ('封禁原因', False), ('封禁时间', False),
            ('平台余额', False), ('创建时间', False),
        ],
    },
    '角色': {
        'table': 'user_roles',
        'pk': '角色ID',
        'attrs': [
            ('用户ID', True), ('角色名', False), ('创建时间', False), ('更新时间', False),
        ],
    },
    '钱包': {
        'table': 'wallets',
        'pk': '钱包ID',
        'attrs': [
            ('用户ID', True), ('钱包地址', False), ('密钥路径', False),
            ('是否绑定', False), ('绑定时间', False), ('创建时间', False), ('更新时间', False),
        ],
    },
    'NFT资产': {
        'table': 'nft_assets',
        'pk': '资产ID',
        'attrs': [
            ('Token ID', False), ('合约地址', False), ('名称', False), ('描述', False),
            ('分类', False), ('图片URL', False), ('元数据哈希', False),
            ('当前版本', False), ('拥有者地址', False), ('创作者地址', False),
            ('版税费率', False), ('版税接收者', False), ('状态', False),
            ('审核状态', False), ('创建时间', False), ('更新时间', False),
        ],
    },
    '版本历史': {
        'table': 'nft_versions',
        'pk': '版本ID',
        'attrs': [
            ('资产ID', True), ('版本号', False), ('元数据哈希', False),
            ('IPFS URI', False), ('修改说明', False), ('更新者地址', False),
            ('交易哈希', False), ('区块号', False), ('创建时间', False),
        ],
    },
    '审核记录': {
        'table': 'nft_reviews',
        'pk': '审核ID',
        'attrs': [
            ('资产ID', True), ('审核人ID', False), ('审核状态', False),
            ('审核意见', False), ('驳回原因', False),
            ('审核时间', False), ('创建时间', False),
        ],
    },
    '铸造任务': {
        'table': 'mint_tasks',
        'pk': '任务ID',
        'attrs': [
            ('用户ID', True), ('创作者地址', False), ('名称', False),
            ('描述', False), ('分类', False), ('图片URL', False),
            ('元数据URL', False), ('版税费率', False), ('当前步骤', False),
            ('步骤状态', False), ('进度(%)', False), ('错误信息', False),
            ('NFT资产ID', True), ('交易哈希', False), ('回滚状态', False),
            ('重试次数', False), ('创建时间', False), ('更新时间', False),
        ],
    },
    '交易订单': {
        'table': 'market_orders',
        'pk': '订单ID',
        'attrs': [
            ('卖家地址', False), ('买家地址', False), ('NFT合约', False),
            ('Token ID', False), ('资产ID', True), ('订单类型', False),
            ('价格', False), ('起始价', False), ('保留价', False),
            ('支付代币', False), ('结束时间', False), ('版税费率', False),
            ('版税接收者', False), ('最终价格', False), ('状态', False),
            ('交易哈希', False), ('创建时间', False), ('更新时间', False),
        ],
    },
    '粉丝代币': {
        'table': 'fan_tokens',
        'pk': '代币ID',
        'attrs': [
            ('代币地址', False), ('项目ID', False), ('创作者地址', False),
            ('名称', False), ('符号', False), ('总供应量', False),
            ('公募剩余量', False), ('公募价格', False), ('公募激活', False),
            ('合约地址', False), ('状态', False), ('创建时间', False),
        ],
    },
    '质押记录': {
        'table': 'stake_records',
        'pk': '记录ID',
        'attrs': [
            ('用户地址', False), ('代币地址', False), ('质押数量', False),
            ('奖励收益', False), ('余额', False), ('最后更新时间', False),
            ('创建时间', False), ('更新时间', False),
        ],
    },
    'IPFS文件': {
        'table': 'ipfs_file_record',
        'pk': '文件ID',
        'attrs': [
            ('CID', False), ('文件名', False), ('文件类型', False),
            ('文件大小', False), ('MIME类型', False), ('是否固定', False),
            ('固定时间', False), ('用户ID', False), ('拥有者地址', False),
            ('资产ID', False), ('描述', False), ('标签', False),
            ('访问次数', False), ('最后访问时间', False),
            ('状态', False), ('创建时间', False), ('更新时间', False),
        ],
    },
    '交易通知': {
        'table': 'transaction_notifications',
        'pk': '通知ID',
        'attrs': [
            ('用户ID', True), ('通知类型', False), ('订单ID', False),
            ('NFT资产ID', False), ('NFT名称', False), ('NFT图片', False),
            ('金额', False), ('交易对方地址', False), ('交易哈希', False),
            ('是否已读', False), ('创建时间', False),
        ],
    },
    '审计日志': {
        'table': 'audit_logs',
        'pk': '日志ID',
        'attrs': [
            ('用户ID', True), ('用户名', False), ('操作类型', False),
            ('所属模块', False), ('描述', False), ('资源类型', False),
            ('资源ID', False), ('IP地址', False), ('请求方法', False),
            ('请求路径', False), ('状态', False), ('错误信息', False),
            ('创建时间', False),
        ],
    },
}


def layout_attributes(cx, cy, pk_name, attrs, radius=200):
    """Calculate positions for attributes around an entity center.
    PK at top, FK attributes slightly offset, regular attributes in circle.
    """
    positions = []
    # PK at top
    positions.append((cx - 55, cy - radius - 20, pk_name, True, False))
    # Other attributes in a circle below
    n = len(attrs)
    # Spread from -150° to 150° (bottom semicircle + sides)
    for i, (attr_name, is_fk) in enumerate(attrs):
        if n <= 6:
            angle = math.pi / 2 + (math.pi * 2 * i / n)
        else:
            # Spread over 300 degrees, starting from just right of top
            angle = -math.pi / 6 + (math.pi * 5 / 3 * i / n)
        ax = cx + radius * math.cos(angle) - 55
        ay = cy + radius * math.sin(angle) - 16
        positions.append((ax, ay, attr_name, False, is_fk))
    return positions


# ============================================================
# Main ER Diagram
# ============================================================

def build_main_er():
    b = DrawioBuilder(page_width=1600, page_height=1000)
    b.diagram.set('name', 'E-R总图')

    b.add_title(550, 10, '图3-15  系统E-R图', 22)

    # User domain (left)
    users = b.add_entity(80, 200, '用户', 100, 50)
    roles = b.add_entity(20, 400, '角色', 100, 50)
    wallets = b.add_entity(160, 400, '钱包', 100, 50)

    # NFT domain (center)
    nft = b.add_entity(460, 200, 'NFT资产', 120, 50)
    versions = b.add_entity(300, 400, '版本历史', 120, 50)
    reviews = b.add_entity(470, 400, '审核记录', 120, 50)
    mint = b.add_entity(630, 400, '铸造任务', 120, 50)
    ipfs = b.add_entity(380, 580, 'IPFS文件', 120, 50)

    # Trading domain (right)
    orders = b.add_entity(850, 200, '交易订单', 120, 50)
    fantoken = b.add_entity(700, 600, '粉丝代币', 120, 50)
    stake = b.add_entity(900, 600, '质押记录', 120, 50)

    # Bottom
    notif = b.add_entity(850, 440, '交易通知', 130, 50)
    audit = b.add_entity(80, 680, '审计日志', 120, 50)

    # ---- Relationships ----
    # User domain
    r1 = b.add_relationship(50, 280, '拥有', 80, 50)
    r2 = b.add_relationship(190, 280, '绑定', 80, 50)
    r3 = b.add_relationship(250, 150, '创建', 80, 50)
    r4 = b.add_relationship(250, 300, '发起', 80, 50)
    r_review = b.add_relationship(250, 430, '审核', 80, 50)

    # NFT domain
    r5 = b.add_relationship(360, 280, '迭代', 80, 50)
    r6 = b.add_relationship(530, 280, '被审核', 80, 50)
    r7 = b.add_relationship(660, 150, '交易', 80, 50)
    r8 = b.add_relationship(380, 460, '关联', 80, 50)
    r_mint_out = b.add_relationship(580, 480, '产出', 80, 50)
    r_fantoken = b.add_relationship(560, 430, '发行代币', 100, 50)

    # Trading domain
    r9 = b.add_relationship(780, 620, '质押', 80, 50)
    r_notif = b.add_relationship(870, 320, '触发', 80, 50)

    # User -> bottom
    r10 = b.add_relationship(100, 530, '接收', 80, 50)
    r11 = b.add_relationship(300, 530, '产生', 80, 50)

    # ---- Connections ----
    # Chen ERD: cardinality labels go near the entity they describe
    # entity1 --[card1]-- diamond --[card2]-- entity2
    # card1 on the entity1→diamond line (near source=entity1)
    # card2 on the diamond→entity2 line (near target=entity2)

    # User domain: 用户1--拥有--N角色, 用户1--绑定--1钱包
    b.add_line(users, r1, '', cardinality_near_source='1'); b.add_line(r1, roles, '', cardinality_near_target='N')
    b.add_line(users, r2, '', cardinality_near_source='1'); b.add_line(r2, wallets, '', cardinality_near_target='1')
    b.add_line(users, r3, '', cardinality_near_source='1'); b.add_line(r3, nft, '', cardinality_near_target='N')
    b.add_line(users, r4, '', cardinality_near_source='1'); b.add_line(r4, mint, '', cardinality_near_target='N')
    # User -> NftReviews (admin reviews)
    b.add_line(users, r_review, '', cardinality_near_source='1'); b.add_line(r_review, reviews, '', cardinality_near_target='N')

    # NFT domain: NFT资产1--迭代--N版本历史, etc.
    b.add_line(nft, r5, '', cardinality_near_source='1'); b.add_line(r5, versions, '', cardinality_near_target='N')
    b.add_line(nft, r6, '', cardinality_near_source='1'); b.add_line(r6, reviews, '', cardinality_near_target='N')
    b.add_line(nft, r7, '', cardinality_near_source='1'); b.add_line(r7, orders, '', cardinality_near_target='N')
    b.add_line(nft, r8, '', cardinality_near_source='1'); b.add_line(r8, ipfs, '', cardinality_near_target='N')
    # MintTask N--产出--1 NftAsset
    b.add_line(mint, r_mint_out, '', cardinality_near_source='N'); b.add_line(r_mint_out, nft, '', cardinality_near_target='1')
    # NftAsset 1--发行代币--1 FanToken
    b.add_line(nft, r_fantoken, '', cardinality_near_source='1'); b.add_line(r_fantoken, fantoken, '', cardinality_near_target='1')

    # FanToken 1--质押--N StakeRecord
    b.add_line(fantoken, r9, '', cardinality_near_source='1'); b.add_line(r9, stake, '', cardinality_near_target='N')

    # MarketOrder 1--触发--N TransactionNotification
    b.add_line(orders, r_notif, '', cardinality_near_source='1'); b.add_line(r_notif, notif, '', cardinality_near_target='N')

    # User -> notifications & audit
    b.add_line(users, r10, '', cardinality_near_source='1'); b.add_line(r10, notif, '', cardinality_near_target='N')
    b.add_line(users, r11, '', cardinality_near_source='1'); b.add_line(r11, audit, '', cardinality_near_target='N')

    b.save('/Users/zhm/Desktop/user-service/diagrams/3-15-er-diagram.drawio')
    print('Main ER diagram saved.')


# ============================================================
# Entity-Attribute Diagrams — one drawio per entity
# ============================================================

def build_single_entity_attr(name, info, filepath, page_width=1400, page_height=1000):
    """Build a single entity-attribute diagram."""
    b = DrawioBuilder(page_width=page_width, page_height=page_height)
    b.diagram.set('name', f'{name}({info["table"]})')

    cx = page_width // 2 - 80
    cy = page_height // 2 - 30

    # Entity rectangle
    eid = b.add_entity(cx, cy, f'{name}\n({info["table"]})', 180, 60)

    # Attributes
    n_attrs = len(info['attrs']) + 1  # +1 for PK
    radius = 220 if n_attrs > 10 else 180 if n_attrs > 6 else 140
    positions = layout_attributes(cx + 90, cy + 30, info['pk'], info['attrs'], radius=radius)

    for ax, ay, attr_name, is_pk, is_fk in positions:
        w = max(100, len(attr_name) * 12 + 20)
        aid = b.add_attribute(ax, ay, attr_name, is_pk=is_pk, is_fk=is_fk, width=w, height=30)
        b.add_line(eid, aid)

    b.save(filepath)
    print(f'  {name} saved: {filepath}')


def build_all_attribute_diagrams():
    """Generate one drawio per entity."""
    for i, (name, info) in enumerate(ENTITIES.items()):
        # Determine page size based on attribute count
        n = len(info['attrs']) + 1
        if n > 14:
            pw, ph = 1800, 1200
        elif n > 10:
            pw, ph = 1600, 1100
        else:
            pw, ph = 1400, 1000

        slug = info['table']
        fig_num = 16 + i
        filepath = f'/Users/zhm/Desktop/user-service/diagrams/3-{fig_num}-er-attr-{slug}.drawio'
        build_single_entity_attr(name, info, filepath, pw, ph)


if __name__ == '__main__':
    build_main_er()
    print()
    print('Generating entity-attribute diagrams...')
    build_all_attribute_diagrams()
    print()
    print('All drawio files generated!')