#!/usr/bin/env python3
"""Generate system module structure drawio diagram."""

import xml.etree.ElementTree as ET
from xml.dom import minidom


class DrawioBuilder:
    def __init__(self, page_width=1200, page_height=800):
        self.root = ET.Element('mxfile', {
            'host': 'draw.io', 'modified': '2026-04-28',
            'agent': 'Claude', 'version': '24.0.0', 'type': 'device',
        })
        self.diagram = ET.SubElement(self.root, 'diagram', {
            'id': 'module', 'name': '系统功能模块图',
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

    def add_group(self, x, y, w, h, label, fill_color):
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id, 'value': label,
            'style': (
                f'rounded=1;whiteSpace=wrap;html=1;fillColor={fill_color};'
                f'strokeColor=#666666;fontSize=14;fontStyle=1;fontName=SimSun;'
                f'verticalAlign=top;align=center;spacingTop=5;strokeWidth=1.5;arcSize=6;'
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
                f'strokeColor=#333333;fontSize=12;fontName=SimSun;strokeWidth=1.2;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(w), 'height': str(h), 'as': 'geometry',
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


def build_module_diagram():
    b = DrawioBuilder(page_width=1100, page_height=680)
    b.add_title(350, 5, '图3-10  系统功能模块图', 20)

    # Top group: 系统总框
    top = b.add_group(40, 50, 1020, 600, '学生创意成果NFT确权与交易系统', '#F5F5F5')

    # Sub-group 1: 用户端
    b.add_group(60, 85, 480, 545, '用户端', '#DAE8FC')
    # Row 1
    b.add_box(80, 125, 130, 40, '用户认证', '#DAE8FC')
    b.add_box(230, 125, 130, 40, '钱包管理', '#DAE8FC')
    b.add_box(380, 125, 130, 40, 'IPFS存储', '#DAE8FC')
    # Row 2
    b.add_box(80, 185, 130, 40, 'NFT铸造', '#DAE8FC')
    b.add_box(230, 185, 130, 40, 'NFT审核', '#DAE8FC')
    b.add_box(380, 185, 130, 40, '市场交易', '#DAE8FC')
    # Row 3
    b.add_box(80, 245, 130, 40, '版本控制', '#DAE8FC')
    b.add_box(230, 245, 130, 40, '粉丝代币', '#DAE8FC')
    b.add_box(380, 245, 130, 40, '通知审计', '#DAE8FC')
    # Row 4
    b.add_box(80, 305, 130, 40, '个人中心', '#DAE8FC')

    # Sub-group 2: 管理端
    b.add_group(560, 85, 480, 280, '管理端', '#FFF2CC')
    b.add_box(580, 125, 130, 40, '数据概览', '#FFF2CC')
    b.add_box(730, 125, 130, 40, '用户管理', '#FFF2CC')
    b.add_box(880, 125, 130, 40, 'NFT管理', '#FFF2CC')
    b.add_box(580, 185, 130, 40, '订单管理', '#FFF2CC')
    b.add_box(730, 185, 130, 40, '代币管理', '#FFF2CC')
    b.add_box(880, 185, 130, 40, '系统设置', '#FFF2CC')

    # Sub-group 3: 后端服务
    b.add_group(560, 385, 480, 120, '后端服务', '#D5E8D4')
    b.add_box(580, 425, 130, 40, 'Spring Boot', '#D5E8D4')
    b.add_box(730, 425, 130, 40, 'Web3j', '#D5E8D4')
    b.add_box(880, 425, 130, 40, 'JWT认证', '#D5E8D4')

    # Sub-group 4: 区块链合约
    b.add_group(60, 380, 480, 120, '区块链合约', '#E1D5E7')
    b.add_box(80, 420, 140, 40, 'NFTAsset\n(ERC-721)', '#E1D5E7')
    b.add_box(240, 420, 140, 40, 'MicroMarket\n(交易合约)', '#E1D5E7')
    b.add_box(400, 420, 110, 40, 'FanToken\n(ERC-20)', '#E1D5E7')

    # Sub-group 5: 存储层
    b.add_group(60, 520, 980, 110, '存储层', '#FFE6CC')
    b.add_box(80, 560, 200, 40, 'MySQL（业务数据）', '#FFE6CC')
    b.add_box(310, 560, 200, 40, 'Redis（会话/缓存）', '#FFE6CC')
    b.add_box(540, 560, 220, 40, 'MinIO（文件缓冲层）', '#FFE6CC')
    b.add_box(790, 560, 220, 40, 'IPFS（不可变存证层）', '#FFE6CC')

    b.save('/Users/zhm/Desktop/user-service/diagrams/3-10-system-module.drawio')
    print('图3-10 系统功能模块图 saved.')


if __name__ == '__main__':
    build_module_diagram()