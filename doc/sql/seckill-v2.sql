/*
Navicat MySQL Data Transfer

Source Server         : local-mysql
Source Server Version : 80028
Source Host           : localhost:3306
Source Database       : seckill

Target Server Type    : MYSQL
Target Server Version : 80028
File Encoding         : 65001

Date: 2022-05-08 23:16:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_goods
-- ----------------------------
DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `goods_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `goods_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品标题',
  `goods_img` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品图片',
  `goods_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '商品详情',
  `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品价格',
  `goods_stock` int DEFAULT '0' COMMENT '商品库存，-1表示没有限制',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_goods
-- ----------------------------
INSERT INTO `t_goods` VALUES ('1', '小米手机', '小米', '/images/xm.png', 0xE5B08FE7B1B3E6898BE69CBAE7A792E69D80, '2099.00', '20');
INSERT INTO `t_goods` VALUES ('2', 'iphone12', 'iphone12', '/images/iphone12.png', 0x6970686F6E653132E7A792E69D80, '4099.00', '20');
INSERT INTO `t_goods` VALUES ('3', 'iphone12pro', 'iphone12pro', '/images/iphone12pro.png', 0x6970686F6E65313270726FE7A792E69D80, '8099.00', '20');

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `delivery_addr_id` bigint DEFAULT NULL COMMENT '收获地址ID',
  `goods_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名字',
  `goods_count` int DEFAULT '0' COMMENT '商品数量',
  `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品价格',
  `order_channel` tinyint DEFAULT '0' COMMENT '1 pc,2 android, 3 ios',
  `status` tinyint DEFAULT '0' COMMENT '订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退货，5已完成',
  `create_date` datetime DEFAULT NULL COMMENT '订单创建时间',
  `pay_date` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_order
-- ----------------------------

-- ----------------------------
-- Table structure for t_seckill_goods
-- ----------------------------
DROP TABLE IF EXISTS `t_seckill_goods`;
CREATE TABLE `t_seckill_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀家',
  `stock_count` int NOT NULL COMMENT '库存数量',
  `start_date` datetime NOT NULL COMMENT '秒杀开始时间',
  `end_date` datetime NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_seckill_goods
-- ----------------------------
INSERT INTO `t_seckill_goods` VALUES ('1', '1', '1000.00', '10', '2022-05-08 11:11:00', '2022-05-14 11:11:11');
INSERT INTO `t_seckill_goods` VALUES ('2', '2', '2099.00', '10', '2022-04-15 21:55:40', '2022-04-22 21:55:44');
INSERT INTO `t_seckill_goods` VALUES ('3', '3', '4099.00', '10', '2022-05-09 11:11:11', '2022-05-10 11:11:11');

-- ----------------------------
-- Table structure for t_seckill_order
-- ----------------------------
DROP TABLE IF EXISTS `t_seckill_order`;
CREATE TABLE `t_seckill_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `seckill_uid_gid` (`user_id`,`goods_id`) USING BTREE COMMENT '用户ID+商品ID成为唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_seckill_order
-- ----------------------------

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL COMMENT '用户ID,手机号码',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'MD5(MD5(pass明文+固定salt)+salt)',
  `salt` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `head` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '头像',
  `register_date` datetime DEFAULT NULL COMMENT '注册时间',
  `last_login_date` datetime DEFAULT NULL COMMENT '最后一次登录事件',
  `login_count` int DEFAULT '0' COMMENT '登录次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES ('13000000000', 'user0', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:05:39', null, '1');
INSERT INTO `t_user` VALUES ('13000000001', 'user1', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000002', 'user2', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000003', 'user3', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000004', 'user4', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000005', 'user5', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000006', 'user6', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000007', 'user7', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000008', 'user8', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000009', 'user9', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000010', 'user10', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000011', 'user11', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000012', 'user12', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000013', 'user13', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000014', 'user14', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000015', 'user15', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000016', 'user16', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000017', 'user17', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000018', 'user18', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000019', 'user19', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000020', 'user20', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000021', 'user21', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000022', 'user22', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000023', 'user23', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000024', 'user24', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000025', 'user25', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000026', 'user26', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000027', 'user27', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000028', 'user28', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000029', 'user29', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000030', 'user30', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000031', 'user31', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000032', 'user32', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000033', 'user33', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000034', 'user34', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000035', 'user35', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000036', 'user36', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000037', 'user37', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000038', 'user38', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000039', 'user39', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000040', 'user40', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000041', 'user41', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000042', 'user42', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000043', 'user43', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000044', 'user44', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000045', 'user45', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000046', 'user46', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000047', 'user47', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000048', 'user48', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000049', 'user49', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000050', 'user50', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000051', 'user51', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000052', 'user52', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000053', 'user53', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000054', 'user54', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000055', 'user55', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000056', 'user56', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000057', 'user57', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000058', 'user58', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000059', 'user59', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000060', 'user60', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000061', 'user61', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000062', 'user62', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000063', 'user63', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000064', 'user64', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000065', 'user65', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000066', 'user66', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000067', 'user67', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000068', 'user68', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000069', 'user69', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000070', 'user70', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000071', 'user71', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000072', 'user72', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000073', 'user73', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000074', 'user74', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000075', 'user75', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000076', 'user76', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000077', 'user77', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000078', 'user78', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000079', 'user79', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000080', 'user80', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000081', 'user81', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000082', 'user82', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000083', 'user83', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000084', 'user84', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000085', 'user85', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000086', 'user86', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000087', 'user87', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000088', 'user88', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000089', 'user89', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000090', 'user90', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000091', 'user91', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000092', 'user92', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000093', 'user93', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000094', 'user94', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000095', 'user95', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000096', 'user96', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000097', 'user97', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000098', 'user98', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('13000000099', 'user99', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', null, '2022-05-08 23:08:49', null, '1');
INSERT INTO `t_user` VALUES ('18612345678', '依依', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', '1', '2022-04-13 21:34:29', '2022-04-14 21:34:33', '0');
INSERT INTO `t_user` VALUES ('18712345678', '丫丫', 'e5d22cfc746c7da8da84e0a996e0fffa', '1a2b3c4d', '1', '2022-04-13 21:34:29', '2022-04-14 21:34:33', '0');
