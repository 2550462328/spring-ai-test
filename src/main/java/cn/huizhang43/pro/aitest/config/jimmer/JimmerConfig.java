package cn.huizhang43.pro.aitest.config.jimmer;

import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JimmerConfig {

  @Bean
  public DatabaseNamingStrategy databaseNamingStrategy() {
    return DefaultDatabaseNamingStrategy.LOWER_CASE;
  }
}
