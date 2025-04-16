package cn.huizhang43.pro.aitest.chat.dto;

import cn.huizhang43.pro.aitest.chat.dao.dto.AiMessageInput;
import lombok.Data;

@Data
public class AiMessageWrapper {
    AiMessageInput message;
    AiMessageParams params;
}
