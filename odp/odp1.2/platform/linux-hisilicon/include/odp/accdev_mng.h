/* Copyright (c) 2014, Linaro Limited
 * All rights reserved.
 *
 * SPDX-License-Identifier:     BSD-3-Clause
 */

/**
 * @file
 *
 * ODP acc
 */

#ifndef ODP_API_ACC_DEV_MNG_H_
#define ODP_API_ACC_DEV_MNG_H_

#ifdef __cplusplus
extern "C" {
#endif

#define ODP_VERSION_LEN 16

struct odp_acc_pf_attr {
	uint32_t pf_id;                            /* �����豸��� */
	char	 firmwareversion[ODP_VERSION_LEN]; /* �̼��汾 */
	char	 hardwareversion[ODP_VERSION_LEN]; /* Ӳ���汾 */
};

/*****************************************************************************
   Function     : odp_acc_get_pf_dev_names
   Description  : ��ȡָ�����͵������豸�б�
   Input        : enum odp_acc_dev_type dev_type:�����豸����
                uint32_t *dev_num: ��Ҫ��ѯ�������豸����
   Output       : char *dev_name[]:�����豸���б�
                uint32_t *dev_num:ʵ�������豸����
   Return       :
   Create By    :
   Modification :
   Restriction  :
*****************************************************************************/
int odp_acc_get_pf_dev_names(enum odp_acc_dev_type dev_type, char *dev_name[], uint32_t *dev_num);

/*****************************************************************************
   Function     : odp_acc_get_vf_dev_names
   Description  : ��ȡָ�������豸�������豸�б�
   Input        : char *dev_name:�����豸��
                uint32_t *vf_num: ��Ҫ��ѯ�������豸����
   Output       : char *vf_name[]:�����豸���б�
                uint32_t *vf_num:ʵ�������豸����
   Return       :
   Create By    :
   Modification :
   Restriction  :
*****************************************************************************/
int odp_acc_get_vf_dev_names(char *dev_name, char *vf_name[], uint32_t *vf_num);

/*****************************************************************************
   Function     : odp_acc_get_pf_dev_attr
   Description  : ��ȡ�����豸��Ϣ
   Input        : char *dev_name
   Output       : struct odp_acc_pf_attr *attr:�豸��Ϣ
   Return       :
   Create By    :
   Modification :
   Restriction  :
*****************************************************************************/
int odp_acc_get_pf_dev_attr(char *dev_name, struct odp_acc_pf_attr *attr);

#ifdef __cplusplus
}
#endif
#endif
