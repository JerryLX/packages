/* Copyright (c) 2014, Linaro Limited
 * All rights reserved.
 *
 * SPDX-License-Identifier:     BSD-3-Clause
 */


/**
 * @file
 *
 * ODP dh
 */

#ifndef ODP_API_DH_H_
#define ODP_API_DH_H_

#ifdef __cplusplus
extern "C" {
#endif


enum odp_dh_op {
	ODP_DH_OP_GEN_PHASE1,
    ODP_DH_OP_GEN_PHASE2,
};


typedef struct odp_dh_op_result {
	odp_crypto_session_t session;    /**< Session handle from creation   */
	odp_bool_t  ok;                  /**< Request completed successfully */
	void *ctx;                       /**< User context from request */
	odp_packet_t octet_string_pv;    /* ������Կ */
	odp_packet_t octet_string_sk;    /* ��ȫ��Կ */

	odp_crypto_compl_status_t status;  /**< status */
} odp_dh_op_result_t;




typedef struct odp_dh_session_params {
    odp_acc_dev_handle dev_handle;     /**< device handle */
	enum odp_dh_op op;
	enum odp_crypto_op_mode pref_mode;   /**< Preferred sync vs async */
	odp_queue_t compl_queue;           /**< Async mode completion event queue */
	odp_pool_t output_pool;            /**< Output buffer pool */

    void (*odp_asym_compl_pfn)(odp_crypto_session_t session, odp_dh_op_result_t *result); /* ע��ӽ��ܻص������� */
} odp_dh_session_params_t;



typedef struct odp_dh_op_params {
	odp_crypto_session_t session;     /**< Session handle from creation */
	void *ctx;                      /**< User context */
	odp_packet_t base_G;                /* ����G������0<G<P */
	odp_packet_t prime_P;               /* ����P */
	odp_packet_t private_X;             /* ˽ԿX������0<X<(P-1) */
	odp_packet_t octet_string_pv;       /* ������Կ */
	odp_packet_t octet_string_sk;       /* ��ȫ��Կ */
} odp_dh_op_params_t;


/*****************************************************************************
 Function     : odp_dh_session_create
 Description  : �����豸�����Ự
 Input        : odp_dh_session_params_t *params:�Ự����
 Output       :
                odp_crypto_session_t *session:�Ự���
                enum odp_crypto_ses_create_err *status:�Ựʧ��ԭ��
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dh_session_create(odp_dh_session_params_t *params,
			  odp_crypto_session_t *session,
			  enum odp_crypto_ses_create_err *status);


/*****************************************************************************
 Function     : odp_dh_session_destroy
 Description  : ɾ���Ự
 Input        : odp_crypto_session_t session:�Ự���
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dh_session_destroy(odp_crypto_session_t session);




/*****************************************************************************
 Function     : odp_dh_operation
 Description  : DH����
 Input        : odp_dh_op_params_t *params:��������
 Output       :
                odp_bool_t *posted:ͬ��/�첽��־
                odp_dh_op_result_t *result:�������
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dh_operation(odp_dh_op_params_t *params,
		     odp_bool_t *posted,
		     odp_dh_op_result_t *result);




/*****************************************************************************
 Function     : odp_dh_operation_burst
 Description  : DH�������(ͬһ���Ự)
 Input        : odp_dh_op_params_t *params_tbl[]:��������
                uint16_t nb_pkts:��������
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dh_operation_burst(odp_dh_op_params_t *params_tbl[], uint16_t nb_pkts);





#ifdef __cplusplus
}
#endif

#endif



