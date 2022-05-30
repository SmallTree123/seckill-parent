package com.seckill.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seckill.user.dao.UserMapper;
import com.seckill.user.pojo.User;
import com.seckill.user.service.UserService;
import com.seckill.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/****
 * @Author:www.itheima.com
 * @Description:User业务层接口实现类
 * @Date  0:16
 *****/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;


    /***
     * 用户登录
     * @param username
     * @return
     */
    @Override
    public User findById(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username",username));
    }

    /**
     * User条件+分页查询
     * @param user 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<User> findPage(User user, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        QueryWrapper<User> example = createExample(user);
        //执行搜索
        return new PageInfo<User>(userMapper.selectList(example));
    }

    /**
     * User构建查询对象
     * @param user
     * @return
     */
    public QueryWrapper<User> createExample(User user){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();

        if(user!=null){
            // 用户名
            if(!StringUtils.isEmpty(user.getUsername())){
                queryWrapper.like("username","%"+user.getUsername()+"%");
            }
            // 密码，加密存储
            if(!StringUtils.isEmpty(user.getPassword())){
                queryWrapper.eq("password",user.getPassword());
            }
            // 注册手机号
            if(!StringUtils.isEmpty(user.getPhone())){
                queryWrapper.eq("phone",user.getPhone());
            }
            // 注册邮箱
            if(!StringUtils.isEmpty(user.getEmail())){
                queryWrapper.eq("email",user.getEmail());
            }
            // 创建时间
            if(!StringUtils.isEmpty(user.getCreated())){
//                    criteria.andEqualTo("created",user.getCreated());
                queryWrapper.gt("created",user.getCreated());
                //当天时间
                queryWrapper.lt("created", TimeUtil.addDateHour(user.getCreated(),24));
            }
            // 修改时间
            if(!StringUtils.isEmpty(user.getUpdated())){
                queryWrapper.eq("updated",user.getUpdated());
            }
            // 昵称
            if(!StringUtils.isEmpty(user.getNickName())){
                queryWrapper.eq("nickName",user.getNickName());
            }
            // 真实姓名
            if(!StringUtils.isEmpty(user.getName())){
//                    criteria.andLike("name","%"+user.getName()+"%");
                queryWrapper.like("nickName","%"+user.getName()+"%");
            }
        }
        return queryWrapper;
    }

}
