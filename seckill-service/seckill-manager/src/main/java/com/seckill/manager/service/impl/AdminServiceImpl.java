package com.seckill.manager.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seckill.manager.dao.AdminMapper;
import com.seckill.manager.pojo.Admin;
import com.seckill.manager.service.AdminService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
/****
 * @Author:www.itheima.com
 * @Description:Admin业务层接口实现类
 * @Date  0:16
 *****/
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /****
     * 根据名字查询管理员
     * @param username
     * @return
     */
    @Override
    public Admin findByName(String username) {
        return adminMapper.selectOne(new QueryWrapper<Admin>().eq("loginName",username));
    }
}
