/* Copyright (c) 2014, Linaro Limited
 * All rights reserved.
 *
 * SPDX-License-Identifier:     BSD-3-Clause
 */


/**
 * @file
 *
 * ODP rbg
 */

#ifndef ODP_API_RBG_H_
#define ODP_API_RBG_H_

#ifdef __cplusplus
extern "C" {
#endif


enum odp_rbg_op {
	ODP_RBG_OP_DRBG_GEN,
    ODP_RBG_OP_DRBG_RESEED,
    ODP_RBG_OP_NRBG_ENTROPY
};


enum odp_rbg_sec_strength {
    ODP_RBG_SEC_STRENGTH_112 = 1,
    ODP_RBG_SEC_STRENGTH_128,
    ODP_RBG_SEC_STRENGTH_192,
    ODP_RBG_SEC_STRENGTH_256
};



typedef struct odp_rbg_op_result {
	odp_crypto_session_t session;    /**< Session handle from creation   */
	odp_bool_t  ok;                  /**< Request completed successfully */
	void *ctx;                       /**< User context from request */
    odp_packet_t out_random_data;

	odp_crypto_compl_status_t status;  /**< status */
} odp_rbg_op_result_t;




typedef struct odp_rbg_session_params {
    odp_acc_dev_handle dev_handle;     /**< device handle */
	enum odp_rbg_op op;
	enum odp_crypto_op_mode pref_mode;   /**< Preferred sync vs async */
	odp_queue_t compl_queue;           /**< Async mode completion event queue */
	odp_pool_t output_pool;            /**< Output buffer pool */

    odp_packet_t personalization_string;
    enum odp_rbg_sec_strength sec_strength;  /* ��ȫǿ�� */
    enum odp_boolean prediction_resistance_required;

    void (*odp_asym_compl_pfn)(odp_crypto_session_t session, odp_rbg_op_result_t *result); /* ע��ӽ��ܻص������� */
} odp_rbg_session_params_t;



typedef struct odp_rbg_op_params {
	odp_crypto_session_t session;     /**< Session handle from creation */
	void *ctx;                      /**< User context */

    uint32_t length_in_bytes;                /* �����������ĳ��� */
    enum odp_rbg_sec_strength sec_strength;  /* ��ȫǿ�� */
    enum odp_boolean prediction_resistance_required;
    odp_packet_t additional_input;           /* �������� */

    odp_packet_t out_random_data;
} odp_rbg_op_params_t;


/*****************************************************************************
 Function     : odp_rbg_session_create
 Description  : �����豸�����Ự
 Input        : odp_rbg_session_params_t *params:�Ự����
 Output       :
                odp_crypto_session_t *session:�Ự���
                enum odp_crypto_ses_create_err *status:�Ựʧ��ԭ��
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rbg_session_create(odp_rbg_session_params_t *params,
			  odp_crypto_session_t *session,
			  enum odp_crypto_ses_create_err *status);


/*****************************************************************************
 Function     : odp_rbg_session_destroy
 Description  : ɾ���Ự
 Input        : odp_crypto_session_t session:�Ự���
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rbg_session_destroy(odp_crypto_session_t session);




/*****************************************************************************
 Function     : odp_rbg_operation
 Description  : RBG����
 Input        : odp_rbg_op_params_t *params:��������
 Output       :
                odp_bool_t *posted:ͬ��/�첽��־
                odp_rbg_op_result_t *result:�������
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rbg_operation(odp_rbg_op_params_t *params,
		     odp_bool_t *posted,
		     odp_rbg_op_result_t *result);




/*****************************************************************************
 Function     : odp_rbg_operation_burst
 Description  : RBG�������(ͬһ���Ự)
 Input        : odp_rbg_op_params_t *params_tbl[]:��������
                uint16_t nb_pkts:��������
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_rbg_operation_burst(odp_rbg_op_params_t *params_tbl[], uint16_t nb_pkts);





#ifdef __cplusplus
}
#endif

#endif


