package com.htsat.cart.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.htsat.cart.config.RedisConfig;
import com.htsat.cart.exception.DeleteException;
import com.htsat.cart.exception.InsertException;
import com.htsat.cart.exception.SearchException;
import com.htsat.cart.exception.UpdateException;
import com.htsat.cart.dao.REcCartskuMapper;
import com.htsat.cart.dao.REcShoppingcartMapper;
import com.htsat.cart.dao.REcSkuMapper;
import com.htsat.cart.dto.SKUDTO;
import com.htsat.cart.dto.ShoppingCartDTO;
import com.htsat.cart.model.REcCartsku;
import com.htsat.cart.model.REcCartskuKey;
import com.htsat.cart.model.REcShoppingcart;
import com.htsat.cart.model.REcSku;
//import com.htsat.cart.service.IRedisService;
import com.htsat.cart.service.IShoppingCartService;
import com.htsat.cart.utils.ComputeUtils;
import com.htsat.cart.utils.ConvertToDTO;
import com.htsat.cart.utils.SerializeUtil;
import com.htsat.cart.utils.SortList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements IShoppingCartService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private REcShoppingcartMapper shoppingcartMapper;

    @Autowired
    private REcCartskuMapper cartskuMapper;

    @Autowired
    private REcSkuMapper skuMapper;

    @Autowired
    private RedisConfig redisConfig;

    private static final String cartRedisKey = "ShoppingCart:";

    /******************************************create*********************************************/

    @Override
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=3600,rollbackFor=Exception.class)
    public void createShoppingCartAndSKU(ShoppingCartDTO shoppingCartDTO) throws InsertException {
        ShoppingCartDTO returnShoppingCartDTO = createShoppingCartAndSKUToMySQL(shoppingCartDTO);
        createShoppCartAndSKUToRedis(returnShoppingCartDTO);
    }

    private ShoppingCartDTO createShoppingCartAndSKUToMySQL(ShoppingCartDTO shoppingCartDTO) throws InsertException {
        int totalquantity = 0;
        BigDecimal totaldiscount = new BigDecimal(0);
        BigDecimal totalPrice = new BigDecimal(0);
        if (shoppingCartDTO.getSkudtoList() != null && shoppingCartDTO.getSkudtoList().size() > 0) {
            totalquantity = ComputeUtils.computeNumber(shoppingCartDTO.getSkudtoList());
            totaldiscount = ComputeUtils.computeDiscount(shoppingCartDTO.getSkudtoList());
            totalPrice = ComputeUtils.computeTotalPrice(shoppingCartDTO.getSkudtoList());
        }

        REcShoppingcart shoppingcart = shoppingcartMapper.selectByPrimaryKey(shoppingCartDTO.getUserId());

        // create
        if (shoppingcart == null){

            // create ShoppingCart
            shoppingcart = new REcShoppingcart();

            shoppingcart.setNuserid(shoppingCartDTO.getUserId());
            shoppingcart.setNshoppingcartid(shoppingCartDTO.getUserId());

            if (StringUtils.isNotEmpty(shoppingCartDTO.getCurrency())) {
                shoppingcart.setScurrency(shoppingCartDTO.getCurrency());
            }
            shoppingcart.setNtotalquantity(totalquantity);
            shoppingcart.setNdiscount(totaldiscount);
            shoppingcart.setNtotalprice(totalPrice);
            try {
                shoppingcartMapper.insertSelective(shoppingcart);
            } catch (Exception e){
                throw new InsertException("mysql : create ShoppingCart failed");
            }

            //create CartSKU
            List<REcCartsku> cartskuList = new ArrayList<>();
            if (shoppingCartDTO.getSkudtoList() != null && shoppingCartDTO.getSkudtoList().size() > 0) {
                for (SKUDTO skuDTO : shoppingCartDTO.getSkudtoList()) {
                    REcCartsku cartsku = new REcCartsku();
                    cartsku.setNuserid(shoppingCartDTO.getUserId());
                    cartsku.setNshoppingcartid(shoppingCartDTO.getUserId());
                    cartsku.setNskuid(skuDTO.getSkuId());
                    cartsku.setNquantity(skuDTO.getQuantity());
                    try {
                        cartskuMapper.insertSelective(cartsku);
                    } catch (Exception e) {
                        throw new InsertException("mysql : create SKU failed");
                    }
                    cartskuList.add(cartsku);
                }
            }
            List<REcSku> skuList = getSKUListBycarskuList(cartskuList);
            ShoppingCartDTO returnShoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);

            return returnShoppingCartDTO;
        }

        // update
        shoppingcart.setNuserid(shoppingCartDTO.getUserId());
        shoppingcart.setNshoppingcartid(shoppingCartDTO.getUserId());

        if (StringUtils.isNotEmpty(shoppingCartDTO.getCurrency())) {
            shoppingcart.setScurrency(shoppingCartDTO.getCurrency());
        }
        shoppingcart.setNtotalquantity(totalquantity);
        shoppingcart.setNdiscount(totaldiscount);
        shoppingcart.setNtotalprice(totalPrice);
        try {
            shoppingcartMapper.updateByPrimaryKeySelective(shoppingcart);
        } catch (Exception e){
            throw new InsertException("mysql : update ShoppingCart failed");
        }

        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingCartDTO.getUserId());
        if (cartskuList != null && cartskuList.size() > 0 ) {
            for (REcCartsku cartsku : cartskuList) {
                try {
                    cartskuMapper.deleteByPrimaryKey(cartsku.getNcartskuid());
                } catch (Exception e) {
                    throw new InsertException("mysql : update SKU - delete failed");
                }
            }
        }

        List<REcCartsku> updateCartskuList = new ArrayList<>();
        if (shoppingCartDTO.getSkudtoList() != null && shoppingCartDTO.getSkudtoList().size() > 0) {
            for (SKUDTO skuDTO : shoppingCartDTO.getSkudtoList()) {
                REcCartsku cartsku = new REcCartsku();
                cartsku.setNuserid(shoppingCartDTO.getUserId());
                cartsku.setNshoppingcartid(shoppingCartDTO.getUserId());
                cartsku.setNskuid(skuDTO.getSkuId());
                cartsku.setNquantity(skuDTO.getQuantity());
                try {
                    cartskuMapper.insertSelective(cartsku);
                } catch (Exception e) {
                    throw new InsertException("mysql : update SKU - create failed");
                }
                updateCartskuList.add(cartsku);
            }
        }
        List<REcSku> skuList = getSKUListBycarskuList(updateCartskuList);
        ShoppingCartDTO returnShoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);

        return returnShoppingCartDTO;
    }

    private void createShoppCartAndSKUToRedis(ShoppingCartDTO returnShoppingCartDTO) throws InsertException{
        Jedis jedis = redisConfig.getJedis();
        String code = null;
        String dtoJson = JSON.toJSONString(returnShoppingCartDTO);
        try {
            code = jedis.set((cartRedisKey + returnShoppingCartDTO.getNshoppingcartid()), dtoJson);
        } catch (Exception e) {
            logger.error("Redis insert error: "+ e.getMessage() +" - " + returnShoppingCartDTO.getUserId() + ", value:" + returnShoppingCartDTO);
            throw new InsertException("redis : create exception");
        } finally{
            redisConfig.returnResource(jedis);
        }
        if (StringUtils.isEmpty(code)) {
            throw new InsertException("redis : create failed");
        }
    }

