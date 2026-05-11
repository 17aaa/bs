#!/usr/bin/env python3
"""Generate UML Use Case drawio files for the thesis."""

import xml.etree.ElementTree as ET
from xml.dom import minidom


class UseCaseBuilder:
    def __init__(self, page_width=1400, page_height=900):
        self.root = ET.Element('mxfile', {
            'host': 'draw.io',
            'modified': '2026-04-28',
            'agent': 'Claude',
            'version': '24.0.0',
            'type': 'device',
        })
        self.diagram = ET.SubElement(self.root, 'diagram', {
            'id': 'usecase', 'name': '用例图',
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

    def add_actor(self, x, y, name):
        """Add a UML actor (stick figure)."""
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'value': name,
            'style': (
                'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;'
                'html=1;fillColor=#FFFFFF;strokeColor=#000000;fontSize=14;'
                'fontStyle=1;fontName=SimSun;outlineConnect=0;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': '40', 'height': '60', 'as': 'geometry',
        })
        return cell_id

    def add_usecase(self, x, y, name, width=140, height=50):
        """Add a use case (ellipse)."""
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'value': name,
            'style': (
                'shape=ellipse;whiteSpace=wrap;html=1;fillColor=#E8F4FD;'
                'strokeColor=#000000;fontSize=13;fontName=SimSun;strokeWidth=1.5;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(width), 'height': str(height), 'as': 'geometry',
        })
        return cell_id

    def add_usecase_highlight(self, x, y, name, width=140, height=50):
        """Add a highlighted use case for admin."""
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'value': name,
            'style': (
                'shape=ellipse;whiteSpace=wrap;html=1;fillColor=#FFF2CC;'
                'strokeColor=#000000;fontSize=13;fontName=SimSun;strokeWidth=1.5;'
            ),
            'vertex': '1', 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {
            'x': str(x), 'y': str(y), 'width': str(width), 'height': str(height), 'as': 'geometry',
        })
        return cell_id

    def add_association(self, source_id, target_id):
        """Solid line: actor-to-usecase or usecase-to-usecase association."""
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'style': 'endArrow=none;html=1;strokeColor=#000000;strokeWidth=1.5;',
            'edge': '1', 'source': source_id, 'target': target_id, 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
        return cell_id

    def add_include(self, source_id, target_id):
        """Dashed arrow with <<include>> label. source --<<include>>--> target."""
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'style': (
                'endArrow=open;html=1;dashed=1;strokeColor=#000000;strokeWidth=1.2;'
                'fontSize=11;fontName=SimSun;fontStyle=2;'
            ),
            'edge': '1', 'source': source_id, 'target': target_id, 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
        # Label
        label_id = self.next_id()
        label = ET.SubElement(self.root_cell, 'mxCell', {
            'id': label_id,
            'value': '&lt;&lt;include&gt;&gt;',
            'style': (
                'edgeLabel;html=1;align=center;verticalAlign=middle;resizable=0;'
                'points=[];fontSize=11;fontStyle=2;fontName=SimSun;'
                'labelBackgroundColor=#FFFFFF;'
            ),
            'vertex': '1', 'connectable': '0', 'parent': cell_id,
        })
        ET.SubElement(label, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
        return cell_id

    def add_extend(self, source_id, target_id):
        """Dashed arrow with <<extend>> label. source --<<extend>>--> target."""
        cell_id = self.next_id()
        cell = ET.SubElement(self.root_cell, 'mxCell', {
            'id': cell_id,
            'style': (
                'endArrow=open;html=1;dashed=1;strokeColor=#000000;strokeWidth=1.2;'
                'fontSize=11;fontName=SimSun;fontStyle=2;'
            ),
            'edge': '1', 'source': source_id, 'target': target_id, 'parent': '1',
        })
        ET.SubElement(cell, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
        # Label
        label_id = self.next_id()
        label = ET.SubElement(self.root_cell, 'mxCell', {
            'id': label_id,
            'value': '&lt;&lt;extend&gt;&gt;',
            'style': (
                'edgeLabel;html=1;align=center;verticalAlign=middle;resizable=0;'
                'points=[];fontSize=11;fontStyle=2;fontName=SimSun;'
                'labelBackgroundColor=#FFFFFF;'
            ),
            'vertex': '1', 'connectable': '0', 'parent': cell_id,
        })
        ET.SubElement(label, 'mxGeometry', {'relative': '1', 'as': 'geometry'})
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


def build_creator_usecase():
    b = UseCaseBuilder(page_width=1400, page_height=700)
    b.diagram.set('name', '创作者用例图')
    b.add_title(450, 10, '图3-1  创作者用例图', 20)

    # Actor
    creator = b.add_actor(60, 260, '创作者')

    # Major use cases (center column)
    certify = b.add_usecase(300, 80, '作品确权', 130, 50)
    sell = b.add_usecase(300, 280, '上架出售', 130, 50)
    token = b.add_usecase(300, 480, '粉丝代币', 130, 50)

    # Sub-use cases (right column)
    # 作品确权
    upload = b.add_usecase(620, 50, '上传作品文件', 140, 45)
    fill_info = b.add_usecase(620, 120, '填写作品信息', 140, 45)
    version = b.add_usecase(620, 190, '版本更新', 120, 45)

    # 上架出售
    fixed = b.add_usecase(620, 270, '固定价格上架', 140, 45)
    auction = b.add_usecase(620, 340, '荷兰拍卖上架', 140, 45)
    cancel = b.add_usecase(620, 410, '取消上架', 120, 45)

    # 粉丝代币
    issue = b.add_usecase(620, 500, '发行代币', 120, 45)

    # Associations: actor -> major use cases
    b.add_association(creator, certify)
    b.add_association(creator, sell)
    b.add_association(creator, token)

    # <<include>>: base includes sub (base must do sub)
    b.add_include(certify, upload)
    b.add_include(certify, fill_info)
    b.add_extend(version, certify)

    b.add_extend(fixed, sell)
    b.add_extend(auction, sell)
    b.add_extend(cancel, sell)

    b.add_extend(issue, token)

    b.save('/Users/zhm/Desktop/user-service/diagrams/3-01-creator-usecase.drawio')
    print('图3-1 创作者用例图 saved.')


def build_user_usecase():
    b = UseCaseBuilder(page_width=1400, page_height=850)
    b.diagram.set('name', '用户用例图')
    b.add_title(450, 10, '图3-2  用户用例图', 20)

    # Actor
    user = b.add_actor(60, 340, '用户')

    # Major use cases
    auth = b.add_usecase(300, 100, '身份认证', 130, 50)
    trade = b.add_usecase(300, 280, '市场交易', 130, 50)
    token = b.add_usecase(300, 470, '粉丝代币', 130, 50)
    center = b.add_usecase(300, 650, '个人中心', 130, 50)

    # Sub-use cases
    # 身份认证
    metamask = b.add_usecase(620, 30, 'MetaMask签名', 140, 45)
    server_wallet = b.add_usecase(620, 100, '服务端钱包', 130, 45)
    bind_wallet = b.add_usecase(620, 170, '钱包绑定', 120, 45)

    # 市场交易
    browse = b.add_usecase(620, 240, '浏览市场', 120, 45)
    buy = b.add_usecase(620, 310, '购买NFT', 120, 45)
    settle = b.add_usecase(870, 310, '积分结算', 120, 45)
    crowd = b.add_usecase(620, 380, '参与代币公募', 150, 45)

    # 粉丝代币
    stake = b.add_usecase(620, 460, '质押代币', 120, 45)
    unstake = b.add_usecase(620, 530, '解除质押', 120, 45)
    reward = b.add_usecase(870, 530, '领取奖励', 120, 45)

    # 个人中心
    my_nft = b.add_usecase(620, 620, '查看我的NFT', 140, 45)
    notify = b.add_usecase(620, 690, '查看通知消息', 140, 45)
    profile = b.add_usecase(620, 760, '修改个人资料', 140, 45)

    # Associations
    b.add_association(user, auth)
    b.add_association(user, trade)
    b.add_association(user, token)
    b.add_association(user, center)

    # 身份认证
    b.add_extend(metamask, auth)
    b.add_extend(server_wallet, auth)
    b.add_extend(bind_wallet, auth)

    # 市场交易
    b.add_include(trade, browse)
    b.add_extend(buy, trade)
    b.add_include(buy, settle)
    b.add_extend(crowd, trade)

    # 粉丝代币
    b.add_extend(stake, token)
    b.add_extend(unstake, token)
    b.add_include(unstake, reward)

    # 个人中心
    b.add_extend(my_nft, center)
    b.add_extend(notify, center)
    b.add_extend(profile, center)

    b.save('/Users/zhm/Desktop/user-service/diagrams/3-02-user-usecase.drawio')
    print('图3-2 用户用例图 saved.')


def build_admin_usecase():
    b = UseCaseBuilder(page_width=1400, page_height=700)
    b.diagram.set('name', '管理员用例图')
    b.add_title(450, 10, '图3-3  管理员用例图', 20)

    # Actor
    admin = b.add_actor(60, 260, '管理员')

    # Major use cases
    review = b.add_usecase_highlight(300, 80, 'NFT审核', 130, 50)
    user_mgmt = b.add_usecase_highlight(300, 250, '用户管理', 130, 50)
    token_mgmt = b.add_usecase_highlight(300, 420, '代币管理', 130, 50)
    stats = b.add_usecase_highlight(300, 580, '数据统计', 130, 50)

    # Sub-use cases
    approve = b.add_usecase_highlight(620, 30, '审核通过', 120, 45)
    reject = b.add_usecase_highlight(620, 100, '审核驳回', 120, 45)
    takedown = b.add_usecase_highlight(620, 170, '下架NFT', 120, 45)

    ban = b.add_usecase_highlight(620, 240, '封禁/解封用户', 150, 45)
    role = b.add_usecase_highlight(620, 310, '调整用户角色', 140, 45)

    activate = b.add_usecase_highlight(620, 400, '激活公募', 120, 45)
    pause = b.add_usecase_highlight(620, 470, '暂停公募', 120, 45)
    end = b.add_usecase_highlight(620, 540, '结束发行', 120, 45)

    # Associations
    b.add_association(admin, review)
    b.add_association(admin, user_mgmt)
    b.add_association(admin, token_mgmt)
    b.add_association(admin, stats)

    # <<extend>>
    b.add_extend(approve, review)
    b.add_extend(reject, review)
    b.add_extend(takedown, review)

    b.add_extend(ban, user_mgmt)
    b.add_extend(role, user_mgmt)

    b.add_extend(activate, token_mgmt)
    b.add_extend(pause, token_mgmt)
    b.add_extend(end, token_mgmt)

    b.save('/Users/zhm/Desktop/user-service/diagrams/3-03-admin-usecase.drawio')
    print('图3-3 管理员用例图 saved.')


if __name__ == '__main__':
    build_creator_usecase()
    build_user_usecase()
    build_admin_usecase()
    print('\nAll use case diagrams generated!')