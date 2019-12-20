package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.consts.AppConsts;
import com.atguigu.core.exception.MemberException;
import com.atguigu.core.utils.ScwUserAppUtils;
import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.ums.util.SmsTemplates;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private MemberDao memberDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SmsTemplates smsTemplates;

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
        //从redis中获取短信校验码
        String mobileCountKey = AppConsts.PHONE_CODE_PREFIX + memberEntity.getMobile() + AppConsts.PHONE_CODE_SUFFIX;
        String codeMobile = this.stringRedisTemplate.opsForValue().get(mobileCountKey);

        if (!code.equals(codeMobile)) {
            throw new MemberException("验证码错误");
        }
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

    @Override
    public String sendSms(String mobile) {
        int count = 0;
        // 判断手机号是否存在
        boolean flag = ScwUserAppUtils.isPhone(mobile);
        if (!flag) {
            return "手机号不存在，请重新输入";
        }
        // 判断手机号24小时次数是否超过三次
        // 拼接手机号码，保存对应次数
        String mobileCountKey = AppConsts.PHONE_CODE_PREFIX + mobile + AppConsts.PHONE_COUNT_SUFFIX;
        flag = stringRedisTemplate.hasKey(mobileCountKey);
        if (flag) {
            // 存在验证码
            String str = stringRedisTemplate.opsForValue().get(mobileCountKey);
            count = Integer.parseInt(str);
            if (count >= 3) {
                return "今日验证码获取炒过上限";
            }
        }
        // 判断手机号是否存在未使用的验证码
        // 拼接手机号码保存的键
        String phoneCodeKey = AppConsts.PHONE_CODE_PREFIX + mobile + AppConsts.PHONE_CODE_SUFFIX;
        flag = stringRedisTemplate.hasKey(phoneCodeKey);
        if (flag) {
            // 代表存在
            return "验证码获取过于频繁";
        }
        // 生成6位验证码
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        // 发送
        flag = true;
        this.smsTemplates.sendSms(mobile, code, "TP1711063");
        if (!flag) {
            // 发送失败
            return "短信发送失败";
        }
        // 发送成功，将手机号和对应的验证码保存十分钟
        stringRedisTemplate.opsForValue().set(phoneCodeKey, code, 600, TimeUnit.SECONDS);
        // 更新当前手机号24小时内获取验证码的次数
        if (count == 0) {
            stringRedisTemplate.opsForValue().set(mobileCountKey, "1", 24, TimeUnit.HOURS);
        } else {
            stringRedisTemplate.boundValueOps(mobileCountKey).increment(count);
        }
        return "短信发送成功";
    }
}