//    /**
//     *
//     * @param shoppingCartDTO
//     * @throws InsertException
//     */
//    @Override
//    public void addShoppingCartAndSKU(ShoppingCartDTO shoppingCartDTO) throws InsertException {
//        ShoppingCartDTO returnShoppingCartDTO = addShoppingCartAndSKUToMySQL(shoppingCartDTO);
//        addShoppCartAndSKUToRedis(returnShoppingCartDTO);
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=3600,rollbackFor=InsertException.class)
//    public ShoppingCartDTO addShoppingCartAndSKUToMySQL(ShoppingCartDTO shoppingCartDTO) throws InsertException {
//        // 创建购物车
//        REcShoppingcart shoppingcart = createShoppingCart(shoppingCartDTO.getUserId(), shoppingCartDTO.getCurrency(), shoppingCartDTO.getSkudtoList());
//        //添加商品
//        List<REcSku> skuList = createCartSKU(shoppingCartDTO, shoppingCartDTO.getSkudtoList(), shoppingcart.getNshoppingcartid());
//        //返回DTO数据
//        ShoppingCartDTO returnShoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);
//        return returnShoppingCartDTO;
//    }
//
//    private REcShoppingcart createShoppingCart(Long userId, String currency, List<SKUDTO> skuDTOList) throws InsertException {
//        int totalquantity = ComputeUtils.computeNumber(skuDTOList);
//        BigDecimal totaldiscount = ComputeUtils.computeDiscount(skuDTOList);
//        BigDecimal totalPrice = ComputeUtils.computeTotalPrice(skuDTOList);
//
//        REcShoppingcart shoppingcart = new REcShoppingcart();
//        shoppingcart.setNuserid(userId);
//        shoppingcart.setScurrency(currency);
//        shoppingcart.setNtotalquantity(totalquantity);
//        shoppingcart.setNdiscount(totaldiscount);
//        shoppingcart.setNtotalprice(totalPrice);
//
//        int insertResult = shoppingcartMapper.insertSelective(shoppingcart);
//        if (insertResult != 1){
//            throw new InsertException("mysql : create ShoppingCart failed");
//        }
//        return shoppingcart;
//    }
//
//    private List<REcSku> createCartSKU(ShoppingCartDTO shoppingCartDTO, List<SKUDTO> skuDTOList, Long shoppingcartId) throws InsertException {
//        List<REcCartsku> cartskuList = new ArrayList<>();
//        for (SKUDTO skuDTO : skuDTOList) {
//            REcCartsku cartsku = new REcCartsku();
//            cartsku.setNuserid(shoppingCartDTO.getUserId());
//            cartsku.setNshoppingcartid(shoppingcartId);
//            cartsku.setNskuid(skuDTO.getSkuId());
//            cartsku.setNquantity(skuDTO.getQuantity());
//            int result = cartskuMapper.insertSelective(cartsku);
//            if (result != 1) {
//                throw new InsertException("mysql : create SKU failed");
//            }
//            cartskuList.add(cartsku);
//        }
//        return getSKUListBycarskuList(cartskuList);
//    }
//
//    private void addShoppCartAndSKUToRedis(ShoppingCartDTO returnShoppingCartDTO) throws InsertException{
//        Jedis jedis = redisConfig.getJedis();
//        String code = null;
//        String dtoJson = JSON.toJSONString(returnShoppingCartDTO);
//        try {
//            code = jedis.set((cartRedisKey + returnShoppingCartDTO.getNshoppingcartid()), dtoJson);
//        } catch (Exception e) {
//            logger.error("Redis insert error: "+ e.getMessage() +" - " + returnShoppingCartDTO.getUserId() + ", value:" + returnShoppingCartDTO);
//        } finally{
//            redisConfig.returnResource(jedis);
//        }
//        if (StringUtils.isEmpty(code)) {
//            throw new InsertException("redis : create failed");
//        }
//    }

    /*********************************************search******************************************/

    @Override
    public String getShoppingCartByUser(Long userid) throws SearchException{
        List<REcShoppingcart> shoppingcartList = shoppingcartMapper.selectByUserId(userid);
        if (shoppingcartList == null || shoppingcartList.size() != 1) {
            logger.error("this user has too many or no shoppingcart");
            throw new SearchException("mysql : too many or no shoppingcart");
        }
        String shoppingCartDTOJSON = getShoppingCart(userid, shoppingcartList.get(0).getNshoppingcartid());
        return shoppingCartDTOJSON;
    }

    /**
     *
     * @param shoppingcartid
     * @return
     * @throws SearchException
     */
    @Override
    public String getShoppingCart(Long userid, Long shoppingcartid) throws SearchException{

        ShoppingCartDTO shoppingCartDTO = getShoppingCartFromRedis(userid, shoppingcartid);

        if (shoppingCartDTO == null) {
            REcShoppingcart shoppingcart = getShoppingCartByMySQL(userid, shoppingcartid);
            if (shoppingcart == null)
                return "None ShoppingCart Matched";
            List<REcSku> skuList = getSKUListByMySQL(shoppingcart.getNshoppingcartid());
            shoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);
        }

        if (shoppingCartDTO != null) {
            String orderDTOJSON = JSON.toJSONString(shoppingCartDTO);
            return orderDTOJSON;
        } else {
            return "None ShoppingCart Matched";
        }

    }

    private REcShoppingcart getShoppingCartByMySQL(Long userid, Long shoppingcartid) throws SearchException{
        REcShoppingcart shoppingcart = shoppingcartMapper.selectByUserIdAndCartId(userid, shoppingcartid);
        if(shoppingcart == null)
            return null;
        return shoppingcart;
    }

    private List<REcSku> getSKUListByMySQL(Long shoppingcartId) throws SearchException{
        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcartId);
        List<REcSku> skuList = getSKUListBycarskuList(cartskuList);
        if (skuList == null) {
            throw new SearchException("mysql : search SKU failed");
        }
        return skuList;
    }

    private ShoppingCartDTO getShoppingCartFromRedis(Long userid, Long shoppingcartid) throws SearchException{
        Jedis jedis = redisConfig.getJedis();
        if(jedis == null){
            return null;
        }
        String shoppingCart = null;
        try {
            shoppingCart = jedis.get(cartRedisKey + shoppingcartid);
        } catch (Exception e) {
            logger.error("Redis get error: "+ e.getMessage() +" - key : " + shoppingcartid);
            throw new SearchException("redis : search exception");
        } finally{
            redisConfig.returnResource(jedis);
        }
        jedis = null;
        if (StringUtils.isEmpty(shoppingCart)) {
            return null;
        } else {
            ShoppingCartDTO shoppingCartDTO = JSON.parseObject(shoppingCart, ShoppingCartDTO.class);
            if (userid == null)
                return shoppingCartDTO;
            else if (userid.equals(shoppingCartDTO.getUserId()))
                return shoppingCartDTO;
            else
                return null;
        }

    }

    /*********************************************delete******************************************/

    @Override
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=3600,rollbackFor=Exception.class)
    public void deleteShoppingCartAndSKU(Long cartid) throws DeleteException {
        deleteShoppingCartAndSKUByMySQL(cartid);
        deleteShoppingCartAndSKUToRedis(cartid);
    }

    private void deleteShoppingCartAndSKUByMySQL(Long shoppingcartId) throws DeleteException {
        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcartId);
        List<Long> skuIdList = new ArrayList<>();
        cartskuList.forEach(n -> skuIdList.add(n.getNskuid()));
        for (Long skuId : skuIdList) {
            deleteCartSKU(shoppingcartId, skuId);
        }
        deleteShoppingCart(shoppingcartId);
    }

    private boolean deleteShoppingCart(Long shoppingcartId) throws DeleteException{
        try {
            shoppingcartMapper.deleteByPrimaryKey(shoppingcartId);
        } catch (Exception e) {
            throw new DeleteException("mysql : delete ShoppingCart failed");
        }
        return true;
    }

    private boolean deleteCartSKU(Long shoppingcartId, Long skuId) throws DeleteException {
        REcCartskuKey cartskuKey = new REcCartskuKey();
        cartskuKey.setNshoppingcartid(shoppingcartId);
        cartskuKey.setNskuid(skuId);
        try {
            cartskuMapper.deleteByREcCartskuKey(cartskuKey);
        } catch (Exception e) {
            throw new DeleteException("mysql : delete CartSKU failed");
        }
        return true;
    }

    private void deleteShoppingCartAndSKUToRedis(Long shoppingcartid) throws DeleteException {
        Jedis jedis = redisConfig.getJedis();
        try {
            Long reply = jedis.del(cartRedisKey + shoppingcartid);
        } catch (Exception e) {
            logger.error("Redis delete error: "+ e.getMessage() +" - key : " + shoppingcartid);
            throw new DeleteException("redis : delete exception");
        }finally{
            redisConfig.returnResource(jedis);
        }
    }


    /*********************************************udpate******************************************/

