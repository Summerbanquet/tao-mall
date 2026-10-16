package com.tao.web.controller.adm;

import com.tao.common.entity.AjaxResult;
import com.tao.common.entity.PageResult;
import com.tao.common.util.EncryptUtil;
import com.tao.common.util.NickNameUtil;
import com.tao.user.entity.User;
import com.tao.user.entity.query.UserQuery;
import com.tao.user.entity.vo.UserVO;
import com.tao.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理的控制器
 */
@RestController
@RequestMapping("/adm/user")
public class UserAdmController {

    @Autowired
    private UserService userService;

    /**
     * 用户数据分页条件查询
     * 说明：返回 UserVO，避免把密码等敏感字段暴露给前端
     * @param query
     * @return
     */
    @PostMapping("/page")
    public AjaxResult page(@RequestBody UserQuery query) {
        PageResult<User> page = userService.page(query);
        List<UserVO> rows = page.getRows().stream().map(o -> toVO((User) o)).collect(Collectors.toList());
        return AjaxResult.success(new PageResult<UserVO>(page.getTotal(), rows));
    }

    /**
     * @param query
     * @return
     */
    @PostMapping("/listSystem")
    public AjaxResult listSystem(@RequestBody UserQuery query) {
        query.setType(1);
        query.setStatus(1);
        List<UserVO> list = userService.list(query);
        return AjaxResult.success(list);
    }


    @PostMapping("/listUser")
    public AjaxResult listUser(@RequestBody UserQuery query) {
        query.setType(2);
        query.setStatus(1);
        List<UserVO> list = userService.list(query);
        return  AjaxResult.success(list);
    }

    /**
     * 用户详情（脱敏，不含密码）
     */
    @GetMapping("/getInfo/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id){
        User user = userService.getById(id);
        if (user == null) {
            return AjaxResult.error("用户不存在");
        }
        return AjaxResult.success(toVO(user));
    }


    @PostMapping("/add")
    public AjaxResult add(@RequestBody User user){
        //自动生成昵称
        user.setNickName(NickNameUtil.generate());
        //对新增用户密码进行加密
        String encPwd = EncryptUtil.encrypt(user.getPassword());
        user.setPassword(encPwd);
        user.setType(1);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        //判断当前新增用户用户名是否已被占用
        if(!userService.checkUserNameUnique(user.getUserName())) {
            return AjaxResult.error("当前用户名已被占用，请重新输入");
        }

        return userService.save(user)?AjaxResult.success("添加用户成功",true):AjaxResult.error("添加用户失败",false);
    }
    /**
     * 修改用户信息（运营、访问）
     * @param user
     * @return
     */
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody User user) {
        return userService.updateById(user) ? AjaxResult.success("修改用户成功", true) : AjaxResult.error("修改用户失败", false);
    }

    /**
     * 指定用户编号删除
     * @param id
     * @return
     */
    @DeleteMapping("/dele/{id}")
    public AjaxResult dele(@PathVariable("id") Long id) {
        return userService.removeById(id) ? AjaxResult.success("删除用户成功", true) : AjaxResult.error("删除用户失败", false);
    }
    /**
     * 指定用户编号删除
     * @param ids
     * @return
     */
    @DeleteMapping("/deles")
    public AjaxResult deles(@RequestBody Long[] ids) {
        int count = userService.removes(ids);
        return count > 0 ? AjaxResult.success("成功删除" + count + "个用户", true) : AjaxResult.error("删除用户失败", false);
    }

    /**
     * User 转 UserVO（去掉密码等敏感字段）
     */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}