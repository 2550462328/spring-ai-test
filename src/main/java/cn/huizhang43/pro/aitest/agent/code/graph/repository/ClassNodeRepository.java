package cn.huizhang43.pro.aitest.agent.code.graph.repository;

import cn.huizhang43.pro.aitest.agent.code.graph.entity.ClassNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ClassNodeRepository extends Neo4jRepository<ClassNode,String> {
}
