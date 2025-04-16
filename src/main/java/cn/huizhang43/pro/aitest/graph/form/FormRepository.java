package cn.huizhang43.pro.aitest.graph.form;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FormRepository extends Neo4jRepository<Form, String> {
}
