package cn.huizhang43.pro.aitest.graph.chunk;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface ChunkRepository extends Neo4jRepository<Chunk, String> {
    List<Chunk> findByTextEmbeddingIsNull();
}
