package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.MemberException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private MemberDao memberDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String param, Integer type) {
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", param);
                break;
            case 2:
                wrapper.eq("mobile", param);
                break;
            case 3:
                wrapper.eq("email", param);
                break;
            default:
                return null;
        }

        return this.memberDao.selectCount(wrapper) == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        //校验短信验证码
        //生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        memberEntity.setSalt(salt);
        //对密码加密
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword() + salt));
        this.save(memberEntity);
        //写入数据库
        //删除redis验证码
    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        //通过用户名查询数据库信息
        MemberEntity memberEntity = this.memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", username));
        if (memberEntity == null) {
            throw new MemberException("用户名或密码错误");
        }
        String salt = memberEntity.getSalt();//获取用户的盐
        String passstr = DigestUtils.md5Hex(password + salt);
        if (!memberEntity.getPassword().equals(passstr)) {
            throw new MemberException("用户名或密码错误");
        }
        return memberEntity;
    }
}