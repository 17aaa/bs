#!/usr/bin/env python3
"""Generate system architecture drawio diagram."""

import xml.etree.ElementTree as ET
from xml.dom import minidom


class DrawioBuilder:
    def __init__(self, page_width=1200, page_height=800):
        self.root = ET.Element('mxfile', {
            'host': 'draw.io', 'modified': '2026-04-28',
            'agent': 'Claude', 'version': '24.0.0', 'type': 'device',
        })
        self.diagram = ET.SubElement(self.root, 'diagram', {
            'id': 'arch', 'name': '系统架构图',
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

    def add_layer(self, x, y, w, h, label, fill_color):
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id, 'value': label,
            'style': (
                f'rounded=1;whiteSpace=wrap;html=1;fillColor={fill_color};'
                f'strokeColor=#666666;fontSize=14;fontStyle=1;fontName=SimSun;'
                f'verticalAlign=top;align=left;spacingLeft=10;spacingTop=5;'
                f'strokeWidth=1.5;arcSize=8;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(w), 'height': str(h), 'as': 'geometry',
        })
        return cell_id

    def add_box(self, x, y, w, h, label, fill_color='#FFFFFF'):
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id, 'value': label,
            'style': (
                f'rounded=1;whiteSpace=wrap;html=1;fillColor={fill_color};'
                f'strokeColor=#333333;fontSize=13;fontName=SimSun;strokeWidth=1.5;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(w), 'height': str(h), 'as': 'geometry',
        })
        return cell_id

    def add_down_arrow(self, x, y1, y2, label=''):
        """Add a vertical downward arrow between layers at position x."""
        cell_id = self.next_id()
        style = (
            'endArrow=block;html=1;strokeColor=#555555;strokeWidth=2;'
            'endFill=1;fontSize=11;fontName=SimSimSun;fontColor=#555555;'
        )
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id, 'value': label, 'style': style,
            'edge': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
        # Source point
        sp = ET.SubElement(cell, 'mxPoint', {
            'x': str(x), 'y': str(y1), 'as': 'sourcePoint',
        })
        # Target point
        tp = ET.SubElement(cell, 'mxPoint', {
            'x': str(x), 'y': str(y2), 'as': 'targetPoint',
        })
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


def build_architecture():
    b = DrawioBuilder(page_width=1100, page_height=760)
    b.add_title(350, 5, '图3-9  系统架构图', 20)

    # ---- Layer 1: 前端展示层 ----
    b.add_layer(40, 50, 1020, 120, '前端展示层', '#DAE8FC')
    b.add_box(80, 90, 210, 60, '用户端\n(React / ethers.js)', '#DAE8FC')
    b.add_box(330, 90, 210, 60, '管理端\n(React / Vite)', '#DAE8FC')
    b.add_box(580, 90, 210, 60, 'MetaMask\n(浏览器钱包)', '#DAE8FC')
    b.add_box(830, 90, 180, 60, '服务端钱包\n(降级模式)', '#DAE8FC')

    # ---- Layer 2: 后端服务层 ----
    b.add_layer(40, 210, 1020, 160, '后端服务层', '#D5E8D4')
    b.add_box(80, 255, 170, 55, 'Spring Boot\n(REST API)', '#D5E8D4')
    b.add_box(280, 255, 150, 55, 'MyBatis Plus\n(ORM)', '#D5E8D4')
    b.add_box(460, 255, 150, 55, 'Web3j\n(链上交互)', '#D5E8D4')
    b.add_box(640, 255, 150, 55, 'JWT\n(身份认证)', '#D5E8D4')
    b.add_box(830, 255, 190, 55, '@Async异步铸造\n(RocketMQ审计日志)', '#D5E8D4')

    # ---- Layer 3: 数据与存储层 ----
    b.add_layer(40, 410, 1020, 120, '数据与存储层', '#FFF2CC')
    b.add_box(80, 450, 180, 55, 'MySQL\n(业务数据)', '#FFF2CC')
    b.add_box(300, 450, 180, 55, 'Redis\n(会话/缓存)', '#FFF2CC')
    b.add_box(520, 450, 200, 55, 'MinIO\n(文件缓冲层)', '#FFF2CC')
    b.add_box(760, 450, 220, 55, 'IPFS / Pinata\n(不可变存证层)', '#FFF2CC')

    # ---- Layer 4: 区块链层 ----
    b.add_layer(40, 570, 1020, 120, '区块链层', '#E1D5E7')
    b.add_box(80, 610, 200, 55, 'Polygon Amoy\n(测试网)', '#E1D5E7')
    b.add_box(320, 610, 200, 55, 'NFTAsset.sol\n(ERC-721)', '#E1D5E7')
    b.add_box(560, 610, 200, 55, 'MicroMarket.sol\n(交易合约)', '#E1D5E7')
    b.add_box(800, 610, 200, 55, 'FanToken.sol\n(ERC-20)', '#E1D5E7')

    # ---- Layer-to-layer arrows (centered, no crossing) ----
    # 前端 → 后端 (3 arrows spread across)
    b.add_down_arrow(250, 170, 210, 'HTTP / REST')
    b.add_down_arrow(550, 170, 210, 'JSON-RPC')
    b.add_down_arrow(850, 170, 210, '')

    # 后端 → 数据与存储
    b.add_down_arrow(300, 370, 410, 'SQL / 缓存')
    b.add_down_arrow(700, 370, 410, '文件读写')

    # 数据与存储 → 区块链
    b.add_down_arrow(550, 530, 570, 'JSON-RPC / 交易广播')

    b.save('/Users/zhm/Desktop/user-service/diagrams/3-09-system-architecture.drawio')
    print('图3-9 系统架构图 saved.')


if __name__ == '__main__':
    build_architecture()