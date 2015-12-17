/* Copyright (c) 2014, Linaro Limited
 * All rights reserved.
 *
 * SPDX-License-Identifier:     BSD-3-Clause
 */


/**
 * @file
 *
 * ODP rsa
 */

#ifndef ODP_API_RSA_H_
#define ODP_API_RSA_H_

#ifdef __cplusplus
extern "C" {
#endif



enum odp_rsa_private_key_rep_type
{
	ODP_RSA_PRIVATE_KEY_REP_TYPE_1 = 1,   /* RSA��һ�������õ�˽Կ */
	ODP_RSA_PRIVATE_KEY_REP_TYPE_2        /* RSA�ڶ��������õ�˽Կ */
};


typedef struct odp_rsa_private_key_rep1
{
	odp_packet_t modulus_n;           /* RSA�����㷨����ԿN */
	odp_packet_t private_exponent_d;  /* RSA�����㷨����ԿD */
}odp_rsa_private_key_rep1_t;


typedef struct odp_rsa_private_key_rep2
{
	odp_packet_t prime_1p;          /* ������1P */
	odp_packet_t prime_2q;          /* ������2Q */
    odp_packet_t exponent_1dp;      /* ��Կ1Dp  */
	odp_packet_t exponent_2dq;      /* ��Կ2Dp  */
    odp_packet_t coefficient_qinv;  /* ��ԿQInv */
}odp_rsa_private_key_rep2_t;


typedef struct odp_rsa_private_key
{
    enum odp_rsa_private_key_rep_type rep_type;      /* ������ţ���ͬ����Ŷ�Ӧ�Ų�ͬ��˽Կ���� */
    odp_rsa_private_key_rep1_t private_key_rep1;     /* ��PKCS��1 V2.1�淶�а���һ�����������RSA˽Կ */
    odp_rsa_private_key_rep2_t private_key_rep2;     /* ��PKCS��1 V2.1�淶�а��ڶ����������RSA˽Կ   */
}odp_rsa_private_key_t;


typedef struct odp_rsa_public_key
{
    odp_packet_t modulus_n;          /* RSA�����㷨�й�ԿN */
	odp_packet_t public_exponent_e;  /* RSA�����㷨�й�ԿE */
}odp_rsa_public_key_t;



typedef struct odp_rsa_op_result {
	odp_crypto_session_t session;      /**< Session handle from creation */
	odp_bool_t  ok;                  /**< Request completed successfully */
	void *ctx;                       /**< User context from request */
	odp_packet_t pkt;                /**< Output packet */
    odp_rsa_private_key_t private_key;
    odp_rsa_public_key_t public_key;
	odp_crypto_compl_status_t status;  /**< status */
} odp_rsa_op_result_t;




typedef struct odp_rsa_session_params {
    odp_acc_dev_handle dev_handle;     /**< device handle */
	enum odp_rsa_op op;                /**< Encode versus decode */
	enum odp_crypto_op_mode pref_mode;   /**< Preferred sync vs async */
	odp_queue_t compl_queue;           /**< Async mode completion event queue */
	odp_pool_t output_pool;            /**< Output buffer pool */

    void (*odp_asym_compl_pfn)(odp_crypto_session_t session, odp_rsa_op_result_t *result); /* ע��ӽ��ܻص������� */
} odp_rsa_session_params_t;



typedef struct odp_rsa_op_params {
	odp_crypto_session_t session;     /**< Session handle from creation */
	void *ctx;                      /**< User context */
	odp_packet_t pkt;               /**< Input packet buffer */
	odp_packet_t out_pkt;           /**< Output packet buffer */

    odp_rsa_private_key_t private_key;
    odp_rsa_public_key_t public_key;
} odp_rsa_op_params_t;


/*****************************************************************************
 Function     : odp_rsa_session_create
 Description  : �����豸�����Ự
 Input        : odp_rsa_session_params_t *params:�Ự����
 Output       :
                odp_crypto_session_t *session:�Ự���
                enum odp_crypto_ses_create_err *status:�Ựʧ��ԭ��
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rsa_session_create(odp_rsa_session_params_t *params,
			  odp_crypto_session_t *session,
			  enum odp_crypto_ses_create_err *status);


/*****************************************************************************
 Function     : odp_rsa_session_destroy
 Description  : ɾ���Ự
 Input        : odp_crypto_session_t session:�Ự���
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rsa_session_destroy(odp_crypto_session_t session);




/*****************************************************************************
 Function     : odp_rsa_operation
 Description  : RSA����
 Input        : odp_rsa_op_params_t *params:��������
 Output       :
                odp_bool_t *posted:ͬ��/�첽��־
                odp_rsa_op_result_t *result:�������
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rsa_operation(odp_rsa_op_params_t *params,
		     odp_bool_t *posted,
		     odp_rsa_op_result_t *result);




/*****************************************************************************
 Function     : odp_rsa_operation_burst
 Description  : RSA�������(ͬһ���Ự)
 Input        : odp_rsa_op_params_t *params_tbl[]:��������
                uint16_t nb_pkts:��������
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rsa_operation_burst(odp_rsa_op_params_t *params_tbl[], uint16_t nb_pkts);




#ifdef __cplusplus
}
#endif

#endif