//    @Override
//    public ShoppingCartDTO updateShoppingCartAndSKU(int type, ShoppingCartDTO shoppingCartDTO) throws UpdateException {
//        ShoppingCartDTO returnShoppingCartDTO = updateShoppingCartAndSKUByMySQL(type, shoppingCartDTO);
//        updateShoppingCartAndSKUToRedis(returnShoppingCartDTO);
//        return returnShoppingCartDTO;
//    }
//
//
//    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=3600,rollbackFor=Exception.class)
//    public ShoppingCartDTO updateShoppingCartAndSKUByMySQL(int type, ShoppingCartDTO shoppingCartDTO) throws UpdateException{
//
//        if (type == 1) {//add
//            if (!checkSKUParam(shoppingCartDTO.getSkudtoList(), getSKUListByDTOList(shoppingCartDTO.getSkudtoList()))) {
//                return null;
//            }
//            REcShoppingcart shoppingcart = updateShoppingCart(type, shoppingCartDTO.getUserId(), shoppingCartDTO.getSkudtoList());
//            List<REcSku> skuList = updateAddCartSKU(shoppingCartDTO.getUserId(), shoppingCartDTO.getSkudtoList());
//            shoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);
//        } else if (type == 2) {//update
//            if (!checkSKUParam(shoppingCartDTO.getSkudtoList(), getSKUListByDTOList(shoppingCartDTO.getSkudtoList()))) {
//                return null;
//            }
//            REcShoppingcart shoppingcart = updateShoppingCart(type, shoppingCartDTO.getUserId(), shoppingCartDTO.getSkudtoList());
//            List<REcSku> skuList = updateRefreshCartSKU(shoppingCartDTO.getUserId(), shoppingCartDTO.getSkudtoList());
//            shoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);
//        } else if (type == 3) {//delete
//            List<SKUDTO> skudtoList = shoppingCartDTO.getSkudtoList();
//            List<REcSku> skuListCheck = getSKUListByDTOList(skudtoList);
//            if (skudtoList.size() != skuListCheck.size())
//                return null;
//            Collections.sort(skudtoList, new SortList<SKUDTO>("SkuId",true));
//            for (int i = 0; i < skudtoList.size(); i++) {
//                if (!skudtoList.get(i).getSkuId().equals(skuListCheck.get(i).getNskuid())) {
//                    return null;
//                }
//            }
//            REcShoppingcart shoppingcart = updateShoppingCart(type, shoppingCartDTO.getUserId(), shoppingCartDTO.getSkudtoList());
//            List<REcSku> skuList = updateDeleteCartSKU(shoppingCartDTO.getUserId(), shoppingCartDTO.getSkudtoList());
//            shoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);
//        } else {
//            return null;
//        }
//
//        return shoppingCartDTO;
//    }
//
//    private REcShoppingcart updateShoppingCart(int type, Long userId, List<SKUDTO> skuDTOList) throws UpdateException{
//        REcShoppingcart shoppingcart = getShoppingCartByUserId(userId);
//        int quantity = ComputeUtils.computeNumber(skuDTOList);
//        BigDecimal discount = ComputeUtils.computeDiscount(skuDTOList);
//        BigDecimal totalPrice = ComputeUtils.computeTotalPrice(skuDTOList);
//
////        REcShoppingcart shoppingcart = shoppingcartMapper.selectByPrimaryKey(userId);
//        if (type == 1) {//add
//            shoppingcart.setNtotalquantity(shoppingcart.getNtotalquantity() + quantity);
//            shoppingcart.setNdiscount(shoppingcart.getNdiscount().add(discount));
//            shoppingcart.setNtotalprice(shoppingcart.getNtotalprice().add(totalPrice));
//        } else if (type == 2) {//update
//            computeQuantityDiscountTotalPrice(skuDTOList, shoppingcart);
//        } else if (type == 3) {//delete
//            shoppingcart.setNtotalquantity(shoppingcart.getNtotalquantity() - quantity);
//            shoppingcart.setNdiscount(shoppingcart.getNdiscount().subtract(discount));
//            shoppingcart.setNtotalprice(shoppingcart.getNtotalprice().subtract(totalPrice));
//        }
//
//        int updateResult = shoppingcartMapper.updateByPrimaryKeySelective(shoppingcart);
//        if (updateResult != 1) {
//            throw new UpdateException("mysql : update ShoppingCart failed");
//        }
//        REcShoppingcart shoppingcartResult = shoppingcartMapper.selectByPrimaryKey(shoppingcart.getNshoppingcartid());
//        if (shoppingcartResult == null) {
//            throw new UpdateException("mysql : update search ShoppingCart failed");
//        }
//        return shoppingcartResult;
//    }
//
//    private List<REcSku> updateAddCartSKU(Long userId, List<SKUDTO> skuDTOList) throws UpdateException {
//        REcShoppingcart shoppingcartResult = getShoppingCartByUserId(userId);
//        for (SKUDTO skuDTO : skuDTOList) {
//            REcCartsku cartsku = new REcCartsku();
//            cartsku.setNuserid(userId);
//            cartsku.setNshoppingcartid(shoppingcartResult.getNshoppingcartid());
//            cartsku.setNskuid(skuDTO.getSkuId());
//            cartsku.setNquantity(skuDTO.getQuantity());
//            int result = cartskuMapper.insert(cartsku);
//            if (result != 1) {
//                throw new UpdateException("mysql : update add CartSKU failed");
//            }
//        }
//        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcartResult.getNshoppingcartid());
//        List<REcSku> skuList = getSKUListBycarskuList(cartskuList);
//        return skuList;
//    }
//
//    private List<REcSku> updateRefreshCartSKU(Long userId, List<SKUDTO> skuDTOList) throws UpdateException {
//        REcShoppingcart shoppingcart = getShoppingCartByUserId(userId);
//        for (SKUDTO skuDTO : skuDTOList) {
//            REcCartskuKey key = new REcCartskuKey();
//            key.setNshoppingcartid(shoppingcart.getNshoppingcartid());
//            key.setNskuid(skuDTO.getSkuId());
//            REcCartsku cartsku = cartskuMapper.selectByREcCartskuKey(key);
//            cartsku.setNquantity(skuDTO.getQuantity());//update quantity
//            int result = cartskuMapper.updateByPrimaryKey(cartsku);
//            if (result != 1) {
//                throw new UpdateException("mysql : update refresh CartSKU failed");
//            }
//        }
//        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcart.getNshoppingcartid());
//        List<REcSku> skuList = getSKUListBycarskuList(cartskuList);
//        return skuList;
//    }
//
//    private List<REcSku> updateDeleteCartSKU(Long userId, List<SKUDTO> skuDTOList) throws UpdateException {
//        REcShoppingcart shoppingcart = getShoppingCartByUserId(userId);
//        for (SKUDTO skuDTO : skuDTOList) {
//            REcCartskuKey key = new REcCartskuKey();
//            key.setNshoppingcartid(shoppingcart.getNshoppingcartid());
//            key.setNskuid(skuDTO.getSkuId());
//            REcCartsku cartsku = cartskuMapper.selectByREcCartskuKey(key);
//            cartsku.setNquantity(skuDTO.getQuantity());//delete quantity
//            int result = cartskuMapper.deleteByPrimaryKey(cartsku.getNcartskuid());
//            if (result != 1) {
//                throw new UpdateException("mysql : update delete CartSKU failed");
//            }
//        }
//        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcart.getNshoppingcartid());
//        List<REcSku> skuList = getSKUListBycarskuList(cartskuList);
//        return skuList;
//    }
//


    private void updateShoppingCartAndSKUToRedis(ShoppingCartDTO returnShoppingCartDTO) throws UpdateException {
        Jedis jedis = redisConfig.getJedis();
        String reply = null;
        String dtoJson = JSON.toJSONString(returnShoppingCartDTO);
        try {
            reply = jedis.set((cartRedisKey + returnShoppingCartDTO.getNshoppingcartid()), dtoJson);
        } catch (Exception e) {
            logger.error("Redis update error: "+ e.getMessage() +" - " + returnShoppingCartDTO.getNshoppingcartid() + "" + ", value:" + returnShoppingCartDTO);
            throw new UpdateException("redis : update exception");
        }finally{
            redisConfig.returnResource(jedis);
        }

        if (StringUtils.isEmpty(reply)) {
            throw new UpdateException("redis : update failed");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=3600,rollbackFor=Exception.class)
    public void updateShoppingCartSKU(ShoppingCartDTO shoppingCartDTO, Long userid, Long cartid, Long skuid) throws UpdateException {
        ShoppingCartDTO returnShoppingCartDTO = updateShoppingCartAndSKUByMySQL(shoppingCartDTO, userid, cartid, skuid);
        updateShoppingCartAndSKUToRedis(returnShoppingCartDTO);
    }

    private ShoppingCartDTO updateShoppingCartAndSKUByMySQL(ShoppingCartDTO shoppingCartDTO, Long userid, Long cartid, Long skuid) throws UpdateException{
        REcShoppingcart shoppingcart = shoppingcartMapper.selectByPrimaryKey(cartid);

        if (shoppingCartDTO == null || shoppingCartDTO.getSkudtoList() == null || shoppingCartDTO.getSkudtoList().size() != 1 || shoppingcart == null) {
            logger.error("ShoppingCartDTO request invalid");
            throw new UpdateException("mysql : update product, shoppingCartDTO request invalid");
        }
        SKUDTO skudto = shoppingCartDTO.getSkudtoList().get(0);
        skudto.setSkuId(skuid);//to make sure sku dto has skuid
//        List<REcSku> skuList = getSKUListByDTOList(shoppingCartDTO.getSkudtoList());
        REcSku sku = skuMapper.selectByPrimaryKey(skuid);
        if (sku == null){
            logger.error("ShoppingCartDTO SKUDTO request invalid, no sku matched");
            throw new UpdateException("mysql : update product, no sku matched");
        }
//        if (!checkSingleSKUParam(skudto, sku)) {
//            logger.error("ShoppingCartDTO SKUDTO request invalid, skudto param don't match sku param");
//            throw new UpdateException("mysql : update product, skudto param don't match sku param");
//        }

//        if (skuList == null || skuList.size() != 1){
//            logger.error("ShoppingCartDTO SKUDTO request invalid");
//            throw new UpdateException("mysql : update product, no sku matched or too many sku matched");
//        }
//        if (!checkSingleSKUParam(shoppingCartDTO.getSkudtoList().get(0), skuList.get(0))) {
//            logger.error("ShoppingCartDTO SKUDTO request invalid");
//            throw new UpdateException("mysql : update product, skudto param don't match sku param");
//        }

        int quantity = ComputeUtils.computeNumber(shoppingCartDTO.getSkudtoList());
        BigDecimal discount = ComputeUtils.computeDiscount(shoppingCartDTO.getSkudtoList());
        BigDecimal totalPrice = ComputeUtils.computeTotalPrice(shoppingCartDTO.getSkudtoList());

        REcCartskuKey key = new REcCartskuKey();
        key.setNshoppingcartid(cartid);
        key.setNskuid(skuid);
        REcCartsku cartsku = cartskuMapper.selectByREcCartskuKey(key);
        if (cartsku == null) {
            //update cartsku, insert product
            cartsku = new REcCartsku();
            cartsku.setNuserid(userid);
            cartsku.setNshoppingcartid(cartid);
            cartsku.setNskuid(skuid);
            cartsku.setNquantity(skudto.getQuantity());

            try {
                cartskuMapper.insert(cartsku);
            } catch (Exception e){
                throw new UpdateException("mysql : update add CartSKU failed");
            }

            //update shoopingcart
            shoppingcart.setNtotalquantity(shoppingcart.getNtotalquantity() + quantity);
            shoppingcart.setNdiscount(shoppingcart.getNdiscount().add(discount));
            shoppingcart.setNtotalprice(shoppingcart.getNtotalprice().add(totalPrice));
        } else{
            //update cartsku, update product
            List<SKUDTO> list = shoppingCartDTO.getSkudtoList();
            list.get(0).setSkuId(skuid);
            computeQuantityDiscountTotalPrice(shoppingCartDTO.getSkudtoList(), shoppingcart);

            cartsku.setNquantity(skudto.getQuantity());//update quantity

            try {
                cartskuMapper.updateByPrimaryKey(cartsku);
            } catch (Exception e) {
                throw new UpdateException("mysql : update refresh CartSKU failed");
            }
        }

        try {
            shoppingcartMapper.updateByPrimaryKeySelective(shoppingcart);
        } catch (Exception e){
            throw new UpdateException("mysql : update ShoppingCart failed");
        }

        REcShoppingcart shoppingcartResult = shoppingcartMapper.selectByPrimaryKey(shoppingcart.getNshoppingcartid());
        if (shoppingcartResult == null) {
            throw new UpdateException("mysql : update search ShoppingCart failed");
        }

        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcartResult.getNshoppingcartid());
        List<REcSku> returnSkuList = getSKUListBycarskuList(cartskuList);

        ShoppingCartDTO returnShoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, returnSkuList);
        return returnShoppingCartDTO;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=3600,rollbackFor=Exception.class)
    public void deleteShoppingCartSKU(Long cartid, Long skuid) throws UpdateException {
        ShoppingCartDTO returnShoppingCartDTO = updateShoppingCartAndSKUByMySQL(cartid, skuid);
        updateShoppingCartAndSKUToRedis(returnShoppingCartDTO);
    }

    public ShoppingCartDTO updateShoppingCartAndSKUByMySQL(Long cartid, Long skuid) throws UpdateException{
        REcShoppingcart shoppingcart = shoppingcartMapper.selectByPrimaryKey(cartid);
        if (shoppingcart == null) {
            logger.error("cartid request invalid");
            throw new UpdateException("mysql : delete product, cartid request invalid");
        }
        REcSku sku = skuMapper.selectByPrimaryKey(skuid);
        if (shoppingcart == null) {
            logger.error("skuid request invalid");
            throw new UpdateException("mysql : delete product, skuid request invalid");
        }

        //delete cartsku
        REcCartskuKey key = new REcCartskuKey();
        key.setNshoppingcartid(cartid);
        key.setNskuid(skuid);
        REcCartsku cartsku = cartskuMapper.selectByREcCartskuKey(key);

        try {
            cartskuMapper.deleteByPrimaryKey(cartsku.getNcartskuid());
        } catch (Exception e) {
            throw new UpdateException("mysql : update delete CartSKU failed");
        }

        List<REcCartsku> cartskuList = cartskuMapper.selectByShoppingCartId(shoppingcart.getNshoppingcartid());
        List<REcSku> skuList = getSKUListBycarskuList(cartskuList);

        List<SKUDTO> skuDTOList = new ArrayList<>();
        //change sku inventory to skudto incentory
        sku.setNinventory(cartsku.getNquantity());
        SKUDTO skudto = ConvertToDTO.convertToSKUDTO(sku);
        skuDTOList.add(skudto);

        //update shoppingcart
        int quantity = ComputeUtils.computeNumber(skuDTOList);
        BigDecimal discount = ComputeUtils.computeDiscount(skuDTOList);
        BigDecimal totalPrice = ComputeUtils.computeTotalPrice(skuDTOList);

        shoppingcart.setNtotalquantity(shoppingcart.getNtotalquantity() - quantity);
        shoppingcart.setNdiscount(shoppingcart.getNdiscount().subtract(discount));
        shoppingcart.setNtotalprice(shoppingcart.getNtotalprice().subtract(totalPrice));

        try {
            shoppingcartMapper.updateByPrimaryKeySelective(shoppingcart);
        } catch (Exception e) {
            throw new UpdateException("mysql : update ShoppingCart failed");
        }

        REcShoppingcart shoppingcartResult = shoppingcartMapper.selectByPrimaryKey(shoppingcart.getNshoppingcartid());
        if (shoppingcartResult == null) {
            throw new UpdateException("mysql : update search ShoppingCart failed");
        }
        ShoppingCartDTO shoppingCartDTO = ConvertToDTO.convertToShoppingDTO(shoppingcart, skuList);
        return shoppingCartDTO;
    }


    /*********************************************check******************************************/

    /**
     *
     * @param skudto
     * @param sku
     * @return
     */
