package com.alibaba.fescar.test.spring;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

@Ignore
public class PurchaseServiceTest {

    private PurchaseService businessService;

    @Before
    public void init() {
        JdbcTemplate jdbcTemplate = DataSourceFactory.createRawJdbcTemplate();

        jdbcTemplate.execute("DELETE FROM storage_tbl WHERE commodity_code='C1000'");
        jdbcTemplate.execute("INSERT INTO storage_tbl(id,commodity_code,count) VALUES(10000,'C1000',10000)");

        jdbcTemplate.execute("DELETE FROM account_tbl WHERE user_id='U1000'");
        jdbcTemplate.execute("INSERT INTO account_tbl(id,user_id,money) VALUES(10000,'U1000',10000)");

        jdbcTemplate.execute("DELETE FROM order_tbl WHERE user_id='U1000'");

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-purchase-service.xml");
        businessService = (PurchaseService) context.getBean("businessService");

    }

    @Test
    public void testRollback() {
        try {
            businessService.purchaseRollback("U1000", "C1000", 10);
        } catch (PurchaseService.RollbackException ex) {
            System.out.println(ex.getMessage());
        }
        int ret;
        JdbcTemplate jdbcTemplate = DataSourceFactory.createJdbcTemplate();

        ret = jdbcTemplate.queryForInt("SELECT count(1) FROM storage_tbl WHERE commodity_code='C1000' AND count=10000 ");
        Assert.assertTrue("storage_tbl assert failure", ret == 1);

        ret = jdbcTemplate.queryForInt("SELECT count(1) FROM account_tbl WHERE user_id='U1000' AND money=10000 ");
        Assert.assertTrue("account_tbl assert failure", ret == 1);

        ret = jdbcTemplate.queryForInt("SELECT count(1) FROM order_tbl WHERE user_id='U1000'");
        Assert.assertTrue("order_tbl assert failure", ret == 0);

    }

    @Test
    public void testCommit() {
        businessService.purchase("U1000", "C1000", 10);

        int ret;
        JdbcTemplate jdbcTemplate = DataSourceFactory.createJdbcTemplate();

        ret = jdbcTemplate.queryForInt("SELECT count(1) FROM storage_tbl WHERE commodity_code='C1000' AND count=10000");
        Assert.assertTrue("storage_tbl assert failure", ret == 0);

        ret = jdbcTemplate.queryForInt("SELECT count(1) FROM account_tbl WHERE user_id='U1000' AND money=10000 ");
        Assert.assertTrue("account_tbl assert failure", ret == 0);

        ret = jdbcTemplate.queryForInt("SELECT count(1) FROM order_tbl WHERE user_id='U1000'");
        Assert.assertTrue("order_tbl assert failure", ret == 1);
    }



}
