/* Copyright (c) 2014, Linaro Limited
 * All rights reserved.
 *
 * SPDX-License-Identifier:     BSD-3-Clause
 */


/**
 * @file
 *
 * ODP dsa
 */

#ifndef ODP_API_DSA_H_
#define ODP_API_DSA_H_

#ifdef __cplusplus
extern "C" {
#endif


enum odp_dsa_op {
	ODP_DSA_OP_GEN_G,
    ODP_DSA_OP_GEN_P,
    ODP_DSA_OP_GEN_Y,
    ODP_DSA_OP_SIGN_R,
    ODP_DSA_OP_SIGN_S,
    ODP_DSA_OP_SIGN_RS,
    ODP_DSA_OP_VERIFY_RS
};


typedef struct odp_dsa_op_result {
	odp_crypto_session_t session;    /**< Session handle from creation */
	odp_bool_t  ok;                  /**< Request completed successfully */
	void *ctx;                       /**< User context from request */
	odp_packet_t R;                /* Rǩ�� */
	odp_packet_t S;                /* Sǩ�� */

    odp_packet_t P;                /* ����P */
    odp_packet_t G;                /* ����G������1<G<P*/
    odp_packet_t Y;                /* ����Y */

    enum odp_boolean protocol_status;
    enum odp_boolean verify_status;
	odp_crypto_compl_status_t status;  /**< status */
} odp_dsa_op_result_t;




typedef struct odp_dsa_session_params {
    odp_acc_dev_handle dev_handle;     /**< device handle */
	enum odp_dsa_op op;
	enum odp_crypto_op_mode pref_mode;   /**< Preferred sync vs async */
	odp_queue_t compl_queue;           /**< Async mode completion event queue */
	odp_pool_t output_pool;            /**< Output buffer pool */

    void (*odp_asym_compl_pfn)(odp_crypto_session_t session, odp_dsa_op_result_t *result); /* ע��ӽ��ܻص������� */
} odp_dsa_session_params_t;



typedef struct odp_dsa_op_params {
	odp_crypto_session_t session;     /**< Session handle from creation */
	void *ctx;                      /**< User context */
	odp_packet_t R;                /* Rǩ�� */
	odp_packet_t S;                /* Sǩ�� */

    odp_packet_t Z;                /* ��ǩ�����ĵ�hashֵ */
    odp_packet_t K;                /* ����K������0<K<Q */
    odp_packet_t H;                /* ����H */
    odp_packet_t Q;                /* ����Q */
    odp_packet_t X;                /* ����X������0<X<Q  */
    odp_packet_t P;                /* ����P */
    odp_packet_t G;                /* ����G������1<G<P*/
    odp_packet_t Y;                /* ����Y */
} odp_dsa_op_params_t;


/*****************************************************************************
 Function     : odp_dsa_session_create
 Description  : �����豸�����Ự
 Input        : odp_dsa_session_params_t *params:�Ự����
 Output       :
                odp_crypto_session_t *session:�Ự���
                enum odp_crypto_ses_create_err *status:�Ựʧ��ԭ��
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dsa_session_create(odp_dsa_session_params_t *params,
			  odp_crypto_session_t *session,
			  enum odp_crypto_ses_create_err *status);


/*****************************************************************************
 Function     : odp_dsa_session_destroy
 Description  : ɾ���Ự
 Input        : odp_crypto_session_t session:�Ự���
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dsa_session_destroy(odp_crypto_session_t session);




/*****************************************************************************
 Function     : odp_dsa_operation
 Description  : DSA����
 Input        : odp_dsa_op_params_t *params:��������
 Output       :
                odp_bool_t *posted:ͬ��/�첽��־
                odp_dsa_op_result_t *result:�������
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dsa_operation(odp_dsa_op_params_t *params,
		     odp_bool_t *posted,
		     odp_dsa_op_result_t *result);




/*****************************************************************************
 Function     : odp_dsa_operation_burst
 Description  : DSA�������(ͬһ���Ự)
 Input        : odp_dsa_op_params_t *params_tbl[]:��������
                uint16_t nb_pkts:��������
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_dsa_operation_burst(odp_dsa_op_params_t *params_tbl[], uint16_t nb_pkts);





#ifdef __cplusplus
}
#endif

#endif


