package cn.huizhang43.pro.aitest.system;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.huizhang43.pro.aitest.base.BusinessException;
import cn.huizhang43.pro.aitest.chat.dao.User;
import cn.huizhang43.pro.aitest.chat.dao.dto.UserLoginInput;
import cn.huizhang43.pro.aitest.system.dao.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("user")
@AllArgsConstructor
public class LoginController {
    
    private final UserRepository userRepository;
    
    @PostMapping("login")
    public SaTokenInfo login(@RequestBody UserLoginInput input) {
        User databaseUser = userRepository.findByPhone(input.getPhone())
                .orElseThrow(() -> new BusinessException("用户名/密码错误"));
        if (!BCrypt.checkpw(input.getPassword(), databaseUser.password())) {
            throw new BusinessException("用户名/密码错误");
        }
        StpUtil.login(databaseUser.id());
        return StpUtil.getTokenInfo();
    }

 
}
