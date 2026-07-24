package cn.huizhang43.pro.aitest.graph.manager;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ManagerRepository extends Neo4jRepository<Manager, String> {
}