//    @Override
//    public boolean checkSingleSKUParam(SKUDTO skudto, REcSku sku) {
//        if (!skudto.getSkuId().equals(sku.getNskuid())
//                || skudto.getQuantity() > sku.getNinventory() || skudto.getQuantity() <= 0
//                || skudto.getDisplayPrice().compareTo(sku.getNdisplayprice()) != 0) {
//            return false;
//        }
//        return true;
//    }

    /**
     * check sku param include price, quantity, id
     * @param skudtoList
     * @param skuList
     * @return
     */
//    @Override
//    public boolean checkSKUParam(List<SKUDTO> skudtoList, List<REcSku> skuList) {
//        if (skudtoList.size() != skuList.size())
//            return false;
//        Collections.sort(skudtoList, new SortList<SKUDTO>("SkuId",true));
//        for (int i = 0; i < skudtoList.size(); i++) {
//            if (!skudtoList.get(i).getSkuId().equals(skuList.get(i).getNskuid())
//                    || skudtoList.get(i).getQuantity() > skuList.get(i).getNinventory() || skudtoList.get(i).getQuantity() <= 0
//                    || skudtoList.get(i).getDisplayPrice().compareTo(skuList.get(i).getNdisplayprice()) != 0) {
//                return false;
//            }
//        }
//        return true;
//    }

    /**
     * get skuList by dtoList
     * @param skudtoList
     * @return
     */
    @Override
    public List<REcSku> getSKUListByDTOList(List<SKUDTO> skudtoList) {
        List<Long> skuIdList = new ArrayList<>();
        skudtoList.forEach(n -> skuIdList.add(n.getSkuId()));
        List<REcSku> skuList = skuMapper.getSKUList(skuIdList);
        return skuList;
    }

    /**
     * get skuList by cartskuList
     * @param cartskuList
     * @return
     */
    @Override
    public List<REcSku> getSKUListBycarskuList(List<REcCartsku> cartskuList) {
        List<Long> skuIdList = new ArrayList<>();
        List<Integer> quantityList = new ArrayList<>();
        cartskuList.forEach(n -> skuIdList.add(n.getNskuid()));
        cartskuList.forEach(n -> quantityList.add(n.getNquantity()));
        // skuidList don't be null
        List<REcSku> skuList = new ArrayList<>();
        if (skuIdList == null || skuIdList.size() == 0)
            return skuList;
        skuList = skuMapper.getSKUList(skuIdList);
        //inventory is sku's quantity to give dto to show
        for (int i = 0; i < skuList.size(); i++) {
            skuList.get(i).setNinventory(quantityList.get(i));
        }
        return skuList;
    }

    private REcShoppingcart computeQuantityDiscountTotalPrice(List<SKUDTO> skuDTOList, REcShoppingcart shoppingcart) {
        int quantityDTO = ComputeUtils.computeNumber(skuDTOList);
        BigDecimal discountDTO = ComputeUtils.computeDiscount(skuDTOList);
        BigDecimal totalPriceDTO = ComputeUtils.computeTotalPrice(skuDTOList);

        int quantityOriginSKU = 0;
        BigDecimal discountOriginSKU = new BigDecimal(0);
        BigDecimal totalPriceOriginSKU = new BigDecimal(0);
        for (SKUDTO skudto : skuDTOList) {
            REcCartskuKey key = new REcCartskuKey();
            key.setNshoppingcartid(shoppingcart.getNshoppingcartid());
            key.setNskuid(skudto.getSkuId());
            REcCartsku cartsku = cartskuMapper.selectByREcCartskuKey(key);
            if (cartsku == null)
                return null;
            REcSku sku = skuMapper.selectByPrimaryKey(skudto.getSkuId());
            if (sku == null)
                return null;
            quantityOriginSKU += cartsku.getNquantity();
            discountOriginSKU = discountOriginSKU.add(sku.getNdiscount().multiply(new BigDecimal(cartsku.getNquantity())));

            BigDecimal displayPriceBig = sku.getNdisplayprice();
            BigDecimal quantityBig = new BigDecimal(cartsku.getNquantity());
            BigDecimal price = displayPriceBig.multiply(quantityBig);
            totalPriceOriginSKU = totalPriceOriginSKU.add(price);
        }

        int quantity = shoppingcart.getNtotalquantity();
        BigDecimal discount = shoppingcart.getNdiscount();
        BigDecimal totalPrice = shoppingcart.getNtotalprice();

        shoppingcart.setNtotalquantity(quantity - quantityOriginSKU + quantityDTO);
        shoppingcart.setNdiscount(discount.subtract(discountOriginSKU).add(discountDTO));
        shoppingcart.setNtotalprice(totalPrice.subtract(totalPriceOriginSKU).add(totalPriceDTO));

        return shoppingcart;
    }

    private REcShoppingcart getShoppingCartByUserId(Long userId) {
        List<REcShoppingcart> shoppingcartList = shoppingcartMapper.selectByUserId(userId);
        if (shoppingcartList == null || shoppingcartList.size() != 1) {
            return null;
        }
        return shoppingcartList.get(0);
    }

    @Override
    public boolean checkShoppingCartByUserId(Long userId){
        List<REcShoppingcart> shoppingcartList = shoppingcartMapper.selectByUserId(userId);
        if (shoppingcartList == null || shoppingcartList.size() != 0) {
            return false;
        }
        return true;
    }

}
