package cn.huizhang43.pro.aitest.chat;

import cn.huizhang43.pro.aitest.base.BusinessException;
import cn.huizhang43.pro.aitest.chat.dao.AiSession;
import cn.huizhang43.pro.aitest.chat.dao.AiSessionRepository;
import cn.huizhang43.pro.aitest.chat.dao.dto.AiSessionInput;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.client.FetchBy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("session")
public class ChatSessionController {

    private final AiSessionRepository sessionRepository;

    /**
     * 查询当前登录用户的会话
     *
     * @return 会话列表
     */
    @GetMapping("user")
    public List<@FetchBy(value = "FETCHER", ownerType = AiSessionRepository.class) AiSession> findByUser() {
        return sessionRepository.findByUser();
    }

    /**
     * 保存会话
     * @param input 会话dto参考src/main/dto/AiSession.dto
     * @return 创建后的id
     */
    @PostMapping("save")
    public String save(@RequestBody AiSessionInput input) {
        return sessionRepository.save(input.toEntity()).id();
    }

    /**
     * 根据id查询会话
     * @param id 会话id
     * @return 会话信息
     */
    @GetMapping("{id}")
    public @FetchBy(value = "FETCHER", ownerType = AiSessionRepository.class) AiSession findById(@PathVariable String id) {
        return sessionRepository.findById(id, AiSessionRepository.FETCHER).orElseThrow(() -> new BusinessException("会话不存在"));
    }

    /**
     * 批量删除会话
     * @param ids 会话id列表
     */
    @DeleteMapping
    public void delete(@RequestBody List<String> ids) {
        sessionRepository.deleteByIds(ids);
    }
}
