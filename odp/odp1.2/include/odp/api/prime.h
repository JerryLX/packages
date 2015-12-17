/* Copyright (c) 2014, Linaro Limited
 * All rights reserved.
 *
 * SPDX-License-Identifier:     BSD-3-Clause
 */


/**
 * @file
 *
 * ODP prime
 */

#ifndef ODP_API_PRIME_H_
#define ODP_API_PRIME_H_

#ifdef __cplusplus
extern "C" {
#endif


enum odp_prime_op {
	ODP_PRIME_OP_TEST,
};


typedef struct odp_prime_op_result {
	odp_crypto_session_t session;    /**< Session handle from creation   */
	odp_bool_t  ok;                  /**< Request completed successfully */
	void *ctx;                       /**< User context from request */

    enum odp_boolean test_passed;
	odp_crypto_compl_status_t status;  /**< status */
} odp_prime_op_result_t;




typedef struct odp_prime_session_params {
    odp_acc_dev_handle dev_handle;     /**< device handle */
	enum odp_prime_op op;
	enum odp_crypto_op_mode pref_mode;   /**< Preferred sync vs async */
	odp_queue_t compl_queue;           /**< Async mode completion event queue */
	odp_pool_t output_pool;            /**< Output buffer pool */

    void (*odp_asym_compl_pfn)(odp_crypto_session_t session, odp_prime_op_result_t *result); /* ע��ӽ��ܻص������� */
} odp_prime_session_params_t;



typedef struct odp_prime_op_params {
	odp_crypto_session_t session;     /**< Session handle from creation */
	void *ctx;                        /**< User context */

	odp_packet_t base_G;                /* ����G������0<G<P */

    odp_packet_t prime_candidate;           /* ���������� */
    enum odp_boolean perform_gcd_test;      /* ��ʾ�Ƿ�ִ��GCD�������� */
    enum odp_boolean perform_fermat_test;   /* ��ʾ�Ƿ�ִ��fermat�������� */
    uint32_t num_miller_rabin_rounds;       /* MR�������������Բ��Ե�׼ȷ�� */
    odp_packet_t miller_rabin_random_input; /* MR�����䳤�ȴ�СΪ������������(���С��64bit��Ϊ64bit)����MR���� */
    enum odp_boolean perform_lucas_test;    /* ��ʾLucas�����ж��Ƿ�ִ�� */
} odp_prime_op_params_t;


/*****************************************************************************
 Function     : odp_prime_session_create
 Description  : �����豸�����Ự
 Input        : odp_prime_session_params_t *params:�Ự����
 Output       :
                odp_crypto_session_t *session:�Ự���
                enum odp_crypto_ses_create_err *status:�Ựʧ��ԭ��
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_prime_session_create(odp_prime_session_params_t *params,
			  odp_crypto_session_t *session,
			  enum odp_crypto_ses_create_err *status);


/*****************************************************************************
 Function     : odp_prime_session_destroy
 Description  : ɾ���Ự
 Input        : odp_crypto_session_t session:�Ự���
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_prime_session_destroy(odp_crypto_session_t session);




/*****************************************************************************
 Function     : odp_prime_operation
 Description  : PRIME����
 Input        : odp_prime_op_params_t *params:��������
 Output       :
                odp_bool_t *posted:ͬ��/�첽��־
                odp_prime_op_result_t *result:�������
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_prime_operation(odp_prime_op_params_t *params,
		     odp_bool_t *posted,
		     odp_prime_op_result_t *result);




/*****************************************************************************
 Function     : odp_prime_operation_burst
 Description  : PRIME�������(ͬһ���Ự)
 Input        : odp_prime_op_params_t *params_tbl[]:��������
                uint16_t nb_pkts:��������
 Output       :
 Return       :
 Create By    :
 Modification :
 Restriction  :
*****************************************************************************/
int odp_prime_operation_burst(odp_prime_op_params_t *params_tbl[], uint16_t nb_pkts);





#ifdef __cplusplus
}
#endif

#endif




