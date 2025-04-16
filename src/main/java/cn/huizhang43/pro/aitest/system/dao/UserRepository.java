package cn.huizhang43.pro.aitest.system.dao;

import cn.huizhang43.pro.aitest.chat.dao.User;
import cn.huizhang43.pro.aitest.chat.dao.UserFetcher;
import cn.huizhang43.pro.aitest.chat.dao.UserTable;
import org.babyfish.jimmer.spring.repository.JRepository;

import java.util.Optional;

public interface UserRepository extends JRepository<User, String> {
    UserTable t = UserTable.$;
    UserFetcher FETCHER = UserFetcher.$.allScalarFields();

    default Optional<User> findByPhone(String phone) {
        return sql().createQuery(t)
                .where(t.phone().eq(phone))
                .select(t)
                .fetchOptional();
    }
}
