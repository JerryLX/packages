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



#ifndef ODP_API_ACC_DEV_H_
#define ODP_API_ACC_DEV_H_

#ifdef __cplusplus
extern "C" {
#endif


enum odp_acc_session_type
{
    ODP_ACC_CRYPTO_SESSION_TYPE,
    ODP_ACC_RSA_SESSION_TYPE,
    ODP_ACC_DSA_SESSION_TYPE,
    ODP_ACC_DH_SESSION_TYPE,
    ODP_ACC_RBG_SESSION_TYPE,
    ODP_ACC_KEY_SESSION_TYPE,
    ODP_ACC_PRIME_SESSION_TYPE,
};




struct odp_acc_op_params {
    enum odp_acc_session_type type;
    union
    {
        odp_crypto_op_params_t *params;
        odp_rsa_op_params_t *rsa_params;
        odp_dsa_op_params_t *dsa_params;
        odp_dh_op_params_t *dh_params;
        odp_rbg_op_params_t *rbg_params;
        odp_key_op_params_t *key_params;
        odp_prime_op_params_t *prime_params;
    };
};

union odp_acc_op_result {
	odp_acc_session_t session;
    odp_crypto_op_result_t result;
    odp_rsa_op_result_t rsa_result;
    odp_dsa_op_result_t dsa_result;
    odp_dh_op_result_t dh_result;
    odp_rbg_op_result_t rbg_result;
    odp_key_op_result_t key_result;
    odp_prime_op_result_t prime_result;
};

struct odp_acc_session_params_t {
    enum odp_acc_session_type type;
    union
    {
        odp_crypto_session_params_t *params;
        odp_rsa_session_params_t *rsa_params;
        odp_dsa_session_params_t *dsa_params;
        odp_dh_session_params_t *dh_params;
        odp_rbg_session_params_t *rbg_params;
        odp_key_session_params_t *key_params;
        odp_prime_session_params_t *prime_params;
    };
};


/* �����豸���� */
enum odp_acc_dev_type
{
    ODP_ACC_DEV_CRYPTO_TYPE,       /* �ӽ����豸                 */
    ODP_ACC_DEV_DC_TYPE,           /* ����ѹ���豸               */
    ODP_ACC_DEV_TYPE_INVALID
};


enum odp_acc_dev_event
{
    ODP_ACC_DEV_EVENT_RESTARTING,     /* ��λ֮ǰ֪ͨ�¼� */
    ODP_ACC_DEV_EVENT_RESTARTED       /* ��λ֮��֪ͨ�¼� */
};



/* �����豸�������� */
struct odp_acc_caps
{
    uint32_t sessions_num;            /* �Ự���� */

    union
    {
        struct
        {
            uint32_t cipher_flow_caps[ODP_CIPHER_ALG_NUM];      /* ÿ���㷨֧�ֵ���������λmpps */
            uint32_t auth_flow_caps[ODP_AUTH_ALG_NUM];          /* ÿ���㷨֧�ֵ���������λmpps */
            uint32_t asym_flow_caps[1];                         /* ÿ���㷨֧�ֵ���������λops  */
            uint64_t cipher_feature;                      /* �ԳƼӽ���֧�ֵ��㷨odp_cipher_alg(1��bitλ��Ӧһ���㷨)   */
            uint64_t auth_feature;                        /* ��֤֧�ֵ��㷨odp_auth_alg(1��bitλ��Ӧһ���㷨)           */
            uint64_t asym_feature;                        /* ֧�ֵķǶԳƼӽ����㷨(1��bitλ��Ӧһ���㷨)               */
        }cy;
        struct
        {
            uint32_t dc_flow_caps[1];                      /* ÿ���㷨֧�ֵ���������λmpps */
            uint64_t dc_feature;                           /* ����ѹ��֧�ֵ��㷨  */
        }dc;
    };
};


/* ��ȡ�����豸ͳ�� */
struct odp_acc_dev_stat
{
    uint64_t  num_sessions_competed;                  /* �Ự�����ɹ����� */
    uint64_t  num_sessions_competed_errors;           /* �Ự����ʧ�ܴ��� */
    uint64_t  num_sessions_removed;                   /* �Ựɾ���ɹ����� */
    uint64_t  num_sessions_removed_errors;            /* �Ựɾ��ʧ�ܴ��� */
    uint64_t  num_op_request_completed;               /* ��������ɹ����� */
    uint64_t  num_op_request_completed_errors;        /* ��������ʧ�ܴ��� */
    uint64_t  num_op_respond_completed;               /* ����Ӧ��ɹ����� */
    uint64_t  num_op_respond_completed_errors;        /* ����Ӧ��ʧ�ܴ��� */
};


void odp_acc_dev_lock(odp_acc_dev_handle dev_handle);
void odp_acc_dev_unlock(odp_acc_dev_handle dev_handle);
/*****************************************************************************
 Function     : odp_acc_get_dev_names
 Description  : ��ȡָ�����͵��豸�б�
 Input        : enum odp_acc_dev_type dev_type:�����豸����
                uint32_t *dev_num: ��Ҫ��ѯ���豸����
 Output       : char *dev_name[]:�豸���б�
                uint32_t *dev_num:ʵ���豸����
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_dev_names(enum odp_acc_dev_type dev_type, char *dev_name[], uint32_t *dev_num);


/*****************************************************************************
 Function     : odp_acc_get_dev_caps
 Description  : ��ȡ�����豸����
 Input        : char *dev_name
 Output       : struct odp_acc_caps *caps:�豸����
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_dev_caps(char *dev_name, struct odp_acc_caps *caps);



/*****************************************************************************
 Function     : odp_acc_get_dev_handle
 Description  : ��ȡ�����豸handle
 Input        : char *dev_name:�豸��
 Output       : NA
 Return       : odp_acc_dev_handle dev_handle:�豸���(0�����512���ǷǷ�ֵ)
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
odp_acc_dev_handle odp_acc_get_dev_handle(char *dev_name);



/*****************************************************************************
 Function     : odp_acc_get_dev_numa_node
 Description  : ��ȡ�����豸����numa_node
 Input        : char *dev_name:�豸��
 Output       : int *numa_node:numa�ڵ�
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_dev_numa_node(char *dev_name, int *numa_node);


/*****************************************************************************
 Function     : odp_acc_get_dev_type
 Description  : ��ȡ�����豸����
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : enum odp_acc_dev_type *dev_type:�豸����
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_dev_type(odp_acc_dev_handle dev_handle, enum odp_acc_dev_type *dev_type);




/*****************************************************************************
 Function     : odp_acc_open_dev
 Description  : �򿪼����豸
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : NA
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_open_dev(odp_acc_dev_handle dev_handle);


/*****************************************************************************
 Function     : odp_acc_close_dev
 Description  : �رռ����豸
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : NA
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_close_dev(odp_acc_dev_handle dev_handle);


/*****************************************************************************
 Function     : odp_acc_reset_dev
 Description  : ���������豸
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : NA
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_reset_dev(odp_acc_dev_handle dev_handle);


/*****************************************************************************
 Function     : odp_acc_get_dev_status
 Description  : ��ȡ�����豸����״̬
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : uint32_t *status(0:����������:�쳣)
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_dev_status(odp_acc_dev_handle dev_handle, uint32_t *status);



/*****************************************************************************
 Function     : odp_acc_get_open_status
 Description  : ��ȡ�����豸��״̬
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : uint32_t *status(0:�رգ�1:��)
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_open_status(odp_acc_dev_handle dev_handle, uint32_t *status);



/*****************************************************************************
 Function     : odp_acc_get_dev_stat
 Description  : ��ȡ�����豸ͳ��
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       : struct odp_acc_dev_stat *stat:ͳ����Ϣ
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_get_dev_stat(odp_acc_dev_handle dev_handle, struct odp_acc_dev_stat *stat);





/*****************************************************************************
 Function     : odp_acc_clear_dev_stat
 Description  : ��������豸ͳ��
 Input        : odp_acc_dev_handle dev_handle:�豸���
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_acc_clear_dev_stat(odp_acc_dev_handle dev_handle);








/*****************************************************************************
 Function     : odp_acc_dev_fault_notification_reg
 Description  : �����豸����֪ͨע�ắ��
 Input        : odp_acc_dev_handle dev_handle:�豸���
                const odp_acc_dev_fault_cb_func cb_func:�ص�����
                void *callback_tag:�ص�����
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
typedef void (*odp_acc_dev_fault_cb_func)(odp_acc_dev_handle dev_handle, void *callback_tag, enum odp_acc_dev_event event);
void odp_acc_dev_fault_notification_reg(odp_acc_dev_handle dev_handle, const odp_acc_dev_fault_cb_func cb_func, void *callback_tag);




/*****************************************************************************
 Function     : odp_acc_dev_recv_burst
 Description  : �ڻص�����δע������µĽ��մ�����
 Input        : odp_acc_dev_handle dev_handle:�豸���
                union odp_acc_op_result result[]:���
                uint32_t pkts_num:�������
 Output       : NA
 Return       : ʵ�ʽ��յĽ������
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
uint32_t odp_acc_dev_recv_burst(odp_acc_dev_handle dev_handle, union odp_acc_op_result result[], uint32_t pkts_num);



/*****************************************************************************
 Function     : odp_acc_result_to_compl
 Description  : �����д���¼���Ӧλ��
 Input        : odp_event_t completion_event:�¼�
                union odp_acc_op_result *result:���
 Output       : NA
 Return       : NA
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
void odp_acc_result_to_compl(odp_event_t completion_event,
			union odp_acc_op_result *result);


#ifdef __cplusplus
}
#endif

#endif

