/*******************************************************************************
   Hisilicon network interface controller driver
   Copyright(c) 2014 - 2019 Huawei Corporation.
   This program is free software; you can redistribute it and/or modify it
   under the terms and conditions of the GNU General Public License,
   version 2, as published by the Free Software Foundation.
   This program is distributed in the hope it will be useful, but WITHOUT
   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
   more details.
   You should have received a copy of the GNU General Public License along with
   this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin St - Fifth Floor, Boston, MA 02110-1301 USA.
   The full GNU General Public License is included in this distribution in
   the file called "COPYING".
   Contact Information:TBD
*******************************************************************************/

#ifndef _HNS_UIO_ENET_H
#define _HNS_UIO_ENET_H

#include "hnae.h"
#include "hns_dsaf_mac.h"
#include "hns_dsaf_main.h"

#define NIC_MOD_VERSION     "iWareV2R2C00B961"
#define DRIVER_UIO_NAME     "hns-nic-uio"
#define NIC_UIO_SIZE        0x10000

struct char_device {
    unsigned int    major;          /*�豸�����豸��*/
    char            class_name[64]; /*�豸���͵�����*/
    char            name[64];       /*�豸�ļ�����(����·��)*/
    struct class *  dev_class;
};

struct nic_uio_device {
    struct device *     dev;
    struct hnae_handle *ae_handle;
    struct net_device  *netdev;
    struct device_node *ae_node;
    struct hnae_vf_cb  *vf_cb;
    
    unsigned int        enet_ver;
    unsigned int        port;
    unsigned int        vf_sum;
    unsigned int        vf_id;
    unsigned int        port_vf_start;  // ����˿ڵ���ʼvf��
    unsigned int        port_vf_end;    // ����˿ڵĽ���vf��
    unsigned int        q_num;
    char                mac_addr[8];

    struct uio_info     uinfo;
};

#endif /*#ifndef _HNS_UIO_ENET_H*/