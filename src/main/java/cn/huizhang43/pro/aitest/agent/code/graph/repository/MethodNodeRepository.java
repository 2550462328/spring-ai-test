package cn.huizhang43.pro.aitest.agent.code.graph.repository;

import cn.huizhang43.pro.aitest.agent.code.graph.entity.MethodNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MethodNodeRepository extends Neo4jRepository<MethodNode, String> {

}
